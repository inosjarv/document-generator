package io.jarv.docgen.builder;

import io.jarv.docgen.internal.PoiTableBorders;
import io.jarv.docgen.style.TableStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

public class TableBuilder {

    private final WordDocumentBuilder parent;
    private final XWPFTable table;
    private boolean firstRowClaimed = false;

    TableBuilder(WordDocumentBuilder parent, XWPFTable table, TableStyle style) {
        this.parent = parent;
        this.table = table;
        applyStyle(style);
    }

    private void applyStyle(TableStyle style) {
        table.setWidth(style.getWidthPercent());
        PoiTableBorders.apply(table, style.getOuter(), style.getInner());
    }

    /**
     * Start a new row. POI creates a default 1x1 row when the table is constructed — the first
     * {@code beginRow} reuses it; subsequent calls create fresh rows (which inherit the column
     * count of row 0).
     */
    public RowBuilder beginRow() {
        XWPFTableRow row;
        if (!firstRowClaimed) {
            row = table.getRow(0);
            firstRowClaimed = true;
        } else {
            row = table.createRow();
        }
        return new RowBuilder(this, row);
    }

    public WordDocumentBuilder endTable() {
        return parent;
    }
}
