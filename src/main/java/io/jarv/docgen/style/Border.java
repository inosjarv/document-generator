package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Border {

    @Builder.Default BorderStyle style = BorderStyle.SINGLE;

    /** Line width in points. */
    @Builder.Default double widthPoints = 1.0;

    @Builder.Default String colorHex = "000000";

    /** Gap between border and content, in points. */
    @Builder.Default double spacingPoints = 4;

    public static Border none() {
        return builder().style(BorderStyle.NONE).build();
    }

    public static Border simple() {
        return builder().build();
    }

    public static Border thick(String colorHex) {
        return builder().widthPoints(2.0).colorHex(colorHex).build();
    }
}
