package io.jarv.docgen.builder;

import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.TableStyle;
import io.jarv.docgen.style.TextStyle;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;

import jakarta.xml.bind.JAXBElement;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class TableBuilderTest {

    @Test
    void twoRowsTwoColumnsProducesCorrectStructure() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginTable(TableStyle.bordered())
                .beginRow()
                .addCell("A1", TextStyle.defaults())
                .addCell("B1", TextStyle.defaults())
                .endRow()
                .beginRow()
                .addCell("A2", TextStyle.defaults())
                .addCell("B2", TextStyle.defaults())
                .endRow()
                .endTable())) {

            Tbl table = round.tables().get(0);
            var rows = RoundTrip.filter(table.getContent(), Tr.class);
            assertThat(rows).hasSize(2);
            var row0Cells = RoundTrip.filter(rows.get(0).getContent(), Tc.class);
            assertThat(row0Cells).hasSize(2);
            assertThat(cellText(row0Cells.get(0))).isEqualTo("A1");
            assertThat(cellText(row0Cells.get(1))).isEqualTo("B1");

            var row1Cells = RoundTrip.filter(rows.get(1).getContent(), Tc.class);
            assertThat(cellText(row1Cells.get(0))).isEqualTo("A2");
            assertThat(cellText(row1Cells.get(1))).isEqualTo("B2");
        }
    }

    @Test
    void borderedTableSetsSingleOnAllSides() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginTable(TableStyle.bordered())
                .beginRow().addCell("x", TextStyle.defaults()).endRow()
                .endTable())) {

            TblBorders borders = round.tables().get(0).getTblPr().getTblBorders();
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
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginTable(TableStyle.borderless())
                .beginRow().addCell("x", TextStyle.defaults()).endRow()
                .endTable())) {

            TblBorders borders = round.tables().get(0).getTblPr().getTblBorders();
            assertThat(borders.getTop().getVal()).isEqualTo(STBorder.NONE);
            assertThat(borders.getInsideH().getVal()).isEqualTo(STBorder.NONE);
        }
    }

    @Test
    void outerOnlyLeavesInnerNone() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginTable(TableStyle.outerOnly())
                .beginRow().addCell("a", TextStyle.defaults()).endRow()
                .beginRow().addCell("b", TextStyle.defaults()).endRow()
                .endTable())) {

            TblBorders borders = round.tables().get(0).getTblPr().getTblBorders();
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
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginTable(style)
                .beginRow().addCell("x", TextStyle.defaults()).endRow()
                .endTable())) {

            var top = round.tables().get(0).getTblPr().getTblBorders().getTop();
            // 2pt → 16 eighths of a point
            assertThat(top.getSz()).isEqualTo(BigInteger.valueOf(16));
            assertThat(top.getColor()).isEqualTo("FF0000");
        }
    }

    private static String cellText(Tc cell) {
        StringBuilder sb = new StringBuilder();
        for (P p : RoundTrip.filter(cell.getContent(), P.class)) {
            for (R r : RoundTrip.filter(p.getContent(), R.class)) {
                for (Object o : r.getContent()) {
                    Object unwrapped = (o instanceof JAXBElement<?> je) ? je.getValue() : o;
                    if (unwrapped instanceof Text t) sb.append(t.getValue());
                }
            }
        }
        return sb.toString();
    }
}
