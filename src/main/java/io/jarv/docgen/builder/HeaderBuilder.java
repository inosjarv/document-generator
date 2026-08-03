package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TextStyle;
import org.docx4j.dml.wordprocessingDrawing.Inline;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.parts.WordprocessingML.BinaryPartAbstractImage;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.wml.Br;
import org.docx4j.wml.CTBorder;
import org.docx4j.wml.CTTblLayoutType;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.Jc;
import org.docx4j.wml.JcEnumeration;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.R;
import org.docx4j.wml.STBorder;
import org.docx4j.wml.STTblLayoutType;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.TblBorders;
import org.docx4j.wml.TblGrid;
import org.docx4j.wml.TblGridCol;
import org.docx4j.wml.TblPr;
import org.docx4j.wml.TblWidth;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Objects;

public class HeaderBuilder {

    private final WordDocumentBuilder parent;
    private final HeaderPart headerPart;
    private final P leftParagraph;
    private final P rightParagraph;
    private boolean rightHasContent = false;

    HeaderBuilder(WordDocumentBuilder parent, HeaderPart headerPart, DocumentTheme theme) {
        this.parent = parent;
        this.headerPart = headerPart;

        Hdr hdr = (Hdr) headerPart.getJaxbElement();

        Tbl table = WordDocumentBuilder.FACTORY.createTbl();
        table.setTblPr(borderlessTablePr());
        table.setTblGrid(twoColumnGrid());

        Tr row = WordDocumentBuilder.FACTORY.createTr();

        Tc leftCell = WordDocumentBuilder.FACTORY.createTc();
        this.leftParagraph = alignedParagraph(JcEnumeration.LEFT);
        leftCell.getContent().add(leftParagraph);
        row.getContent().add(leftCell);

        Tc rightCell = WordDocumentBuilder.FACTORY.createTc();
        this.rightParagraph = alignedParagraph(JcEnumeration.RIGHT);
        rightCell.getContent().add(rightParagraph);
        row.getContent().add(rightCell);

        table.getContent().add(row);
        hdr.getContent().add(table);
    }

    private static TblPr borderlessTablePr() {
        TblPr tblPr = new TblPr();
        TblWidth width = new TblWidth();
        width.setW(BigInteger.valueOf(5000));
        width.setType("pct");
        tblPr.setTblW(width);

        CTTblLayoutType layout = new CTTblLayoutType();
        layout.setType(STTblLayoutType.AUTOFIT);
        tblPr.setTblLayout(layout);

        TblBorders borders = new TblBorders();
        borders.setTop(noneBorder());
        borders.setBottom(noneBorder());
        borders.setLeft(noneBorder());
        borders.setRight(noneBorder());
        borders.setInsideH(noneBorder());
        borders.setInsideV(noneBorder());
        tblPr.setTblBorders(borders);
        return tblPr;
    }

    private static CTBorder noneBorder() {
        CTBorder b = new CTBorder();
        b.setVal(STBorder.NONE);
        b.setColor("auto");
        b.setSz(BigInteger.ZERO);
        b.setSpace(BigInteger.ZERO);
        return b;
    }

    private static TblGrid twoColumnGrid() {
        TblGrid grid = new TblGrid();
        grid.getGridCol().add(new TblGridCol());
        grid.getGridCol().add(new TblGridCol());
        return grid;
    }

    private static P alignedParagraph(JcEnumeration alignment) {
        P p = WordDocumentBuilder.FACTORY.createP();
        PPr ppr = WordDocumentBuilder.FACTORY.createPPr();
        Jc jc = new Jc();
        jc.setVal(alignment);
        ppr.setJc(jc);
        p.setPPr(ppr);
        return p;
    }

    public HeaderBuilder addLeftImage(InputStream imageStream, PictureType type, ImageStyle imageStyle) throws IOException {
        Objects.requireNonNull(imageStream, "imageStream");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(imageStyle, "imageStyle");
        try {
            byte[] bytes = imageStream.readAllBytes();
            BinaryPartAbstractImage imagePart = BinaryPartAbstractImage.createImagePart(parent.pkg(), headerPart, bytes);
            long cx = io.jarv.docgen.internal.DocxUnits.pixelsToEmu(imageStyle.getWidthPx());
            long cy = io.jarv.docgen.internal.DocxUnits.pixelsToEmu(imageStyle.getHeightPx());
            Inline inline = imagePart.createImageInline(
                    "image." + type.extension(), "image", 1, 2, cx, cy, false);

            R run = WordDocumentBuilder.FACTORY.createR();
            Drawing drawing = WordDocumentBuilder.FACTORY.createDrawing();
            drawing.getAnchorOrInline().add(inline);
            run.getContent().add(drawing);
            leftParagraph.getContent().add(run);
            return this;
        } catch (IOException e) {
            throw e;
        } catch (Exception e) {
            throw new IOException("failed to add header image", e);
        }
    }

    public HeaderBuilder addRightText(String text, TextStyle style) {
        Objects.requireNonNull(style, "style");
        if (rightHasContent) {
            R breakRun = WordDocumentBuilder.FACTORY.createR();
            breakRun.getContent().add(new Br());
            rightParagraph.getContent().add(breakRun);
        }

        R run = WordDocumentBuilder.FACTORY.createR();
        ParagraphBuilder.applyTextStyle(run, style);
        Text t = WordDocumentBuilder.FACTORY.createText();
        t.setValue(text != null ? text : "");
        t.setSpace("preserve");
        run.getContent().add(WordDocumentBuilder.FACTORY.createRT(t));
        rightParagraph.getContent().add(run);

        rightHasContent = true;
        return this;
    }

    public WordDocumentBuilder endHeader() {
        return parent;
    }
}
