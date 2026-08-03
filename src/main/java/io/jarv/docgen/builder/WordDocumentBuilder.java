package io.jarv.docgen.builder;

import io.jarv.docgen.internal.PoiUnits;
import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TableStyle;
import io.jarv.docgen.style.TextStyle;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ooxml.POIXMLDocumentPart;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFHeader;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.util.Objects;

@Slf4j
public class WordDocumentBuilder implements AutoCloseable {

    private final XWPFDocument document;
    private final DocumentTheme theme;

    public WordDocumentBuilder() {
        this(DocumentTheme.defaults());
    }

    public WordDocumentBuilder(DocumentTheme theme) {
        this.document = new XWPFDocument();
        this.theme = theme != null ? theme : DocumentTheme.defaults();
        applyThemeMargins();
    }

    private CTSectPr sectPr() {
        var body = document.getDocument().getBody();
        return body.isSetSectPr() ? body.getSectPr() : body.addNewSectPr();
    }

    private void applyThemeMargins() {
        CTSectPr sectPr = sectPr();
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();

        pageMar.setTop(BigInteger.valueOf(PoiUnits.inchesToTwips(theme.getMarginTop())));
        pageMar.setBottom(BigInteger.valueOf(PoiUnits.inchesToTwips(theme.getMarginBottom())));
        pageMar.setLeft(BigInteger.valueOf(PoiUnits.inchesToTwips(theme.getMarginLeft())));
        pageMar.setRight(BigInteger.valueOf(PoiUnits.inchesToTwips(theme.getMarginRight())));
    }

    /** Convenience: add a single-run paragraph with default paragraph styling. */
    public WordDocumentBuilder addText(String text, TextStyle style) {
        return addText(text, style, ParagraphStyle.defaults());
    }

    /** Convenience: add a single-run paragraph with custom paragraph styling (alignment, spacing, indent). */
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
        XWPFParagraph paragraph = document.createParagraph();
        return new ParagraphBuilder(this, paragraph, style, theme);
    }

    public TableBuilder beginTable(TableStyle style) {
        Objects.requireNonNull(style, "style");
        XWPFTable table = document.createTable();
        return new TableBuilder(this, table, style);
    }

    /** Convenience: add an image in its own paragraph, aligned per {@link ImageStyle#getAlignment()}. */
    public WordDocumentBuilder addImage(InputStream stream, PictureType type, ImageStyle imageStyle)
            throws IOException, InvalidFormatException {
        Objects.requireNonNull(imageStyle, "imageStyle");
        ParagraphStyle wrapper = ParagraphStyle.builder().alignment(imageStyle.getAlignment()).build();
        return beginParagraph(wrapper).addImage(stream, type, imageStyle).endParagraph();
    }

    public HeaderBuilder beginHeader() {
        CTSectPr sectPr = sectPr();
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();
        pageMar.setHeader(BigInteger.valueOf(PoiUnits.inchesToTwips(theme.getMarginHeader())));

        XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
        return new HeaderBuilder(this, header, theme);
    }

    public void writeTo(OutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream");
        document.write(outputStream);
    }

    public byte[] buildAsBytes() throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.write(out);
            return out.toByteArray();
        }
    }

    public String debugXmlDump() {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);

        StringBuilder dump = new StringBuilder();
        dump.append("========== MAIN DOCUMENT (document.xml) ==========\n");
        dump.append(document.getDocument().xmlText(options)).append("\n\n");

        dump.append("========== HEADERS ==========\n");
        int headerIndex = 0;
        for (POIXMLDocumentPart part : document.getRelations()) {
            if (part instanceof XWPFHeader header) {
                headerIndex++;
                dump.append("--- Header ").append(headerIndex).append(" ---\n");
                dump.append(header._getHdrFtr().xmlText(options)).append("\n\n");
            }
        }
        return dump.toString();
    }

    @Override
    public void close() throws IOException {
        document.close();
    }
}
