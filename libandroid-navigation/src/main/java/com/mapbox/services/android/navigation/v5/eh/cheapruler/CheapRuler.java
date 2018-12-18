package com.mapbox.services.android.navigation.v5.eh.cheapruler;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.List;

/**
 * Quick java port of https://github.com/mapbox/cheap-ruler.
 */
public final class CheapRuler {

    /**
     * Create a CheapRuler for the given reference latitude.
     *
     * @param latitude the reference latitude
     * @param unit     the measurement unit to use
     * @return the {@link CheapRuler} instance
     */
    public static CheapRuler forLatitude(final double latitude, final Unit unit) {
        return new CheapRuler(latitude, unit);
    }

    /**
     * Multipliers for converting between units.
     * <p>
     * example // convert 50 meters to yards
     * 50 * cheapRuler.units.yards / cheapRuler.units.meters;
     */
    public enum Unit {
        //CHECKSTYLE:OFF
        KILOMETERS(1),
        MILES(1000 / 1609.344),
        NAUTICAL_MILES(1000 / 1852.),
        METERS(1000),
        METRES(1000),
        YARDS(1000 / 0.9144),
        FEET(1000 / 0.3048),
        INCHES(1000 / 0.0254);
        //CHECKSTYLE:ON

        private final double multiplier;

        Unit(final double multiplier) {
            this.multiplier = multiplier;
        }

        /**
         * @return The multiplier for this Unit.
         */
        public double getMultiplier() {
            return multiplier;
        }
    }

    private double kx;
    private double ky;

    /**
     * Create a CheapRuler for the given reference latitude.
     *
     * @param latitude the reference latitude
     * @param unit     the unit to use
     */
    private CheapRuler(final double latitude, final Unit unit) {
        double multiplier = unit.multiplier;

        //CHECKSTYLE:OFF
        double cos = Math.cos(latitude * Math.PI / 180);
        double cos2 = 2 * cos * cos - 1;
        double cos3 = 2 * cos * cos2 - cos;
        double cos4 = 2 * cos * cos3 - cos2;
        double cos5 = 2 * cos * cos4 - cos3;

        // multipliers for converting longitude and latitude degrees into distance (http://1.usa.gov/1Wb1bv7)
        this.kx = multiplier * (111.41513 * cos - 0.09455 * cos3 + 0.00012 * cos5);
        this.ky = multiplier * (111.13209 - 0.56605 * cos2 + 0.0012 * cos4);
        //CHECKSTYLE:ON
    }

