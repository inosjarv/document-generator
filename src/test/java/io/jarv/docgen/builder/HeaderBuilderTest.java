package io.jarv.docgen.builder;

import io.jarv.docgen.style.DocumentTheme;
import io.jarv.docgen.style.TextStyle;
import jakarta.xml.bind.JAXBElement;
import org.docx4j.wml.Br;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Tr;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static org.assertj.core.api.Assertions.assertThat;

class HeaderBuilderTest {

    @Test
    void singleRightEntryProducesNoLineBreak() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginHeader()
                .addRightText("One", TextStyle.defaults())
                .endHeader())) {
            assertThat(countBreaksInRightHeaderCell(round)).isZero();
        }
    }

    @Test
    void twoRightEntriesGetExactlyOneBreakBetween() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginHeader()
                .addRightText("First", TextStyle.defaults())
                .addRightText("Second", TextStyle.defaults())
                .endHeader())) {
            assertThat(countBreaksInRightHeaderCell(round)).isEqualTo(1);
        }
    }

    @Test
    void threeRightEntriesGetExactlyTwoBreaks() throws Exception {
        try (RoundTrip.Doc round = RoundTrip.of(b -> b.beginHeader()
                .addRightText("A", TextStyle.defaults())
                .addRightText("B", TextStyle.defaults())
                .addRightText("C", TextStyle.defaults())
                .endHeader())) {
            assertThat(countBreaksInRightHeaderCell(round)).isEqualTo(2);
        }
    }

    @Test
    void headerMarginIsAppliedInTwips() throws Exception {
        DocumentTheme theme = DocumentTheme.builder().marginHeader(0.75).build();
        try (RoundTrip.Doc round = RoundTrip.of(theme, b -> b.beginHeader()
                .addRightText("x", TextStyle.defaults())
                .endHeader())) {
            var pageMar = round.body().getSectPr().getPgMar();
            assertThat(pageMar.getHeader()).isEqualTo(BigInteger.valueOf(1080L));
        }
    }

    private static long countBreaksInRightHeaderCell(RoundTrip.Doc doc) throws Exception {
        Hdr hdr = doc.firstHeaderContents();
        Tbl table = RoundTrip.filter(hdr.getContent(), Tbl.class).get(0);
        Tr row = RoundTrip.filter(table.getContent(), Tr.class).get(0);
        Tc rightCell = RoundTrip.filter(row.getContent(), Tc.class).get(1);
        P rightPara = RoundTrip.filter(rightCell.getContent(), P.class).get(0);
        return RoundTrip.filter(rightPara.getContent(), R.class).stream()
                .flatMap(r -> r.getContent().stream())
                .map(o -> (o instanceof JAXBElement<?> je) ? je.getValue() : o)
                .filter(Br.class::isInstance)
                .count();
    }
}
