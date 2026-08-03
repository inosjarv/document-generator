package io.jarv.docgen.internal;

public final class PoiUnits {

    public static final int TWIPS_PER_INCH = 1440;
    public static final int TWENTIETHS_PER_POINT = 20;

    private PoiUnits() {}

    public static long inchesToTwips(double inches) {
        return (long) (inches * TWIPS_PER_INCH);
    }

    public static int pointsToTwentieths(double points) {
        return (int) (points * TWENTIETHS_PER_POINT);
    }
}
