package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class ParagraphStyle {

    @Builder.Default Alignment alignment = Alignment.LEFT;

    /** {@code null} means inherit {@link DocumentTheme#getDefaultLineSpacing()}. */
    Double lineSpacingMultiplier;

    /** Space before paragraph, in points. */
    @Builder.Default double spaceBefore = 0;

    /** Space after paragraph, in points. */
    @Builder.Default double spaceAfter = 0;

    /** Left indent, in inches. */
    @Builder.Default double indentLeft = 0;

    public static ParagraphStyle defaults() { return builder().build(); }
}
