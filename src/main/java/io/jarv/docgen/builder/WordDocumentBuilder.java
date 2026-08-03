package io.jarv.docgen.builder;

import io.jarv.docgen.internal.DocxUnits;
import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TableStyle;
import io.jarv.docgen.style.TextStyle;
import lombok.extern.slf4j.Slf4j;
import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.exceptions.InvalidFormatException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Body;
import org.docx4j.wml.HdrFtrRef;
import org.docx4j.wml.HeaderReference;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.SectPr;
import org.docx4j.wml.Tbl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Objects;

@Slf4j
public class WordDocumentBuilder implements AutoCloseable {

    static final ObjectFactory FACTORY = new ObjectFactory();

    private final WordprocessingMLPackage pkg;
    private final MainDocumentPart mainPart;
    private final DocumentTheme theme;
    private int headerCount = 0;

    public WordDocumentBuilder() {
        this(DocumentTheme.defaults());
    }

    public WordDocumentBuilder(DocumentTheme theme) {
        try {
            this.pkg = WordprocessingMLPackage.createPackage();
        } catch (InvalidFormatException e) {
            throw new IllegalStateException("failed to create docx package", e);
        }
        this.mainPart = pkg.getMainDocumentPart();
        this.theme = theme != null ? theme : DocumentTheme.defaults();
        applyThemeMargins();
    }

    WordprocessingMLPackage pkg() { return pkg; }
    MainDocumentPart mainPart() { return mainPart; }
    DocumentTheme theme() { return theme; }

    private SectPr sectPr() {
        try {
            Body body = mainPart.getContents().getBody();
            SectPr sectPr = body.getSectPr();
            if (sectPr == null) {
                sectPr = FACTORY.createSectPr();
                body.setSectPr(sectPr);
            }
            return sectPr;
        } catch (Docx4JException e) {
            throw new IllegalStateException("failed to access section properties", e);
        }
    }

    private void applyThemeMargins() {
        SectPr sectPr = sectPr();
        SectPr.PgMar pgMar = sectPr.getPgMar();
        if (pgMar == null) {
            pgMar = FACTORY.createSectPrPgMar();
            sectPr.setPgMar(pgMar);
        }
        pgMar.setTop(BigInteger.valueOf(DocxUnits.inchesToTwips(theme.getMarginTop())));
        pgMar.setBottom(BigInteger.valueOf(DocxUnits.inchesToTwips(theme.getMarginBottom())));
        pgMar.setLeft(BigInteger.valueOf(DocxUnits.inchesToTwips(theme.getMarginLeft())));
        pgMar.setRight(BigInteger.valueOf(DocxUnits.inchesToTwips(theme.getMarginRight())));
    }

    /** Convenience: add a single-run paragraph with default paragraph styling. */
    public WordDocumentBuilder addText(String text, TextStyle style) {
        return addText(text, style, ParagraphStyle.defaults());
    }

    /** Convenience: add a single-run paragraph with custom paragraph styling. */
    public WordDocumentBuilder addText(String text, TextStyle style, ParagraphStyle paragraphStyle) {
        return beginParagraph(paragraphStyle)
                .addRun(text, style)
                .endParagraph();
    }

    public ParagraphBuilder beginParagraph() {
        return beginParagraph(ParagraphStyle.defaults());
    }

    public ParagraphBuilder beginParagraph(ParagraphStyle style) {
        Objects.requireNonNull(style, "style");
        P paragraph = FACTORY.createP();
        mainPart.getContent().add(paragraph);
        return new ParagraphBuilder(this, paragraph, style, theme);
    }

    public TableBuilder beginTable(TableStyle style) {
        Objects.requireNonNull(style, "style");
        Tbl table = FACTORY.createTbl();
        mainPart.getContent().add(table);
        return new TableBuilder(this, table, style);
    }

    /** Convenience: add an image in its own paragraph, aligned per {@link ImageStyle#getAlignment()}. */
    public WordDocumentBuilder addImage(InputStream stream, PictureType type, ImageStyle imageStyle) throws IOException {
        Objects.requireNonNull(imageStyle, "imageStyle");
        ParagraphStyle wrapper = ParagraphStyle.builder().alignment(imageStyle.getAlignment()).build();
        return beginParagraph(wrapper).addImage(stream, type, imageStyle).endParagraph();
    }

    public HeaderBuilder beginHeader() {
        try {
            headerCount++;
            HeaderPart headerPart = new HeaderPart(new PartName("/word/header" + headerCount + ".xml"));
            headerPart.setJaxbElement(FACTORY.createHdr());
            Relationship rel = mainPart.addTargetPart(headerPart);

            HeaderReference reference = FACTORY.createHeaderReference();
            reference.setId(rel.getId());
            reference.setType(HdrFtrRef.DEFAULT);

            SectPr sectPr = sectPr();
            sectPr.getEGHdrFtrReferences().add(reference);

            SectPr.PgMar pgMar = sectPr.getPgMar();
            pgMar.setHeader(BigInteger.valueOf(DocxUnits.inchesToTwips(theme.getMarginHeader())));

            return new HeaderBuilder(this, headerPart, theme);
        } catch (InvalidFormatException e) {
            throw new IllegalStateException("failed to create header part", e);
        }
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream");
        try {
            pkg.save(outputStream);
        } catch (Docx4JException e) {
            throw new IOException("failed to write document", e);
        }
    }

    public byte[] buildAsBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            writeTo(out);
            return out.toByteArray();
        }
    }

    public String debugXmlDump() {
        try {
            StringBuilder dump = new StringBuilder();
            dump.append("========== MAIN DOCUMENT (document.xml) ==========\n");
            dump.append(XmlUtils.marshaltoString(mainPart.getContents(), true, true)).append("\n\n");

            dump.append("========== HEADERS ==========\n");
            int idx = 0;
            for (Relationship rel : mainPart.getRelationshipsPart().getJaxbElement().getRelationship()) {
                Part part = mainPart.getRelationshipsPart().getPart(rel);
                if (part instanceof HeaderPart hp) {
                    idx++;
                    dump.append("--- Header ").append(idx).append(" ---\n");
                    dump.append(XmlUtils.marshaltoString(hp.getContents(), true, true)).append("\n\n");
                }
            }
            return dump.toString();
        } catch (Docx4JException e) {
            throw new IllegalStateException("failed to marshal debug xml", e);
        }
    }

    @Override
    public void close() throws IOException {
        // docx4j has no explicit close on the package; the OPC parts are GC-managed.
    }
}
