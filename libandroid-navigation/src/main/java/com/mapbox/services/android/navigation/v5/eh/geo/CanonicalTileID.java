package com.mapbox.services.android.navigation.v5.eh.geo;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;

/**
 * Canonical tile identifier in a slippy map.
 */
public class CanonicalTileID {
    private int z;
    private int x;
    private int y;

    /**
     * Creates a new instance of CanonicalTileID representing a tile coordinate
     * in a slippy map.
     *
     * @param z The z coordinate or zoom level.
     * @param x The x coordinate.
     * @param y The y coordinate.
     */
    public CanonicalTileID(final int z, final int x, final int y) {
        this.z = z;
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a CanonicalTileID from a {@link UnwrappedTileID}, both
     * representing the same tile.
     *
     * @param unwrapped the unwrapped tile coordinate.
     */
    public CanonicalTileID(final UnwrappedTileID unwrapped) {
        int z = unwrapped.getZ();
        int x = unwrapped.getX();
        int y = unwrapped.getY();

        int wrap = (x < 0 ? x - (1 << z) + 1 : x) / (1 << z);

        this.z = z;
        this.x = x - wrap * (1 << z);
        this.y = y < 0 ? 0 : Math.min(y, (1 << z) - 1);
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
     * @return the bounding box for this tile
     */
    public BoundingBox getBounds() {
        return BoundingBox.fromPoints(getSouthWest(), getNorthEast());
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

    @Override
    public String toString() {
        return String.format("%d/%d/%d", z, x, y);
    }

    //CHECKSTYLE:OFF

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CanonicalTileID tileID = (CanonicalTileID) o;

        if (z != tileID.z) return false;
        if (x != tileID.x) return false;
        return y == tileID.y;
    }

    @Override
    public int hashCode() {
        int result = z;
        result = 31 * result + x;
        result = 31 * result + y;
        return result;
    }

    //CHECKSTYLE:ON
}
