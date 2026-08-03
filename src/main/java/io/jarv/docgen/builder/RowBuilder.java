package io.jarv.docgen.builder;

import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;

import java.util.Objects;

public class RowBuilder {

    private final TableBuilder parent;
    private final XWPFTableRow row;
    private int nextCellIndex = 0;

    RowBuilder(TableBuilder parent, XWPFTableRow row) {
        this.parent = parent;
        this.row = row;
    }

    /**
     * Add a cell with a single-run paragraph. Reuses cells POI created implicitly (each new row
     * inherits the column count of the first row) before creating additional ones.
     */
    public RowBuilder addCell(String text, TextStyle style) {
        Objects.requireNonNull(style, "style");
        XWPFTableCell cell = (nextCellIndex < row.getTableCells().size())
                ? row.getCell(nextCellIndex)
                : row.createCell();

        XWPFParagraph paragraph = cell.getParagraphs().get(0);
        XWPFRun run = paragraph.createRun();
        run.setText(text != null ? text : "");
        ParagraphBuilder.applyTextStyle(run, style);
        nextCellIndex++;
        return this;
    }

    public TableBuilder endRow() {
        return parent;
    }
}
