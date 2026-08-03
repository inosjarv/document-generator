package io.jarv.docgen.internal;

public final class DocxUnits {

    public static final int TWIPS_PER_INCH = 1440;
    public static final int TWENTIETHS_PER_POINT = 20;
    public static final int EMU_PER_INCH = 914_400;
    public static final int EMU_PER_PIXEL_96DPI = EMU_PER_INCH / 96;

    private DocxUnits() {}

    public static long inchesToTwips(double inches) {
        return (long) (inches * TWIPS_PER_INCH);
    }

    public static int pointsToTwentieths(double points) {
        return (int) (points * TWENTIETHS_PER_POINT);
    }

    public static long pixelsToEmu(int pixels) {
        return (long) pixels * EMU_PER_PIXEL_96DPI;
    }
}
