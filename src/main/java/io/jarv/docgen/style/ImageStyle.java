package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ImageStyle {

    int widthPx;
    int heightPx;

    /** Only meaningful when used via the top-level {@code addImage} — controls the wrapper paragraph. */
    @Builder.Default Alignment alignment = Alignment.LEFT;
}
