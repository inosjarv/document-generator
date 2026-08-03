package io.jarv.docgen.builder;

import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TextStyle;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageBmpPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageGifPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImageJpegPart;
import org.docx4j.openpackaging.parts.WordprocessingML.ImagePngPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.Drawing;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

class ImageTest {

    private static byte[] tinyPng() throws Exception {
        BufferedImage img = new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, "png", baos);
        return baos.toByteArray();
    }

    @Test
    void topLevelAddImageCreatesParagraphWithPicture() throws Exception {
        byte[] png = tinyPng();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.addImage(
                new ByteArrayInputStream(png),
                PictureType.PNG,
                ImageStyle.builder().widthPx(50).heightPx(50).build()))) {

            assertThat(round.paragraphs()).hasSize(1);
            assertThat(imagePartCount(round)).isEqualTo(1);
        }
    }

    @Test
    void inlineAddImageAttachesToExistingParagraph() throws Exception {
        byte[] png = tinyPng();
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginParagraph()
                .addRun("Before ", TextStyle.defaults())
                .addImage(new ByteArrayInputStream(png),
                        PictureType.PNG,
                        ImageStyle.builder().widthPx(20).heightPx(20).build())
                .addRun(" after", TextStyle.defaults())
                .endParagraph())) {

            assertThat(round.paragraphs()).hasSize(1);
            assertThat(imagePartCount(round)).isEqualTo(1);
            long drawingRuns = countDrawings(round.paragraphs().get(0));
            assertThat(drawingRuns).isEqualTo(1);
        }
    }

    private static long imagePartCount(RoundTrip.Doc doc) {
        var rp = doc.pkg().getMainDocumentPart().getRelationshipsPart();
        long count = 0;
        for (Relationship rel : rp.getJaxbElement().getRelationship()) {
            Object part = rp.getPart(rel);
            if (part instanceof ImagePngPart
                    || part instanceof ImageJpegPart
                    || part instanceof ImageGifPart
                    || part instanceof ImageBmpPart) {
                count++;
            }
        }
        return count;
    }

    private static long countDrawings(P paragraph) {
        long count = 0;
        for (R run : RoundTrip.filter(paragraph.getContent(), R.class)) {
            for (Object o : run.getContent()) {
                Object unwrapped = (o instanceof JAXBElement<?> je) ? je.getValue() : o;
                if (unwrapped instanceof Drawing) count++;
            }
        }
        return count;
    }
}
