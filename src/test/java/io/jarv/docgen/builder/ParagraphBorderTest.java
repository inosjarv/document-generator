package io.jarv.docgen.builder;

import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.BorderSet;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ParagraphBorderTest {

    @Test
    void paragraphWithoutBorderStyleHasNoPBdr() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("plain", TextStyle.defaults()))) {
            var ppr = round.getParagraphs().get(0).getCTP().getPPr();
            assertThat(ppr == null || ppr.getPBdr() == null).isTrue();
        }
    }

    @Test
    void borderOnAllSidesWritesFourEntries() throws Exception {
        ParagraphStyle boxed = ParagraphStyle.builder()
                .border(BorderSet.all(Border.simple())).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("boxed", TextStyle.defaults(), boxed))) {
            CTPBdr bdr = round.getParagraphs().get(0).getCTP().getPPr().getPBdr();
            assertThat(bdr.getTop().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(bdr.getBottom().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(bdr.getLeft().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(bdr.getRight().getVal()).isEqualTo(STBorder.SINGLE);
        }
    }

    @Test
    void horizontalRulesLeaveSidesOff() throws Exception {
        ParagraphStyle rules = ParagraphStyle.builder()
                .border(BorderSet.horizontal(Border.simple())).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("hr", TextStyle.defaults(), rules))) {
            CTPBdr bdr = round.getParagraphs().get(0).getCTP().getPPr().getPBdr();
            assertThat(bdr.getTop().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(bdr.getBottom().getVal()).isEqualTo(STBorder.SINGLE);
            // horizontal() leaves left/right as null → helper writes NONE
            assertThat(bdr.getLeft().getVal()).isEqualTo(STBorder.NONE);
            assertThat(bdr.getRight().getVal()).isEqualTo(STBorder.NONE);
        }
    }

    @Test
    void customBorderWidthAndColorApplied() throws Exception {
        ParagraphStyle styled = ParagraphStyle.builder()
                .border(BorderSet.all(Border.builder()
                        .widthPoints(1.5).colorHex("#2C3E50").spacingPoints(8).build()))
                .build();
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("x", TextStyle.defaults(), styled))) {
            var top = round.getParagraphs().get(0).getCTP().getPPr().getPBdr().getTop();
            // 1.5pt → 12 eighths of a point
            assertThat(top.getSz()).isEqualTo(BigInteger.valueOf(12));
            assertThat(top.xgetColor().getStringValue()).isEqualTo("2C3E50");
            assertThat(top.getSpace()).isEqualTo(BigInteger.valueOf(8));
        }
    }

    @Test
    void borderStyleNoneWritesNoneVal() throws Exception {
        ParagraphStyle styled = ParagraphStyle.builder()
                .border(BorderSet.none()).build();
        try (XWPFDocument round = RoundTrip.of(b -> b.addText("x", TextStyle.defaults(), styled))) {
            var top = round.getParagraphs().get(0).getCTP().getPPr().getPBdr().getTop();
            assertThat(top.getVal()).isEqualTo(STBorder.NONE);
        }
    }
}
