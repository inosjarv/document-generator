package io.jarv;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.wp.usermodel.HeaderFooterType;
import org.apache.poi.xwpf.usermodel.*;
import org.apache.poi.util.Units;
import org.apache.xmlbeans.XmlOptions;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTPageMar;
import org.openxmlformats.schemas.wordprocessingml.x2006.main.CTSectPr;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;

@Slf4j
public class DocumentGenerator {

    public static void main(String[] args) {
        log.info("Starting standalone document generation...");

        // 1. Initialize Theme
        DocumentTheme theme = DocumentTheme.builder()
                .primaryColor("2C3E50") // Dark Blue/Gray
                .secondaryColor("7F8C8D") // Lighter Gray
                .build();

        // 2. Initialize Styles
        TextStyle titleStyle = TextStyle.builder().fontSize(24).bold(true).colorHex(theme.getPrimaryColor()).build();
        TextStyle dateStyle = TextStyle.builder().fontSize(16).bold(false).colorHex(theme.getSecondaryColor()).build();
        TextStyle bodyStyle = TextStyle.builder().fontSize(12).build();
        TextStyle bespokeStyle = TextStyle.builder().fontSize(12).lineSpacingMultiplier(2.0).build(); // Double spaced

        // 3. Generate the file to disk
        String outputPath = "Generated_Report_Test.docx";
        try (
                FileOutputStream fos = new FileOutputStream(outputPath);
                WordDocumentBuilder builder = new WordDocumentBuilder(theme)) {
            builder.beginHeader()
                    // .addLeftImage(logoStream, 150, 50) // Commented out for standalone testing to
                    // avoid missing file errors
                    .addRightText("SYSTEM ARCHITECTURE REPORT", titleStyle)
                    .addRightText("August 2026", dateStyle)
                    .endHeader()

                    .addText("Executive Summary",
                            TextStyle.builder().fontSize(16).bold(true).colorHex(theme.getPrimaryColor()).build())
                    .addText("This document was generated purely via Apache POI using a fluent builder pattern. " +
                            "The line spacing here is the default 1.15x as defined by the DocumentTheme.", bodyStyle)
                    .addText(
                            "This specific paragraph is using a bespoke TextStyle with a 2.0x line spacing multiplier. "
                                    +
                                    "Notice how it isolates the styling changes cleanly without affecting the rest of the document layout.",
                            bespokeStyle);

            // Log the XML for debugging
            builder.getDebugXmlDump();

            // Stream to file
            builder.writeTo(fos);
            log.info("Document successfully generated at: {}", outputPath);

        } catch (Exception e) {
            log.error("Failed to generate document", e);
        }
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
class DocumentTheme {
    @Builder.Default
    private double marginTop = 1.5;
    @Builder.Default
    private double marginBottom = 1.0;
    @Builder.Default
    private double marginLeft = 1.0;
    @Builder.Default
    private double marginRight = 1.0;

    @Builder.Default
    private double marginHeader = 0.5;

    @Builder.Default
    private String primaryColor = "333333";
    @Builder.Default
    private String secondaryColor = "666666";

    @Builder.Default
    private double defaultLineSpacing = 1.15;
}

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
class TextStyle {
    @Builder.Default
    private boolean bold = false;
    @Builder.Default
    private boolean italic = false;
    @Builder.Default
    private int fontSize = 11;
    @Builder.Default
    private String colorHex = "000000";

    private Double lineSpacingMultiplier;
}

@Slf4j
class WordDocumentBuilder implements AutoCloseable {
    private final XWPFDocument document;
    private final DocumentTheme theme;

    public WordDocumentBuilder(DocumentTheme theme) {
        this.document = new XWPFDocument();
        this.theme = theme != null ? theme : new DocumentTheme();
        applyThemeMargins();
    }

    private void applyThemeMargins() {
        CTSectPr sectPr = document.getDocument().getBody().isSetSectPr() ? document.getDocument().getBody().getSectPr()
                : document.getDocument().getBody().addNewSectPr();
        CTPageMar pageMar = sectPr.isSetPgMar() ? sectPr.getPgMar() : sectPr.addNewPgMar();

        pageMar.setTop(BigInteger.valueOf((long) (theme.getMarginTop() * 1440)));
        pageMar.setBottom(BigInteger.valueOf((long) (theme.getMarginBottom() * 1440)));
        pageMar.setLeft(BigInteger.valueOf((long) (theme.getMarginLeft() * 1440)));
        pageMar.setRight(BigInteger.valueOf((long) (theme.getMarginRight() * 1440)));
    }

    public WordDocumentBuilder addText(String text, TextStyle style) {
        XWPFParagraph paragraph = document.createParagraph();

        double spacing = style.getLineSpacingMultiplier() != null ? style.getLineSpacingMultiplier()
                : theme.getDefaultLineSpacing();
        paragraph.setSpacingBetween(spacing, LineSpacingRule.AUTO);

        XWPFRun run = paragraph.createRun();
        run.setText(text);
        run.setBold(style.isBold());
        run.setItalic(style.isItalic());
        run.setFontSize(style.getFontSize());
        run.setColor(style.getColorHex());

        return this;
    }

    public HeaderBuilder beginHeader() {
        CTSectPr sectPr = document.getDocument().getBody().getSectPr();
        CTPageMar pageMar = sectPr.getPgMar();
        pageMar.setHeader(BigInteger.valueOf((long) (theme.getMarginHeader() * 1440)));

        XWPFHeader header = document.createHeader(HeaderFooterType.DEFAULT);
        return new HeaderBuilder(this, header, theme);
    }

    // --- Stream Management ---
    public void writeTo(OutputStream outputStream) throws Exception {
        document.write(outputStream);
    }

    public byte[] buildAsBytes() throws Exception {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            document.write(out);
            return out.toByteArray();
        }
    }

    // --- Debugging ---
    public String getDebugXmlDump() {
        XmlOptions options = new XmlOptions();
        options.setSavePrettyPrint();
        options.setSavePrettyPrintIndent(4);

        StringBuilder dump = new StringBuilder();
        dump.append("========== MAIN DOCUMENT (document.xml) ==========\n");
        dump.append(document.getDocument().xmlText(options)).append("\n\n");

        dump.append("========== HEADERS ==========\n");
        for (int i = 0; i < document.getHeaderList().size(); i++) {
            XWPFHeader header = document.getHeaderList().get(i);
            dump.append("--- Header ").append(i + 1).append(" ---\n");
            dump.append(header._getHdrFtr().xmlText(options)).append("\n\n");
        }

        String finalDump = dump.toString();
        log.debug("\n{}", finalDump);
        return finalDump;
    }

    @Override
    public void close() throws Exception {
        if (this.document != null) {
            this.document.close();
        }
    }

    // ==========================================
    // NESTED HEADER BUILDER
    // ==========================================
    public class HeaderBuilder {
        private final WordDocumentBuilder parentBuilder;
        private final XWPFParagraph leftParagraph;
        private final XWPFParagraph rightParagraph;
        private final DocumentTheme theme;

        public HeaderBuilder(WordDocumentBuilder parentBuilder, XWPFHeader header, DocumentTheme theme) {
            this.parentBuilder = parentBuilder;
            this.theme = theme;

            XWPFTable table = header.createTable(1, 2);
            table.setWidth("100%");

            table.setTopBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setBottomBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setLeftBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setRightBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setInsideHBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");
            table.setInsideVBorder(XWPFTable.XWPFBorderType.NONE, 0, 0, "auto");

            this.leftParagraph = table.getRow(0).getCell(0).getParagraphs().get(0);
            this.leftParagraph.setAlignment(ParagraphAlignment.LEFT);

            this.rightParagraph = table.getRow(0).getCell(1).getParagraphs().get(0);
            this.rightParagraph.setAlignment(ParagraphAlignment.RIGHT);
        }

        public HeaderBuilder addLeftImage(InputStream imageStream, int widthPx, int heightPx) throws Exception {
            XWPFRun run = leftParagraph.createRun();
            run.addPicture(imageStream, XWPFDocument.PICTURE_TYPE_PNG, "logo.png", Units.toEMU(widthPx),
                    Units.toEMU(heightPx));
            return this;
        }

        public HeaderBuilder addRightText(String text, TextStyle style) {
            double spacing = style.getLineSpacingMultiplier() != null ? style.getLineSpacingMultiplier()
                    : theme.getDefaultLineSpacing();
            rightParagraph.setSpacingBetween(spacing, LineSpacingRule.AUTO);

            XWPFRun run = rightParagraph.createRun();
            run.setText(text);
            run.setFontSize(style.getFontSize());
            run.setBold(style.isBold());
            run.setColor(style.getColorHex());
            run.addBreak();
            return this;
        }

        public WordDocumentBuilder endHeader() {
            return parentBuilder;
        }
    }
}
