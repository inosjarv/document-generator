package io.jarv.docgen.style;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BorderTest {

    @Test
    void simpleIsOnePointSingleBlack() {
        Border b = Border.simple();
        assertThat(b.getStyle()).isEqualTo(BorderStyle.SINGLE);
        assertThat(b.getWidthPoints()).isEqualTo(1.0);
        assertThat(b.getColorHex()).isEqualTo("000000");
    }

    @Test
    void noneUsesNoneStyle() {
        assertThat(Border.none().getStyle()).isEqualTo(BorderStyle.NONE);
    }

    @Test
    void thickIsTwoPointsWithColor() {
        Border b = Border.thick("FF00FF");
        assertThat(b.getWidthPoints()).isEqualTo(2.0);
        assertThat(b.getColorHex()).isEqualTo("FF00FF");
    }

    @Test
    void borderSetAllPopulatesFourSides() {
        BorderSet all = BorderSet.all(Border.simple());
        assertThat(all.getTop()).isNotNull();
        assertThat(all.getBottom()).isNotNull();
        assertThat(all.getLeft()).isNotNull();
        assertThat(all.getRight()).isNotNull();
    }

    @Test
    void borderSetHorizontalLeavesSidesNull() {
        BorderSet hz = BorderSet.horizontal(Border.simple());
        assertThat(hz.getTop()).isNotNull();
        assertThat(hz.getBottom()).isNotNull();
        assertThat(hz.getLeft()).isNull();
        assertThat(hz.getRight()).isNull();
    }
}
