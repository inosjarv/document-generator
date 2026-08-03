package io.jarv.docgen.internal;

import io.jarv.docgen.style.BorderSet;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;

public final class Docx4jParagraphBorders {

    private Docx4jParagraphBorders() {}

    public static void apply(PPr ppr, BorderSet borders) {
        if (borders == null) return;
        PPrBase.PBdr pBdr = new PPrBase.PBdr();
        pBdr.setTop(Docx4jBorderMapping.toCtBorder(borders.getTop()));
        pBdr.setBottom(Docx4jBorderMapping.toCtBorder(borders.getBottom()));
        pBdr.setLeft(Docx4jBorderMapping.toCtBorder(borders.getLeft()));
        pBdr.setRight(Docx4jBorderMapping.toCtBorder(borders.getRight()));
        ppr.setPBdr(pBdr);
    }
}
