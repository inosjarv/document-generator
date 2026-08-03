package io.jarv.docgen.builder;

import io.jarv.docgen.internal.Docx4jParagraphBorders;
import io.jarv.docgen.internal.DocxUnits;
import io.jarv.docgen.internal.HexColor;
import io.jarv.docgen.style.Alignment;
import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TextStyle;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.wml.BooleanDefaultTrue;
import org.docx4j.wml.Br;
import org.docx4j.wml.Color;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.HpsMeasure;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.PPrBase;
import org.docx4j.wml.R;
import org.docx4j.wml.RFonts;
import org.docx4j.wml.RPr;
import org.docx4j.wml.STLineSpacingRule;
import org.docx4j.wml.Text;
import org.docx4j.wml.U;
import org.docx4j.wml.UnderlineEnumeration;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Objects;

public class ParagraphBuilder {

    private final WordDocumentBuilder parent;
    private final P paragraph;
    private final DocumentTheme theme;

    ParagraphBuilder(WordDocumentBuilder parent, P paragraph, ParagraphStyle style, DocumentTheme theme) {
        this.parent = parent;
        this.paragraph = paragraph;
        this.theme = theme;
        applyStyle(style);
    }

    private void applyStyle(ParagraphStyle style) {
        PPr ppr = paragraph.getPPr() != null ? paragraph.getPPr() : WordDocumentBuilder.FACTORY.createPPr();

        Jc jc = new Jc();
        jc.setVal(toJc(style.getAlignment()));
        ppr.setJc(jc);

        PPrBase.Spacing spacing = new PPrBase.Spacing();
        double lineMultiplier = style.getLineSpacingMultiplier() != null
                ? style.getLineSpacingMultiplier()
                : theme.getDefaultLineSpacing();
        spacing.setLine(BigInteger.valueOf((long) (lineMultiplier * 240)));
        spacing.setLineRule(STLineSpacingRule.AUTO);
        if (style.getSpaceBefore() > 0) {
            spacing.setBefore(BigInteger.valueOf(DocxUnits.pointsToTwentieths(style.getSpaceBefore())));
        }
        if (style.getSpaceAfter() > 0) {
            spacing.setAfter(BigInteger.valueOf(DocxUnits.pointsToTwentieths(style.getSpaceAfter())));
        }
        ppr.setSpacing(spacing);

        if (style.getIndentLeft() > 0) {
            PPrBase.Ind ind = new PPrBase.Ind();
            ind.setLeft(BigInteger.valueOf(DocxUnits.inchesToTwips(style.getIndentLeft())));
            ppr.setInd(ind);
        }

        if (style.getBorder() != null) {
            Docx4jParagraphBorders.apply(ppr, style.getBorder());
        }

        paragraph.setPPr(ppr);
    }

    public ParagraphBuilder addRun(String text, TextStyle style) {
        Objects.requireNonNull(style, "style");
        R run = WordDocumentBuilder.FACTORY.createR();
        applyTextStyle(run, style);
        Text t = WordDocumentBuilder.FACTORY.createText();
        t.setValue(text != null ? text : "");
        t.setSpace("preserve");
        run.getContent().add(WordDocumentBuilder.FACTORY.createRT(t));
        paragraph.getContent().add(run);
        return this;
    }

    public ParagraphBuilder addLineBreak() {
        R run = WordDocumentBuilder.FACTORY.createR();
        Br br = new Br();
        run.getContent().add(br);
        paragraph.getContent().add(run);
        return this;
    }

    public ParagraphBuilder addImage(InputStream stream, PictureType type, ImageStyle imageStyle) throws IOException {
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(imageStyle, "imageStyle");
        try {
            byte[] bytes = stream.readAllBytes();
            BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(parent.pkg(), bytes);
            long cx = DocxUnits.pixelsToEmu(imageStyle.getWidthPx());
            long cy = DocxUnits.pixelsToEmu(imageStyle.getHeightPx());
            Inline inline = imagePart.createImageInline(
                    "image." + type.extension(), "image", 1, 2, cx, cy, false);

            R run = WordDocumentBuilder.FACTORY.createR();
            Drawing drawing = WordDocumentBuilder.FACTORY.createDrawing();
            drawing.getAnchorOrInline().add(inline);
            run.getContent().add(drawing);
            paragraph.getContent().add(run);
            return this;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("failed to add image", e);
        }
    }

    public WordDocumentBuilder endParagraph() {
        return parent;
    }

    /** Package-private: shared with RowBuilder and HeaderBuilder for consistent run styling. */
    static void applyTextStyle(R run, TextStyle style) {
        RPr rpr = new RPr();

        RFonts fonts = new RFonts();
        fonts.setAscii(style.getFontFamily());
        fonts.setHAnsi(style.getFontFamily());
        fonts.setCs(style.getFontFamily());
        fonts.setEastAsia(style.getFontFamily());
        rpr.setRFonts(fonts);

        HpsMeasure size = new HpsMeasure();
        size.setVal(BigInteger.valueOf(style.getFontSize() * 2L));
        rpr.setSz(size);
        rpr.setSzCs(size);

        if (style.isBold()) {
            BooleanDefaultTrue b = new BooleanDefaultTrue();
            b.setVal(true);
            rpr.setB(b);
        }
        if (style.isItalic()) {
            BooleanDefaultTrue i = new BooleanDefaultTrue();
            i.setVal(true);
            rpr.setI(i);
        }
        if (style.isUnderline()) {
            U u = new U();
            u.setVal(UnderlineEnumeration.SINGLE);
            rpr.setU(u);
        }

        Color color = new Color();
        color.setVal(HexColor.normalize(style.getColorHex()));
        rpr.setColor(color);

        run.setRPr(rpr);
    }

    static JcEnumeration toJc(Alignment alignment) {
        return switch (alignment) {
            case LEFT -> JcEnumeration.LEFT;
            case CENTER -> JcEnumeration.CENTER;
            case RIGHT -> JcEnumeration.RIGHT;
            case JUSTIFY -> JcEnumeration.BOTH;
        };
    }
}
