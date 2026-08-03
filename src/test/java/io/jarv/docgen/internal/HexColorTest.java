package io.jarv.docgen.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HexColorTest {

    @Test
    void uppercasesSixDigitHex() {
        assertThat(HexColor.normalize("2c3e50")).isEqualTo("2C3E50");
    }

    @Test
    void stripsLeadingHash() {
        assertThat(HexColor.normalize("#2C3E50")).isEqualTo("2C3E50");
    }

    @Test
    void passesThroughAlreadyNormalizedInput() {
        assertThat(HexColor.normalize("FF0000")).isEqualTo("FF0000");
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> HexColor.normalize(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsShortForm() {
        assertThatThrownBy(() -> HexColor.normalize("fff"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonHex() {
        assertThatThrownBy(() -> HexColor.normalize("XYZ123"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsEmpty() {
        assertThatThrownBy(() -> HexColor.normalize(""))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
