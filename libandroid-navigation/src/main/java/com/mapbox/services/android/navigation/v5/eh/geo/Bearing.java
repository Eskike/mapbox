package com.mapbox.services.android.navigation.v5.eh.geo;

/**
 * Bearing utilities.
 */
public final class Bearing {
    /**
     * 90 degrees.
     */
    public static final int RIGHT_ANGLE = 90;
    /**
     * 180 degrees.
     */
    public static final int HALF_ROTATION = 180;
    /**
     * 360 degrees.
     */
    public static final int FULL_ROTATION = 360;

    private Bearing() {
    }

    /**
     * @param bearing the input
     * @return the normalised bearing (0-360)
     */
    public static double normalise(final double bearing) {
        double x = bearing;
        while (x > HALF_ROTATION) {
            x -= FULL_ROTATION;
        }
        while (x < -HALF_ROTATION) {
            x += FULL_ROTATION;
        }
        return x;
    }

    /**
     * Diff in degrees between bearing a and b.
     *
     * @param a bearing
     * @param b bearing
     * @return the normalised diff
     */
    public static double diff(final double a, final double b) {
        double r = (b - a) % FULL_ROTATION;
        return normalise(r);
    }

    /**
     * Diff in degrees between bearing a and b, optionally absolute.
     *
     * @param a   bearing
     * @param b   bearing
     * @param abs return absolute diff if true
     * @return the normalised diff
     */
    public static double diff(final double a, final double b, final boolean abs) {
        return abs ? Math.abs(diff(a, b)) : diff(a, b);
    }
}
