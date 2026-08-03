package io.jarv.docgen.internal;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PoiUnitsTest {

    @Test
    void oneInchIs1440Twips() {
        assertThat(PoiUnits.inchesToTwips(1.0)).isEqualTo(1440L);
    }

    @Test
    void fractionalInchesConvert() {
        assertThat(PoiUnits.inchesToTwips(1.5)).isEqualTo(2160L);
    }

    @Test
    void zeroInchesIsZero() {
        assertThat(PoiUnits.inchesToTwips(0)).isEqualTo(0L);
    }

    @Test
    void twelvePointsIs240Twentieths() {
        assertThat(PoiUnits.pointsToTwentieths(12.0)).isEqualTo(240);
    }
}
