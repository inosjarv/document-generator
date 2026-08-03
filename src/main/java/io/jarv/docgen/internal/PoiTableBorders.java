package io.jarv.docgen.internal;

import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.BorderStyle;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTable.XWPFBorderType;

public final class PoiTableBorders {

    private PoiTableBorders() {}

    public static void apply(XWPFTable table, Border outer, Border inner) {
        applyOuter(table, outer);
        applyInner(table, inner);
    }

    private static void applyOuter(XWPFTable table, Border border) {
        if (border == null || border.getStyle() == BorderStyle.NONE) {
            table.setTopBorder(XWPFBorderType.NONE, 0, 0, "auto");
            table.setBottomBorder(XWPFBorderType.NONE, 0, 0, "auto");
            table.setLeftBorder(XWPFBorderType.NONE, 0, 0, "auto");
            table.setRightBorder(XWPFBorderType.NONE, 0, 0, "auto");
            return;
        }
        XWPFBorderType type = toBorderType(border.getStyle());
        int size = (int) (border.getWidthPoints() * 8);
        int space = (int) border.getSpacingPoints();
        String color = HexColor.normalize(border.getColorHex());
        table.setTopBorder(type, size, space, color);
        table.setBottomBorder(type, size, space, color);
        table.setLeftBorder(type, size, space, color);
        table.setRightBorder(type, size, space, color);
    }

    private static void applyInner(XWPFTable table, Border border) {
        if (border == null || border.getStyle() == BorderStyle.NONE) {
            table.setInsideHBorder(XWPFBorderType.NONE, 0, 0, "auto");
            table.setInsideVBorder(XWPFBorderType.NONE, 0, 0, "auto");
            return;
        }
        XWPFBorderType type = toBorderType(border.getStyle());
        int size = (int) (border.getWidthPoints() * 8);
        int space = (int) border.getSpacingPoints();
        String color = HexColor.normalize(border.getColorHex());
        table.setInsideHBorder(type, size, space, color);
        table.setInsideVBorder(type, size, space, color);
    }

    private static XWPFBorderType toBorderType(BorderStyle style) {
        return switch (style) {
            case NONE -> XWPFBorderType.NONE;
            case SINGLE -> XWPFBorderType.SINGLE;
            case DOUBLE -> XWPFBorderType.DOUBLE;
            case DOTTED -> XWPFBorderType.DOTTED;
            case DASHED -> XWPFBorderType.DASHED;
            case THICK -> XWPFBorderType.THICK;
        };
    }
}
