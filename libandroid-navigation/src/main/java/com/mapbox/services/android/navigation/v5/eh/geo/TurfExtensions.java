package com.mapbox.services.android.navigation.v5.eh.geo;

import com.mapbox.services.android.navigation.v5.eh.logging.Logger;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.turf.TurfConversion;
import com.mapbox.turf.TurfMeasurement;

import java.util.OptionalDouble;
import java.util.stream.IntStream;

/**
 * Some missing turf functions.
 */
public final class TurfExtensions {
    private static final Logger LOGGER = new Logger();

    private TurfExtensions() {
    }

    /**
     * Calculate the distance from a point to a line.
     *
     * @param pt    the point
     * @param line  the line
     * @param units the units for the distance
     * @return the distance
     */
    public static double pointToLineDistance(final Point pt, final LineString line, final String units) {
        // validation
        if (pt == null) {
            LOGGER.warn("pt is required");
            return Double.MAX_VALUE;
        }
        if (line == null || line.coordinates().size() < 2) {
            LOGGER.warn("line is required");
            return Double.MAX_VALUE;
        }

        // Go over each segment and calculate the distance
        OptionalDouble distance = IntStream.range(1, line.coordinates().size())
                .mapToDouble(i -> distanceToSegment(pt, line.coordinates().get(i - 1), line.coordinates().get(i)))
                .min();

        // Convert to requested units
        return TurfConversion.convertLength(distance.orElse(Double.MAX_VALUE), "degrees", units);
    }

    private static double distanceToSegment(final Point p, final Point a, final Point b) {
        Point v = Point.fromLngLat(b.longitude() - a.longitude(), b.latitude() - a.latitude());
        Point w = Point.fromLngLat(p.longitude() - a.longitude(), p.latitude() - a.latitude());

        double c1 = dot(w, v);
        if (c1 <= 0) {
            return TurfMeasurement.distance(p, a, "degrees");
        }
        double c2 = dot(v, v);
        if (c2 <= c1) {
            return TurfMeasurement.distance(p, b, "degrees");
        }
        double b2 = c1 / c2;
        Point pb = Point.fromLngLat(a.longitude() + (b2 * v.longitude()), a.latitude() + (b2 * v.latitude()));
        return TurfMeasurement.distance(p, pb, "degrees");
    }

    private static double dot(final Point u, final Point v) {
        return (u.longitude() * v.longitude() + u.latitude() * v.latitude());
    }
}
