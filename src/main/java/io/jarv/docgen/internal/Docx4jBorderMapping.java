package io.jarv.docgen.internal;

import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.BorderStyle;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.STBorder;

import java.math.BigInteger;

/** Shared conversion helpers between our {@link Border} value objects and docx4j's {@link CTBorder}. */
public final class Docx4jBorderMapping {

    private Docx4jBorderMapping() {}

    /** Build a docx4j CTBorder from our style. If {@code border} is null, returns an explicit NONE border. */
    public static CTBorder toCtBorder(Border border) {
        CTBorder ct = new CTBorder();
        if (border == null || border.getStyle() == BorderStyle.NONE) {
            ct.setVal(STBorder.NONE);
            ct.setColor("auto");
            ct.setSz(BigInteger.ZERO);
            ct.setSpace(BigInteger.ZERO);
            return ct;
        }
        ct.setVal(toStBorder(border.getStyle()));
        ct.setSz(BigInteger.valueOf((long) (border.getWidthPoints() * 8)));
        ct.setSpace(BigInteger.valueOf((long) border.getSpacingPoints()));
        ct.setColor(HexColor.normalize(border.getColorHex()));
        return ct;
    }

    private static STBorder toStBorder(BorderStyle style) {
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
