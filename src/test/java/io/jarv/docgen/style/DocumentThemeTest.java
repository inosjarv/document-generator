package io.jarv.docgen.style;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentThemeTest {

    @Test
    void defaultsAreReasonable() {
        DocumentTheme theme = DocumentTheme.defaults();
        assertThat(theme.getMarginTop()).isEqualTo(1.5);
        assertThat(theme.getMarginBottom()).isEqualTo(1.0);
        assertThat(theme.getDefaultLineSpacing()).isEqualTo(1.15);
        assertThat(theme.getPrimaryColor()).isEqualTo("333333");
    }

    @Test
    void corporatePresetOverridesColorsAndKeepsMarginDefaults() {
        DocumentTheme theme = DocumentTheme.corporate();
        assertThat(theme.getPrimaryColor()).isEqualTo("2C3E50");
        assertThat(theme.getSecondaryColor()).isEqualTo("7F8C8D");
        assertThat(theme.getMarginTop()).isEqualTo(1.5);
    }

    @Test
    void minimalPresetTightensTopMargin() {
        DocumentTheme theme = DocumentTheme.minimal();
        assertThat(theme.getMarginTop()).isEqualTo(1.0);
        assertThat(theme.getPrimaryColor()).isEqualTo("000000");
    }

    @Test
    void toBuilderCopiesThenOverrides() {
        DocumentTheme original = DocumentTheme.corporate();
        DocumentTheme tweaked = original.toBuilder().marginLeft(2.0).build();
        assertThat(tweaked.getMarginLeft()).isEqualTo(2.0);
        assertThat(tweaked.getPrimaryColor()).isEqualTo(original.getPrimaryColor());
    }
}
