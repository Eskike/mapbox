package com.mapbox.services.android.navigation.v5.eh.vt;

import java.util.Locale;

/**
 * A simple point.
 */
class Point {
    private final double x;
    private final double y;

    /**
     * Creates a 3D point.
     *
     * @param x X coordinate.
     * @param y Y coordinate.
     */
    Point(final double x, final double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a Point from another Point.
     *
     * @param point the point to be copied.
     */
    Point(final Point point) {
        this.x = point.x;
        this.y = point.y;
    }

    private Point(final Builder builder) {
        x = builder.x;
        y = builder.y;
    }

    /**
     * @return a {@link Builder}
     */
    static Builder newBuilder() {
        return new Builder();
    }

    /**
     * @return the X coordinate.
     */
    double getX() {
        return x;
    }

    /**
     * @return the Y coordinate.
     */
    double getY() {
        return y;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.5f,%.5f", x, y);
    }

    /**
     * {@code Point} builder static inner class.
     */
    static final class Builder {
        private double x;
        private double y;

        private Builder() {
        }

        /**
         * Sets the {@code x} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param val the {@code x} to set
         * @return a reference to this Builder
         */
        Builder withX(final double val) {
            x = val;
            return this;
        }

        /**
         * Sets the {@code y} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param val the {@code y} to set
         * @return a reference to this Builder
         */
        Builder withY(final double val) {
            y = val;
            return this;
        }

        /**
         * @return the X coordinate.
         */
        double getX() {
            return x;
        }

        /**
         * @return the Y coordinate.
         */
        double getY() {
            return y;
        }

        /**
         * Returns a {@code Point} built from the parameters previously set.
         *
         * @return a {@code Point} built with parameters of this {@code Point.Builder}
         */
        Point build() {
            return new Point(this);
        }
    }
}
