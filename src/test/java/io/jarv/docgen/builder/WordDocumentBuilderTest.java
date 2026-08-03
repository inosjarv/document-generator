package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.TextStyle;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Text;
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
        try (RoundTrip.Doc round = RoundTrip.read(bytes)) {
            assertThat(round.paragraphs()).hasSize(1);
        }
    }

    @Test
    void addTextCreatesSingleRunParagraphWithStyleApplied() throws Exception {
        TextStyle style = TextStyle.builder()
                .fontSize(14).bold(true).colorHex("#FF0000").build();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.addText("Heading", style))) {
            List<P> paragraphs = round.paragraphs();
            assertThat(paragraphs).hasSize(1);
            R run = firstRun(paragraphs.get(0));
            assertThat(runText(run)).isEqualTo("Heading");
            assertThat(run.getRPr().getB().isVal()).isTrue();
            assertThat(run.getRPr().getSz().getVal()).isEqualTo(BigInteger.valueOf(28)); // half-points
            assertThat(run.getRPr().getColor().getVal()).isEqualTo("FF0000");
        }
    }

    @Test
    void multiRunParagraphKeepsEachRunSeparate() throws Exception {
        TextStyle normal = TextStyle.defaults();
        TextStyle emphasis = TextStyle.builder().italic(true).build();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginParagraph()
                .addRun("plain ", normal)
                .addRun("italic", emphasis)
                .addRun(" plain again", normal)
                .endParagraph())) {
            List<R> runs = runs(round.paragraphs().get(0));
            assertThat(runs).hasSize(3);
            assertThat(runs.get(0).getRPr().getI()).isNull();
            assertThat(runs.get(1).getRPr().getI().isVal()).isTrue();
            assertThat(runs.get(2).getRPr().getI()).isNull();
        }
    }

    @Test
    void themeMarginsAreAppliedInTwips() throws Exception {
        DocumentTheme theme = DocumentTheme.builder()
                .marginTop(2.0).marginBottom(1.0).marginLeft(1.25).marginRight(0.75).build();
        try (RoundTrip.Doc round = RoundTrip.of(theme,
                b -> b.addText("x", TextStyle.defaults()))) {
            var pageMar = round.body().getSectPr().getPgMar();
            assertThat(pageMar.getTop()).isEqualTo(BigInteger.valueOf(2880));
            assertThat(pageMar.getBottom()).isEqualTo(BigInteger.valueOf(1440));
            assertThat(pageMar.getLeft()).isEqualTo(BigInteger.valueOf(1800));
            assertThat(pageMar.getRight()).isEqualTo(BigInteger.valueOf(1080));
        }
    }

    @Test
    void nullTextBecomesEmptyRun() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(
                b -> b.addText(null, TextStyle.defaults()))) {
            R run = firstRun(round.paragraphs().get(0));
            assertThat(runText(run)).isEmpty();
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

        try (RoundTrip.Doc round = RoundTrip.of(b -> b
                .addText("Title", bold, titleBlock)
                .addText("Body body body", body))) {

            List<P> paragraphs = round.paragraphs();
            assertThat(paragraphs).hasSize(2);
            // 6pt → 120 twentieths of a point.
            assertThat(paragraphs.get(0).getPPr().getSpacing().getAfter())
                    .isEqualTo(BigInteger.valueOf(120));
            assertThat(firstRun(paragraphs.get(0)).getRPr().getB().isVal()).isTrue();
            assertThat(firstRun(paragraphs.get(1)).getRPr().getB()).isNull();
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

    // ----- helpers -----

    static List<R> runs(P paragraph) {
        return RoundTrip.filter(paragraph.getContent(), R.class);
    }

    static R firstRun(P paragraph) {
        return runs(paragraph).get(0);
    }

    static String runText(R run) {
        StringBuilder sb = new StringBuilder();
        for (Object o : run.getContent()) {
            Object unwrapped = (o instanceof JAXBElement<?> je) ? je.getValue() : o;
            if (unwrapped instanceof Text t) sb.append(t.getValue());
        }
        return sb.toString();
    }
}
