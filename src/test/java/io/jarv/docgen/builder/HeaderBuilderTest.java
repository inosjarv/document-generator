package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderBuilderTest {

    @Test
    void singleRightEntryProducesNoLineBreak() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginHeader()
                .addRightText("One", TextStyle.defaults())
                .endHeader())) {
            assertThat(countBreaksInRightHeaderCell(round)).isZero();
        }
    }

    @Test
    void twoRightEntriesGetExactlyOneBreakBetween() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginHeader()
                .addRightText("First", TextStyle.defaults())
                .addRightText("Second", TextStyle.defaults())
                .endHeader())) {
            assertThat(countBreaksInRightHeaderCell(round)).isEqualTo(1);
        }
    }

    @Test
    void threeRightEntriesGetExactlyTwoBreaks() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginHeader()
                .addRightText("A", TextStyle.defaults())
                .addRightText("B", TextStyle.defaults())
                .addRightText("C", TextStyle.defaults())
                .endHeader())) {
            assertThat(countBreaksInRightHeaderCell(round)).isEqualTo(2);
        }
    }

    @Test
    void headerMarginIsAppliedInTwips() throws Exception {
        DocumentTheme theme = DocumentTheme.builder().marginHeader(0.75).build();
        try (XWPFDocument round = RoundTrip.of(theme, b -> b.beginHeader()
                .addRightText("x", TextStyle.defaults())
                .endHeader())) {
            var pageMar = round.getDocument().getBody().getSectPr().getPgMar();
            assertThat(pageMar.getHeader()).isEqualTo(java.math.BigInteger.valueOf(1080L));
        }
    }

    private static long countBreaksInRightHeaderCell(XWPFDocument doc) {
        XWPFHeader header = doc.getHeaderList().get(0);
        XWPFParagraph rightPara = header.getTables().get(0)
                .getRow(0).getCell(1).getParagraphs().get(0);
        return rightPara.getRuns().stream()
                .mapToLong(r -> r.getCTR().getBrList().size())
                .sum();
    }
}
