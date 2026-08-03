package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WordDocumentBuilderTest {

    @Test
    void producesReadableDocxBytes() throws Exception {
        byte[] bytes;
        try (WordDocumentBuilder builder = new WordDocumentBuilder()) {
            builder.addText("Hello", TextStyle.defaults());
            bytes = builder.buildAsBytes();
        }
        assertThat(bytes).isNotEmpty();
        try (XWPFDocument round = RoundTrip.read(bytes)) {
            assertThat(round.getParagraphs()).hasSize(1);
        }
    }

    @Test
    void addTextCreatesSingleRunParagraphWithStyleApplied() throws Exception {
        TextStyle style = TextStyle.builder()
                .fontSize(14).bold(true).colorHex("#FF0000").build();
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("Heading", style))) {
            List<XWPFParagraph> paragraphs = round.getParagraphs();
            assertThat(paragraphs).hasSize(1);
            XWPFRun run = paragraphs.get(0).getRuns().get(0);
            assertThat(run.getText(0)).isEqualTo("Heading");
            assertThat(run.isBold()).isTrue();
            assertThat(run.getFontSize()).isEqualTo(14);
            assertThat(run.getColor()).isEqualTo("FF0000");
        }
    }

    @Test
    void multiRunParagraphKeepsEachRunSeparate() throws Exception {
        TextStyle normal = TextStyle.defaults();
        TextStyle emphasis = TextStyle.builder().italic(true).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.beginParagraph()
                .addRun("plain ", normal)
                .addRun("italic", emphasis)
                .addRun(" plain again", normal)
                .endParagraph())) {
            List<XWPFRun> runs = round.getParagraphs().get(0).getRuns();
            assertThat(runs).hasSize(3);
            assertThat(runs.get(0).isItalic()).isFalse();
            assertThat(runs.get(1).isItalic()).isTrue();
            assertThat(runs.get(2).isItalic()).isFalse();
        }
    }

    @Test
    void themeMarginsAreAppliedInTwips() throws Exception {
        DocumentTheme theme = DocumentTheme.builder()
                .marginTop(2.0).marginBottom(1.0).marginLeft(1.25).marginRight(0.75).build();
        try (XWPFDocument round = RoundTrip.of(theme,
                b -> b.addText("x", TextStyle.defaults()))) {
            var pageMar = round.getDocument().getBody().getSectPr().getPgMar();
            assertThat(pageMar.getTop()).isEqualTo(BigInteger.valueOf(2880));
            assertThat(pageMar.getBottom()).isEqualTo(BigInteger.valueOf(1440));
            assertThat(pageMar.getLeft()).isEqualTo(BigInteger.valueOf(1800));
            assertThat(pageMar.getRight()).isEqualTo(BigInteger.valueOf(1080));
        }
    }

    @Test
    void nullTextBecomesEmptyRun() throws Exception {
        try (XWPFDocument round = RoundTrip.of(
                b -> b.addText(null, TextStyle.defaults()))) {
            XWPFRun run = round.getParagraphs().get(0).getRuns().get(0);
            assertThat(run.getText(0)).isEmpty();
        }
    }

    @Test
    void invalidColorHexRejectedAtWrite() throws Exception {
        try (WordDocumentBuilder builder = new WordDocumentBuilder()) {
            TextStyle bad = TextStyle.builder().colorHex("nope").build();
            assertThatThrownBy(() -> builder.addText("x", bad))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Test
    void writeToRejectsNullStream() throws Exception {
        try (WordDocumentBuilder builder = new WordDocumentBuilder()) {
            assertThatThrownBy(() -> builder.writeTo(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Test
    void titleParagraphKeepsConfigurableSpaceAfterInPoints() throws Exception {
        TextStyle bold = TextStyle.builder().bold(true).fontSize(16).build();
        TextStyle body = TextStyle.defaults();
        ParagraphStyle titleBlock = ParagraphStyle.builder().spaceAfter(6.0).build();

        try (XWPFDocument round = RoundTrip.of(b -> b
                .addText("Title", bold, titleBlock)
                .addText("Body body body", body))) {

            assertThat(round.getParagraphs()).hasSize(2);
            // POI stores spacingAfter in twentieths of a point → 6pt = 120.
            assertThat(round.getParagraphs().get(0).getSpacingAfter()).isEqualTo(120);
            assertThat(round.getParagraphs().get(0).getRuns().get(0).isBold()).isTrue();
            assertThat(round.getParagraphs().get(1).getRuns().get(0).isBold()).isFalse();
        }
    }

    @Test
    void debugXmlDumpIncludesBodyAndHeaderSections() throws Exception {
        try (WordDocumentBuilder builder = new WordDocumentBuilder()) {
            builder.beginHeader()
                    .addRightText("hdr", TextStyle.defaults())
                    .endHeader()
                    .addText("body", TextStyle.defaults());
            String dump = builder.debugXmlDump();
            assertThat(dump).contains("MAIN DOCUMENT");
            assertThat(dump).contains("HEADERS");
            assertThat(dump).contains("hdr");
            assertThat(dump).contains("body");
        }
    }
}
