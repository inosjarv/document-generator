package io.jarv.docgen.builder;

import io.jarv.docgen.style.Alignment;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.TextStyle;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.Br;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.R;
import org.docx4j.wml.UnderlineEnumeration;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ParagraphBuilderTest {

    @Test
    void centerAlignmentIsPersisted() throws Exception {
        ParagraphStyle centered = ParagraphStyle.builder().alignment(Alignment.CENTER).build();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginParagraph(centered)
                .addRun("x", TextStyle.defaults()).endParagraph())) {
            assertThat(round.paragraphs().get(0).getPPr().getJc().getVal())
                    .isEqualTo(JcEnumeration.CENTER);
        }
    }

    @Test
    void justifyMapsToBoth() throws Exception {
        ParagraphStyle justified = ParagraphStyle.builder().alignment(Alignment.JUSTIFY).build();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginParagraph(justified)
                .addRun("x", TextStyle.defaults()).endParagraph())) {
            assertThat(round.paragraphs().get(0).getPPr().getJc().getVal())
                    .isEqualTo(JcEnumeration.BOTH);
        }
    }

    @Test
    void addLineBreakInsertsExactlyOneBreak() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginParagraph()
                .addRun("line1", TextStyle.defaults())
                .addLineBreak()
                .addRun("line2", TextStyle.defaults())
                .endParagraph())) {
            long breaks = RoundTrip.filter(round.paragraphs().get(0).getContent(), R.class).stream()
                    .flatMap(r -> r.getContent().stream())
                    .map(o -> (o instanceof JAXBElement<?> je) ? je.getValue() : o)
                    .filter(Br.class::isInstance)
                    .count();
            assertThat(breaks).isEqualTo(1);
        }
    }

    @Test
    void indentAppliedInTwips() throws Exception {
        ParagraphStyle indented = ParagraphStyle.builder().indentLeft(0.5).build();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginParagraph(indented)
                .addRun("x", TextStyle.defaults()).endParagraph())) {
            assertThat(round.paragraphs().get(0).getPPr().getInd().getLeft())
                    .isEqualTo(BigInteger.valueOf(720));
        }
    }

    @Test
    void underlineFlagIsAppliedToRun() throws Exception {
        TextStyle underlined = TextStyle.builder().underline(true).build();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.addText("x", underlined))) {
            R run = WordDocumentBuilderTest.firstRun(round.paragraphs().get(0));
            assertThat(run.getRPr().getU().getVal()).isEqualTo(UnderlineEnumeration.SINGLE);
        }
    }
}
