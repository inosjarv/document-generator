package io.jarv.docgen.internal;

import java.util.regex.Pattern;

public final class HexColor {

    private static final Pattern SIX_HEX = Pattern.compile("[0-9A-Fa-f]{6}");

    private HexColor() {}

    public static String normalize(String hex) {
        if (hex == null) {
            throw new IllegalArgumentException("color hex must not be null");
        }
        String stripped = hex.startsWith("#") ? hex.substring(1) : hex;
        if (!SIX_HEX.matcher(stripped).matches()) {
            throw new IllegalArgumentException("invalid 6-digit hex color: " + hex);
        }
        return stripped.toUpperCase();
    }
}
