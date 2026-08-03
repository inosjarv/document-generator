package io.jarv.docgen.builder;

import io.jarv.docgen.internal.Docx4jTableBorders;
import io.jarv.docgen.style.TableStyle;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tr;

import java.math.BigInteger;

public class TableBuilder {

    private final WordDocumentBuilder parent;
    private final Tbl table;
    private int firstRowCellCount = 0;
    private int currentRowCellCount = 0;
    private boolean firstRowFinalized = false;

    TableBuilder(WordDocumentBuilder parent, Tbl table, TableStyle style) {
        this.parent = parent;
        this.table = table;
        applyStyle(style);
    }

    private void applyStyle(TableStyle style) {
        TblPr tblPr = new TblPr();

        TblWidth width = new TblWidth();
        String widthPct = style.getWidthPercent().replace("%", "");
        // OOXML pct type: value = fiftieths of a percent (100% = 5000).
        width.setW(BigInteger.valueOf((long) (Double.parseDouble(widthPct) * 50)));
        width.setType("pct");
        tblPr.setTblW(width);

        CTTblLayoutType layout = new CTTblLayoutType();
        layout.setType(STTblLayoutType.AUTOFIT);
        tblPr.setTblLayout(layout);

        Docx4jTableBorders.apply(tblPr, style.getOuter(), style.getInner());
        table.setTblPr(tblPr);
    }

    public RowBuilder beginRow() {
        if (!firstRowFinalized) {
            firstRowFinalized = firstRowCellCount > 0;
            firstRowCellCount = currentRowCellCount > firstRowCellCount
                    ? currentRowCellCount
                    : firstRowCellCount;
        }
        currentRowCellCount = 0;
        Tr row = WordDocumentBuilder.FACTORY.createTr();
        table.getContent().add(row);
        return new RowBuilder(this, row);
    }

    void recordCellForColumnCount() {
        currentRowCellCount++;
    }

    public WordDocumentBuilder endTable() {
        int columns = Math.max(firstRowCellCount, currentRowCellCount);
        TblGrid grid = new TblGrid();
        for (int i = 0; i < columns; i++) {
            grid.getGridCol().add(new TblGridCol());
        }
        table.setTblGrid(grid);
        return parent;
    }
}
