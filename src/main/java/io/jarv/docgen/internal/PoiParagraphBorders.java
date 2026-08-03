package io.jarv.docgen.internal;

import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.BorderSet;
import io.jarv.docgen.style.BorderStyle;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTBorder;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPBdr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPPr;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.STBorder;

import java.math.BigInteger;

public final class PoiParagraphBorders {

    private PoiParagraphBorders() {}

    public static void apply(XWPFParagraph paragraph, BorderSet borders) {
        if (borders == null) return;
        CTPPr ppr = paragraph.getCTP().isSetPPr()
                ? paragraph.getCTP().getPPr()
                : paragraph.getCTP().addNewPPr();
        CTPBdr bdr = ppr.isSetPBdr() ? ppr.getPBdr() : ppr.addNewPBdr();

        applySide(bdr.isSetTop() ? bdr.getTop() : bdr.addNewTop(), borders.getTop());
        applySide(bdr.isSetBottom() ? bdr.getBottom() : bdr.addNewBottom(), borders.getBottom());
        applySide(bdr.isSetLeft() ? bdr.getLeft() : bdr.addNewLeft(), borders.getLeft());
        applySide(bdr.isSetRight() ? bdr.getRight() : bdr.addNewRight(), borders.getRight());
    }

    private static void applySide(CTBorder ctBorder, Border border) {
        if (border == null || border.getStyle() == BorderStyle.NONE) {
            ctBorder.setVal(STBorder.NONE);
            return;
        }
        ctBorder.setVal(toStBorder(border.getStyle()));
        ctBorder.setSz(BigInteger.valueOf((long) (border.getWidthPoints() * 8)));
        ctBorder.setSpace(BigInteger.valueOf((long) border.getSpacingPoints()));
        ctBorder.setColor(HexColor.normalize(border.getColorHex()));
    }

    private static STBorder.Enum toStBorder(BorderStyle style) {
        return switch (style) {
            case NONE -> STBorder.NONE;
            case SINGLE -> STBorder.SINGLE;
            case DOUBLE -> STBorder.DOUBLE;
            case DOTTED -> STBorder.DOTTED;
            case DASHED -> STBorder.DASHED;
            case THICK -> STBorder.THICK;
        };
    }
}
