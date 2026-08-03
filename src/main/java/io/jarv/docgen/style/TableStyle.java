package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TableStyle {

    /** Border on all four outer edges. */
    @Builder.Default Border outer = Border.simple();

    /** Border between rows and columns. */
    @Builder.Default Border inner = Border.simple();

    /** Width as a percentage string, e.g. "100%" or "80%". */
    @Builder.Default String widthPercent = "100%";

    public static TableStyle bordered() {
        return builder().build();
    }

    public static TableStyle borderless() {
        return builder().outer(Border.none()).inner(Border.none()).build();
    }

    public static TableStyle outerOnly() {
        return builder().inner(Border.none()).build();
    }

    public static TableStyle gridOnly() {
        return builder().outer(Border.none()).build();
    }
}
