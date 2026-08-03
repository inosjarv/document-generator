package io.jarv.docgen.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocxUnitsTest {

    @Test
    void oneInchIs1440Twips() {
        assertThat(DocxUnits.inchesToTwips(1.0)).isEqualTo(1440L);
    }

    @Test
    void fractionalInchesConvert() {
        assertThat(DocxUnits.inchesToTwips(1.5)).isEqualTo(2160L);
    }

    @Test
    void zeroInchesIsZero() {
        assertThat(DocxUnits.inchesToTwips(0)).isEqualTo(0L);
    }

    @Test
    void twelvePointsIs240Twentieths() {
        assertThat(DocxUnits.pointsToTwentieths(12.0)).isEqualTo(240);
    }

    @Test
    void ninetySixPixelsIsOneInchInEmu() {
        // 96 px at 96 dpi = 1 inch = 914400 EMU
        assertThat(DocxUnits.pixelsToEmu(96)).isEqualTo(914_400L);
    }
}
