package io.jarv.docgen.style;

import lombok.Builder;
import lombok.Value;

/** Per-side borders. Null on any side means no border applied there. */
@Value
@Builder(toBuilder = true)
public class BorderSet {

    Border top;
    Border bottom;
    Border left;
    Border right;

    public static BorderSet all(Border border) {
        return builder().top(border).bottom(border).left(border).right(border).build();
    }

    public static BorderSet horizontal(Border border) {
        return builder().top(border).bottom(border).build();
    }

    public static BorderSet none() {
        return all(Border.none());
    }
}
