package io.jarv.docgen.examples;

import io.jarv.docgen.builder.WordDocumentBuilder;
import io.jarv.docgen.style.Alignment;
import io.jarv.docgen.style.Border;
import io.jarv.docgen.style.BorderSet;
import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TableStyle;
import io.jarv.docgen.style.TextStyle;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Objects;

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

        // Title paragraph with 6pt of breathing room after it, so body sits close but not touching.
        ParagraphStyle titleBlock = ParagraphStyle.builder()
                .spaceAfter(6.0).build();

        // Paragraph wrapped in a box.
        ParagraphStyle callout = ParagraphStyle.builder()
                .border(BorderSet.all(Border.builder()
                        .widthPoints(1.0).colorHex(theme.getPrimaryColor()).spacingPoints(6).build()))
                .spaceBefore(6.0).spaceAfter(6.0)
                .build();

        byte[] spidermanBytes = loadSpiderman();

        String outputPath = "Generated_Report_Test-%s.docx".formatted(System.currentTimeMillis());
        try (
                FileOutputStream fos = new FileOutputStream(outputPath);
                WordDocumentBuilder builder = new WordDocumentBuilder(theme)) {

            builder.beginHeader()
                    .addRightText("SYSTEM ARCHITECTURE REPORT", titleStyle)
                    .addRightText("August 2026", dateStyle)
                    .endHeader()

                    .addText("Executive Summary", headingStyle, titleBlock)
                    .addText(
                            "This document was generated via docx4j using a fluent builder pattern. "
                                    + "The line spacing here is defined by the DocumentTheme.",
                            bodyStyle)
                    .beginParagraph(doubleSpaced)
                    .addRun("This paragraph is ", bodyStyle)
                    .addRun("double spaced", emphasisStyle)
                    .addRun(" and mixes two run styles cleanly.", bodyStyle)
                    .endParagraph()

                    .addText("This paragraph has a border box around it.", bodyStyle, callout)

                    .addText("Data Summary", headingStyle, titleBlock)
                    .beginTable(TableStyle.bordered())
                    .beginRow()
                    .addCell("Metric", headingStyle)
                    .addCell("Value", headingStyle)
                    .endRow()
                    .beginRow()
                    .addCell("Latency (p50)", bodyStyle)
                    .addCell("42 ms", bodyStyle)
                    .endRow()
                    .beginRow()
                    .addCell("Latency (p99)", bodyStyle)
                    .addCell("310 ms", bodyStyle)
                    .endRow()
                    .endTable()

                    .addImage(new ByteArrayInputStream(spidermanBytes),
                            PictureType.PNG,
                            ImageStyle.builder()
                                    .widthPx(200).heightPx(200)
                                    .alignment(Alignment.CENTER)
                                    .build());

            if (log.isDebugEnabled()) {
                log.debug("\n{}", builder.debugXmlDump());
            }

            builder.writeTo(fos);
            log.info("Document successfully generated at: {}", outputPath);

        } catch (Exception e) {
            log.error("Failed to generate document", e);
        }
    }

    private static byte[] loadSpiderman() {
        try (InputStream in = Objects.requireNonNull(
                DemoGenerator.class.getResourceAsStream("/spiderman.png"),
                "spiderman.png not found on the classpath under /spiderman.png")) {
            return in.readAllBytes();
        } catch (Exception e) {
            throw new RuntimeException("failed to load spiderman.png", e);
        }
    }
}
