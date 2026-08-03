package io.jarv.docgen.examples;

import io.jarv.docgen.builder.WordDocumentBuilder;
import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.TextStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.FileOutputStream;

@Slf4j
public class DemoGenerator {

    public static void main(String[] args) {
        log.info("Starting standalone document generation...");

        DocumentTheme theme = DocumentTheme.corporate();

        TextStyle titleStyle = TextStyle.builder()
                .fontSize(24).bold(true).colorHex(theme.getPrimaryColor()).build();
        TextStyle dateStyle = TextStyle.builder()
                .fontSize(16).colorHex(theme.getSecondaryColor()).build();
        TextStyle headingStyle = TextStyle.builder()
                .fontSize(16).bold(true).colorHex(theme.getPrimaryColor()).build();
        TextStyle bodyStyle = TextStyle.builder().fontSize(12).build();
        TextStyle emphasisStyle = TextStyle.builder().fontSize(12).italic(true).build();

        ParagraphStyle doubleSpaced = ParagraphStyle.builder()
                .lineSpacingMultiplier(2.0).build();

        String outputPath = "Generated_Report_Test.docx";
        try (
                FileOutputStream fos = new FileOutputStream(outputPath);
                WordDocumentBuilder builder = new WordDocumentBuilder(theme)) {

            builder.beginHeader()
                    .addRightText("SYSTEM ARCHITECTURE REPORT", titleStyle)
                    .addRightText("August 2026", dateStyle)
                    .endHeader()

                    .addText("Executive Summary", headingStyle)
                    .addText(
                            "This document was generated purely via Apache POI using a fluent builder pattern. "
                                    + "The line spacing here is the default 1.15x as defined by the DocumentTheme.",
                            bodyStyle)
                    .beginParagraph(doubleSpaced)
                    .addRun("This paragraph is ", bodyStyle)
                    .addRun("double spaced", emphasisStyle)
                    .addRun(" and mixes two run styles cleanly.", bodyStyle)
                    .endParagraph();

            if (log.isDebugEnabled()) {
                log.debug("\n{}", builder.debugXmlDump());
            }

            builder.writeTo(fos);
            log.info("Document successfully generated at: {}", outputPath);

        } catch (Exception e) {
            log.error("Failed to generate document", e);
        }
    }
}
