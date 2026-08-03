package io.jarv.docgen.style;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StyleDefaultsTest {

    @Test
    void textStyleDefaultsAreConservative() {
        TextStyle style = TextStyle.defaults();
        assertThat(style.getFontFamily()).isEqualTo("Calibri");
        assertThat(style.getFontSize()).isEqualTo(11);
        assertThat(style.isBold()).isFalse();
        assertThat(style.isItalic()).isFalse();
        assertThat(style.isUnderline()).isFalse();
        assertThat(style.getColorHex()).isEqualTo("000000");
    }

    @Test
    void paragraphStyleDefaultsInheritLineSpacingFromTheme() {
        ParagraphStyle style = ParagraphStyle.defaults();
        assertThat(style.getAlignment()).isEqualTo(Alignment.LEFT);
        assertThat(style.getLineSpacingMultiplier()).isNull();
        assertThat(style.getSpaceBefore()).isEqualTo(0.0);
        assertThat(style.getSpaceAfter()).isEqualTo(0.0);
        assertThat(style.getIndentLeft()).isEqualTo(0.0);
    }
}
