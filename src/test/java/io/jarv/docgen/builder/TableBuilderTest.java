package io.jarv.docgen.builder;

import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.TableStyle;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTTblBorders;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TableBuilderTest {

    @Test
    void twoRowsTwoColumnsProducesCorrectStructure() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginTable(TableStyle.bordered())
                .beginRow()
                .addCell("A1", TextStyle.defaults())
                .addCell("B1", TextStyle.defaults())
                .endRow()
                .beginRow()
                .addCell("A2", TextStyle.defaults())
                .addCell("B2", TextStyle.defaults())
                .endRow()
                .endTable())) {

            XWPFTable table = round.getTables().get(0);
            assertThat(table.getRows()).hasSize(2);
            assertThat(table.getRow(0).getTableCells()).hasSize(2);
            assertThat(table.getRow(0).getCell(0).getText()).isEqualTo("A1");
            assertThat(table.getRow(0).getCell(1).getText()).isEqualTo("B1");
            assertThat(table.getRow(1).getCell(0).getText()).isEqualTo("A2");
            assertThat(table.getRow(1).getCell(1).getText()).isEqualTo("B2");
        }
    }

    @Test
    void borderedTableSetsSingleOnAllSides() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginTable(TableStyle.bordered())
                .beginRow()
                .addCell("x", TextStyle.defaults())
                .endRow()
                .endTable())) {

            CTTblBorders borders = round.getTables().get(0).getCTTbl().getTblPr().getTblBorders();
            assertThat(borders.getTop().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(borders.getBottom().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(borders.getLeft().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(borders.getRight().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(borders.getInsideH().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(borders.getInsideV().getVal()).isEqualTo(STBorder.SINGLE);
        }
    }

    @Test
    void borderlessTableSetsNoneOnAllSides() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginTable(TableStyle.borderless())
                .beginRow()
                .addCell("x", TextStyle.defaults())
                .endRow()
                .endTable())) {

            CTTblBorders borders = round.getTables().get(0).getCTTbl().getTblPr().getTblBorders();
            assertThat(borders.getTop().getVal()).isEqualTo(STBorder.NONE);
            assertThat(borders.getInsideH().getVal()).isEqualTo(STBorder.NONE);
        }
    }

    @Test
    void outerOnlyLeavesInnerNone() throws Exception {
        try (XWPFDocument round = RoundTrip.of(b -> b.beginTable(TableStyle.outerOnly())
                .beginRow().addCell("a", TextStyle.defaults()).endRow()
                .beginRow().addCell("b", TextStyle.defaults()).endRow()
                .endTable())) {

            CTTblBorders borders = round.getTables().get(0).getCTTbl().getTblPr().getTblBorders();
            assertThat(borders.getTop().getVal()).isEqualTo(STBorder.SINGLE);
            assertThat(borders.getInsideH().getVal()).isEqualTo(STBorder.NONE);
        }
    }

    @Test
    void customBorderWidthAndColorApplied() throws Exception {
        TableStyle style = TableStyle.builder()
                .outer(Border.builder().widthPoints(2.0).colorHex("FF0000").build())
                .inner(Border.none())
                .build();
        try (XWPFDocument round = RoundTrip.of(b -> b.beginTable(style)
                .beginRow().addCell("x", TextStyle.defaults()).endRow()
                .endTable())) {

            var top = round.getTables().get(0).getCTTbl().getTblPr().getTblBorders().getTop();
            // 2pt → 16 eighths of a point
            assertThat(top.getSz()).isEqualTo(BigInteger.valueOf(16));
            assertThat(top.xgetColor().getStringValue()).isEqualTo("FF0000");
        }
    }
}
