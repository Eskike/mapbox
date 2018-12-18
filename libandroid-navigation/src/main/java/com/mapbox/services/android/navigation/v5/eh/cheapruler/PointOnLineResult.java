package com.mapbox.services.android.navigation.v5.eh.cheapruler;

import com.mapbox.geojson.Point;

import java.util.List;

/**
 * Result for {@link CheapRuler#pointOnLine(List, Point)}].
 */
public final class PointOnLineResult {
    private final Point point;
    private final int index;
    private final double t;

    PointOnLineResult(final Point point, final int index, final double t) {
        this.point = point;
        this.index = index;
        this.t = t;
    }

    /**
     * @return closest point on the line from the given point
     */
    public Point point() {
        return point;
    }

    /**
     * @return the start index of the segment with the closest point
     */
    public int index() {
        return index;
    }

    /**
     * @return a parameter from 0 to 1 that indicates where the closest point is on that segment
     */
    public double t() {
        return t;
    }
}
