package com.mapbox.services.android.navigation.v5.eh.geo;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;
import com.mapbox.geojson.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities to handle {@link BoundingBox}.
 */
public final class BoundingBoxUtils {

    private BoundingBoxUtils() {
    }

    /**
     * Check if the bounding box contains the point.
     *
     * @param bounds the bbox
     * @param point  the point
     * @return true if contained
     */
    public static boolean contains(final BoundingBox bounds, final Point point) {
        if (bounds == null || point == null) {
            return false;
        }

        return point.latitude() <= bounds.north()
                && point.latitude() >= bounds.south()
                && point.longitude() <= bounds.east()
                && point.longitude() >= bounds.west();
    }

    /**
     * Extend the bounding box to contain the point.
     *
     * @param box   the source box
     * @param point a geographic coordinate.
     * @return the extended bounding box
     */
    public static BoundingBox extend(final BoundingBox box, final Point point) {
        double south, west, north, east;

        south = point.latitude() < box.south() ? point.latitude() : box.south();
        north = point.latitude() > box.north() ? point.latitude() : box.north();
        west = point.longitude() < box.west() ? point.longitude() : box.west();
        east = point.longitude() > box.east() ? point.longitude() : box.east();

        return BoundingBox.fromLngLats(west, south, east, north);
    }

    /**
     * Creates a bound from two arbitrary points. Contrary to the constructor,
     * this method always creates a non-empty box.
     *
     * @param a the first point.
     * @param b the second point.
     * @return the convex hull.
     */
    public static BoundingBox fromCoordinates(final Point a, final Point b) {
        BoundingBox bounds = BoundingBox.fromPoints(a, a);
        return extend(bounds, b);
    }

    /**
     * Check if the given bbox is empty.
     *
     * @param box the bbox
     * @return true if empty
     */
    public static boolean isEmpty(final BoundingBox box) {
        return box.southwest().latitude() > box.northeast().latitude()
                || box.southwest().longitude() > box.northeast().longitude();
    }

    /**
     * Create a {@link Polygon} from a {@link BoundingBox}.
     *
     * @param bbox the {@link BoundingBox}
     * @return the {@link Polygon}
     */
    public static Polygon toPolygon(final BoundingBox bbox) {
        List<Point> bbPoints = new ArrayList<>();
        bbPoints.add(bbox.northeast());
        bbPoints.add(Point.fromLngLat(bbox.east(), bbox.south()));
        bbPoints.add(bbox.southwest());
        bbPoints.add(Point.fromLngLat(bbox.west(), bbox.north()));
        bbPoints.add(bbox.northeast());
        ArrayList<List<Point>> ring = new ArrayList<>();
        ring.add(bbPoints);
        return Polygon.fromLngLats(ring);
    }
}