    /**
     * Given two points of the form  returns the distance.
     *
     * @param a point a
     * @param b point b
     * @return the distance
     */
    public double distance(final Point a, final Point b) {
        double dx = (a.longitude() - b.longitude()) * kx;
        double dy = (a.latitude() - b.latitude()) * ky;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Given a line (an array of points), returns the total line distance.
     *
     * @param points the line
     * @return the total distance
     */
    public double lineDistance(final List<Point> points) {
        double total = 0.;

        if (points.isEmpty()) {
            return total;
        }

        for (int i = 0; i < points.size() - 1; ++i) {
            total += distance(points.get(i), points.get(i + 1));
        }

        return total;
    }

    /**
     * Returns the bearing between two points in angles.
     *
     * @param a point a
     * @param b point b
     * @return the angle in degrees
     **/
    public double bearing(final Point a, final Point b) {
        double dx = (b.longitude() - a.longitude()) * kx;
        double dy = (b.latitude() - a.latitude()) * ky;

        if (dx == 0 && dy == 0) {
            return 0.;
        }

        //CHECKSTYLE:OFF
        double value = Math.atan2(dx, dy) * 180. / Math.PI;

        if (value > 180.) {
            value -= 360.;
        }
        //CHECKSTYLE:ON

        return value;
    }

    /**
     * Returns a new point given easting and northing offsets (in ruler units) from the starting point.
     *
     * @param point the point to offset
     * @param dx    easting.
     * @param dy    northing.
     * @return the offset {@link Point}
     */
    public Point offset(final Point point, final double dx, final double dy) {
        return Point.fromLngLat(
                point.longitude() + dx / this.kx,
                point.latitude() + dy / this.ky
        );
    }

    /**
     * Given a point, returns a bounding box object ([w, s, e, n]) created from the
     * given point buffered by a given distance.
     *
     * @param point  the point to buffer
     * @param buffer the buffer size (in ruler units)
     * @return the {@link Point}
     */
    public BoundingBox bufferPoint(final Point point, final double buffer) {
        double v = buffer / this.ky;
        double h = buffer / this.kx;
        return BoundingBox.fromPoints(
                Point.fromLngLat(point.longitude() - h, point.latitude() - v),
                Point.fromLngLat(point.longitude() + h, point.latitude() + v)
        );
    }

    /**
     * Returns an object of the form {point, index, t}, where point is closest point on the line
     * from the given point, index is the start index of the segment with the closest point, and
     * t is a parameter from 0 to 1 that indicates where the closest point is on that segment.
     *
     * @param line the line
     * @param p    the point
     * @return the {@link PointOnLineResult}
     */
    public PointOnLineResult pointOnLine(final List<Point> line, final Point p) {
        double minDist = Double.MAX_VALUE;
        double minX = 0., minY = 0., minT = 0.;
        int minI = 0;

        if (line.isEmpty()) {
            return new PointOnLineResult(Point.fromLngLat(0, 0), 0, 0.);
        }

        for (int i = 0; i < line.size() - 1; ++i) {
            double t = 0.;
            Point point1 = line.get(i);
            double x = point1.longitude();
            double y = point1.latitude();

            Point point2 = line.get(i + 1);
            double dx = (point2.longitude() - x) * kx;
            double dy = (point2.latitude() - y) * ky;

            if (dx != 0. || dy != 0.) {
                t = ((p.longitude() - x) * kx * dx + (p.latitude() - y) * ky * dy) / (dx * dx + dy * dy);

                if (t > 1) {
                    x = point2.longitude();
                    y = point2.latitude();

                } else if (t > 0) {
                    x += (dx / kx) * t;
                    y += (dy / ky) * t;
                }
            }

            dx = (p.longitude() - x) * kx;
            dy = (p.latitude() - y) * ky;

            double sqDist = dx * dx + dy * dy;

            if (sqDist < minDist) {
                minDist = sqDist;
                minX = x;
                minY = y;
                minI = i;
                minT = t;
            }
        }

        return new PointOnLineResult(Point.fromLngLat(minX, minY), minI, Math.max(0., Math.min(1., minT)));
    }

    /**
     * Returns the distance along the linestring from the given {@link PointOnLineResult}.
     *
     * @param line the line
     * @param pol  the {@link PointOnLineResult}
     * @return the absolute t value
     */
    public double distanceAlong(final List<Point> line, final PointOnLineResult pol) {
        double sum = 0;
        for (int i = 0; i < pol.index(); i++) {
            sum += distance(line.get(i), line.get(i + 1));
        }
        sum += (distance(line.get(pol.index()), line.get(pol.index() + 1)) * pol.t());
        return sum;
    }

    /**
     * Returns a part of the given line between the start and the stop points indicated by distance along the line.
     *
     * @param start the slice start
     * @param stop  the slice stop
     * @param line  the line to slice
     * @return the sliced line
     */
    public List<Point> lineSliceAlong(final double start, final double stop, final List<Point> line) {
        double sum = 0.;
        List<Point> slice = new ArrayList<>();

        if (line.isEmpty()) {
            return slice;
        }

        for (int i = 0; i < line.size() - 1; ++i) {
            Point p0 = line.get(i);
            Point p1 = line.get(i + 1);
            double d = distance(p0, p1);

            sum += d;

            if (sum > start && slice.size() == 0) {
                slice.add(interpolate(p0, p1, (start - (sum - d)) / d));
            }

            if (sum >= stop) {
                slice.add(interpolate(p0, p1, (stop - (sum - d)) / d));
                return slice;
            }

            if (sum > start) {
                slice.add(p1);
            }
        }

        return slice;
    }

    /**
     * Returns a part of the given line between the start and the end of the line.
     *
     * @param start the slice start
     * @param line  the line to slice
     * @return the sliced line
     */
    public List<Point> lineSliceAlong(final double start, final List<Point> line) {
        double sum = 0.;
        List<Point> slice = new ArrayList<>();

        if (line.isEmpty()) {
            return slice;
        }

        for (int i = 0; i < line.size() - 1; ++i) {
            Point p0 = line.get(i);
            Point p1 = line.get(i + 1);
            double d = distance(p0, p1);

            sum += d;

            if (sum > start && slice.size() == 0) {
                slice.add(interpolate(p0, p1, (start - (sum - d)) / d));
            }

            if (sum > start) {
                slice.add(p1);
            }

            slice.add(p1);
        }

        return slice;
    }

    private static Point interpolate(final Point a, final Point b, final double t) {
        double dx = b.longitude() - a.longitude();
        double dy = b.latitude() - a.latitude();
        return Point.fromLngLat(a.longitude() + dx * t, a.latitude() + dy * t);
    }
}
