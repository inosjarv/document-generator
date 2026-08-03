package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class TextStyle {

    @Builder.Default String fontFamily = "Calibri";
    @Builder.Default int fontSize = 11;
    @Builder.Default boolean bold = false;
    @Builder.Default boolean italic = false;
    @Builder.Default boolean underline = false;
    @Builder.Default String colorHex = "000000";

    public static TextStyle defaults() { return builder().build(); }
}
