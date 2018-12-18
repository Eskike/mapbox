package com.mapbox.services.android.navigation.v5.eh.geo;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;

/**
 * Unwrapped tile identifier in a slippy map. Similar to {@link CanonicalTileID},
 * but might go around the globe.
 */
public class UnwrappedTileID {
    private int z;
    private int x;
    private int y;

    /**
     * Creates a new instance of UnwrappedTileID representing a tile coordinate
     * in a slippy map that might go around the globe.
     *
     * @param z The z coordinate or zoom level.
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public UnwrappedTileID(final int z, final int x, final int y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a UnwrappedTileID from a geographic coordinate and zoom level.
     *
     * @param coordinate the geographic coordinate.
     * @param zoom       the zoom level.
     */
    public UnwrappedTileID(final Point coordinate, final int zoom) {
        double lat = coordinate.latitude();
        double lng = coordinate.longitude();

        //CHECKSTYLE:OFF MagicNumber

        // See: http://wiki.openstreetmap.org/wiki/Slippy_map_tilenames
        z = zoom;
        x = (int) Math.floor((lng + 180.0) / 360.0 * Math.pow(2.0, zoom));
        y = (int) Math.floor((1.0 - Math.log(Math.tan(lat * Math.PI / 180.0)
                + 1.0 / Math.cos(lat * Math.PI / 180.0)) / Math.PI) / 2.0 * Math.pow(2.0, zoom));

        //CHECKSTYLE:ON MagicNumber

        // Needed because how rounding works on Java is different
        // from how it works in C++.
        if (x < 0) {
            x = 0;
        }
        if (x >= (1 << zoom)) {
            x = ((1 << zoom) - 1);
        }
        if (y < 0) {
            y = 0;
        }
        if (y >= (1 << zoom)) {
            y = ((1 << zoom) - 1);
        }
    }

    /**
     * @return the z coordinate or zoom level.
     */
    public int getZ() {
        return z;
    }

    /**
     * @return the x coordinate.
     */
    public int getX() {
        return x;
    }

    /**
     * @return the y coordinate.
     */
    public int getY() {
        return y;
    }

    /**
     * @return the bounding box for this tile
     */
    public BoundingBox getBounds() {
        return BoundingBox.fromPoints(getSouthWest(), getNorthEast());
    }

    /**
     * @return the north east corner of this tile
     */
    public Point getNorthEast() {
        return Point.fromLngLat(tile2lon(x + 1, z), tile2lat(y, z));
    }

    /**
     * @return the south west corner of this tile
     */
    public Point getSouthWest() {
        return Point.fromLngLat(tile2lon(x, z), tile2lat(y + 1, z));
    }

    /**
     * @return a {@link CanonicalTileID} created from this tile.
     */
    public CanonicalTileID toCanonicalTileID() {
        return new CanonicalTileID(this);
    }

    @Override
    public String toString() {
        return String.format("%d/%d/%d", z, x, y);
    }

    //CHECKSTYLE:OFF MagicNumber

    private static double tile2lon(final int x, final int z) {
        return x / Math.pow(2.0, z) * 360.0 - 180;
    }

    private static double tile2lat(final int y, final int z) {
        double n = Math.PI - (2.0 * Math.PI * y) / Math.pow(2.0, z);
        return Math.toDegrees(Math.atan(Math.sinh(n)));
    }

    //CHECKSTYLE:ON MagicNumber
}
