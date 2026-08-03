package io.jarv.docgen.builder;

import io.jarv.docgen.internal.HexColor;
import io.jarv.docgen.internal.PoiParagraphBorders;
import io.jarv.docgen.internal.PoiUnits;
import io.jarv.docgen.style.Alignment;
import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.ParagraphStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.LineSpacingRule;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class ParagraphBuilder {

    private final WordDocumentBuilder parent;
    private final XWPFParagraph paragraph;
    private final DocumentTheme theme;

    ParagraphBuilder(WordDocumentBuilder parent, XWPFParagraph paragraph, ParagraphStyle style, DocumentTheme theme) {
        this.parent = parent;
        this.paragraph = paragraph;
        this.theme = theme;
        applyStyle(style);
    }

    private void applyStyle(ParagraphStyle style) {
        paragraph.setAlignment(toPoi(style.getAlignment()));

        double spacing = style.getLineSpacingMultiplier() != null
                ? style.getLineSpacingMultiplier()
                : theme.getDefaultLineSpacing();
        paragraph.setSpacingBetween(spacing, LineSpacingRule.AUTO);

        if (style.getSpaceBefore() > 0) {
            paragraph.setSpacingBefore(PoiUnits.pointsToTwentieths(style.getSpaceBefore()));
        }
        if (style.getSpaceAfter() > 0) {
            paragraph.setSpacingAfter(PoiUnits.pointsToTwentieths(style.getSpaceAfter()));
        }
        if (style.getIndentLeft() > 0) {
            paragraph.setIndentationLeft((int) PoiUnits.inchesToTwips(style.getIndentLeft()));
        }
        if (style.getBorder() != null) {
            PoiParagraphBorders.apply(paragraph, style.getBorder());
        }
    }

    public ParagraphBuilder addImage(InputStream stream, PictureType type, ImageStyle imageStyle)
            throws IOException, InvalidFormatException {
        Objects.requireNonNull(stream, "stream");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(imageStyle, "imageStyle");
        XWPFRun run = paragraph.createRun();
        run.addPicture(stream, type.poiType(), "image." + type.extension(),
                Units.toEMU(imageStyle.getWidthPx()), Units.toEMU(imageStyle.getHeightPx()));
        return this;
    }

    public ParagraphBuilder addRun(String text, TextStyle style) {
        Objects.requireNonNull(style, "style");
        XWPFRun run = paragraph.createRun();
        run.setText(text != null ? text : "");
        applyTextStyle(run, style);
        return this;
    }

    public ParagraphBuilder addLineBreak() {
        paragraph.createRun().addBreak();
        return this;
    }

    public WordDocumentBuilder endParagraph() {
        return parent;
    }

    static void applyTextStyle(XWPFRun run, TextStyle style) {
        run.setFontFamily(style.getFontFamily());
        run.setFontSize(style.getFontSize());
        run.setBold(style.isBold());
        run.setItalic(style.isItalic());
        if (style.isUnderline()) {
            run.setUnderline(UnderlinePatterns.SINGLE);
        }
        run.setColor(HexColor.normalize(style.getColorHex()));
    }

    static ParagraphAlignment toPoi(Alignment alignment) {
        return switch (alignment) {
            case LEFT -> ParagraphAlignment.LEFT;
            case CENTER -> ParagraphAlignment.CENTER;
            case RIGHT -> ParagraphAlignment.RIGHT;
            case JUSTIFY -> ParagraphAlignment.BOTH;
        };
    }
}
