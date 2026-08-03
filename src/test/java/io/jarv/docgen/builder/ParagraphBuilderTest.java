package io.jarv.docgen.builder;

import io.jarv.docgen.style.Alignment;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ParagraphBuilderTest {

    @Test
    void centerAlignmentIsPersisted() throws Exception {
        ParagraphStyle centered = ParagraphStyle.builder().alignment(Alignment.CENTER).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.beginParagraph(centered)
                .addRun("x", TextStyle.defaults()).endParagraph())) {
            assertThat(round.getParagraphs().get(0).getAlignment())
                    .isEqualTo(ParagraphAlignment.CENTER);
        }
    }

    @Test
    void justifyMapsToPoiBoth() throws Exception {
        ParagraphStyle justified = ParagraphStyle.builder().alignment(Alignment.JUSTIFY).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.beginParagraph(justified)
                .addRun("x", TextStyle.defaults()).endParagraph())) {
            assertThat(round.getParagraphs().get(0).getAlignment())
                    .isEqualTo(ParagraphAlignment.BOTH);
        }
    }

    @Test
    void addLineBreakInsertsExactlyOneBreak() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginParagraph()
                .addRun("line1", TextStyle.defaults())
                .addLineBreak()
                .addRun("line2", TextStyle.defaults())
                .endParagraph())) {
            long breaks = round.getParagraphs().get(0).getRuns().stream()
                    .mapToLong(r -> r.getCTR().getBrList().size()).sum();
            assertThat(breaks).isEqualTo(1);
        }
    }

    @Test
    void indentAppliedInTwips() throws Exception {
        ParagraphStyle indented = ParagraphStyle.builder().indentLeft(0.5).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.beginParagraph(indented)
                .addRun("x", TextStyle.defaults()).endParagraph())) {
            assertThat(round.getParagraphs().get(0).getIndentationLeft()).isEqualTo(720);
        }
    }

    @Test
    void underlineFlagIsAppliedToRun() throws Exception {
        TextStyle underlined = TextStyle.builder().underline(true).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("x", underlined))) {
            assertThat(round.getParagraphs().get(0).getRuns().get(0).getUnderline())
                    .isEqualTo(UnderlinePatterns.SINGLE);
        }
    }
}
