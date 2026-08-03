package io.jarv.docgen.builder;

import io.jarv.docgen.style.ImageStyle;
import io.jarv.docgen.style.PictureType;
import io.jarv.docgen.style.TextStyle;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
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
        try (XWPFDocument round = RoundTrip.of(b -> b.addImage(
                new ByteArrayInputStream(png),
                PictureType.PNG,
                ImageStyle.builder().widthPx(50).heightPx(50).build()))) {

            assertThat(round.getParagraphs()).hasSize(1);
            assertThat(round.getAllPictures()).hasSize(1);
        }
    }

    @Test
    void inlineAddImageAttachesToExistingParagraph() throws Exception {
        byte[] png = tinyPng();
        try (XWPFDocument round = RoundTrip.of(b -> b.beginParagraph()
                .addRun("Before ", TextStyle.defaults())
                .addImage(new ByteArrayInputStream(png),
                        PictureType.PNG,
                        ImageStyle.builder().widthPx(20).heightPx(20).build())
                .addRun(" after", TextStyle.defaults())
                .endParagraph())) {

            assertThat(round.getParagraphs()).hasSize(1);
            assertThat(round.getAllPictures()).hasSize(1);
            assertThat(round.getParagraphs().get(0).getRuns().size()).isGreaterThanOrEqualTo(3);
        }
    }
}
