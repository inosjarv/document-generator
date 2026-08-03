package io.jarv.docgen.internal;

import io.jarv.docgen.style.Border;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblPr;

public final class Docx4jTableBorders {

    private Docx4jTableBorders() {}

    public static void apply(TblPr tblPr, Border outer, Border inner) {
        TblBorders borders = new TblBorders();
        borders.setTop(Docx4jBorderMapping.toCtBorder(outer));
        borders.setBottom(Docx4jBorderMapping.toCtBorder(outer));
        borders.setLeft(Docx4jBorderMapping.toCtBorder(outer));
        borders.setRight(Docx4jBorderMapping.toCtBorder(outer));
        borders.setInsideH(Docx4jBorderMapping.toCtBorder(inner));
        borders.setInsideV(Docx4jBorderMapping.toCtBorder(inner));
        tblPr.setTblBorders(borders);
    }
}
