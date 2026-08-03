package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class DocumentTheme {

    @Builder.Default double marginTop = 1.5;
    @Builder.Default double marginBottom = 1.0;
    @Builder.Default double marginLeft = 1.0;
    @Builder.Default double marginRight = 1.0;
    @Builder.Default double marginHeader = 0.5;

    @Builder.Default String primaryColor = "333333";
    @Builder.Default String secondaryColor = "666666";

    @Builder.Default double defaultLineSpacing = 1.15;

    public static DocumentTheme defaults() {
        return builder().build();
    }

    public static DocumentTheme corporate() {
        return builder()
                .primaryColor("B91C1C")       // crimson — strong red for headings
                .secondaryColor("78350F")     // warm brown — pairs with the red without clashing
                .defaultLineSpacing(1.3)
                .marginHeader(0.6)
                .build();
    }

    public static DocumentTheme minimal() {
        return builder()
                .marginTop(1.0)
                .primaryColor("000000")
                .secondaryColor("444444")
                .build();
    }
}
