package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class HeaderBuilder {

    private final WordDocumentBuilder parent;
    private final XWPFParagraph leftParagraph;
    private final XWPFParagraph rightParagraph;
    private boolean rightHasContent = false;

    HeaderBuilder(WordDocumentBuilder parent, XWPFHeader header, DocumentTheme theme) {
        this.parent = parent;

        XWPFTable table = header.createTable(1, 2);
        table.setWidth("100%");
        clearBorders(table);

        this.leftParagraph = table.getRow(0).getCell(0).getParagraphs().get(0);
        this.leftParagraph.setAlignment(ParagraphAlignment.LEFT);

        this.rightParagraph = table.getRow(0).getCell(1).getParagraphs().get(0);
        this.rightParagraph.setAlignment(ParagraphAlignment.RIGHT);
    }

    private static void clearBorders(XWPFTable table) {
        table.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
        table.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
        table.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
        table.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
        table.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
        table.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
    }

    public HeaderBuilder addLeftImage(InputStream imageStream, PictureType type, int widthPx, int heightPx)
            throws IOException, InvalidFormatException {
        Objects.requireNonNull(imageStream, "imageStream");
        Objects.requireNonNull(type, "type");
        XWPFRun run = leftParagraph.createRun();
        run.addPicture(imageStream, type.poiType(), "image." + type.extension(),
                Units.toEMU(widthPx), Units.toEMU(heightPx));
        return this;
    }

    public HeaderBuilder addRightText(String text, TextStyle style) {
        Objects.requireNonNull(style, "style");
        if (rightHasContent) {
            rightParagraph.createRun().addBreak();
        }
        XWPFRun run = rightParagraph.createRun();
        run.setText(text != null ? text : "");
        ParagraphBuilder.applyTextStyle(run, style);
        rightHasContent = true;
        return this;
    }

    public WordDocumentBuilder endHeader() {
        return parent;
    }
}
