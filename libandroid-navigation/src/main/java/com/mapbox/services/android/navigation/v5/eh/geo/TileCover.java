package com.mapbox.services.android.navigation.v5.eh.geo;

import com.mapbox.services.android.navigation.v5.eh.utils.Constants;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.Point;

import java.util.HashSet;
import java.util.Set;

/**
 * Helper funtions to get a tile cover, i.e. a set of tiles needed for
 * covering a bounding box.
 */
public final class TileCover {
    private TileCover() {
    }

    /**
     * Get the minimum set of tiles to cover a rectangular bounding box at a certain zoom level.
     * A big bounding box at a high zoom level, like 20, will return an absurd amount of tiles.
     *
     * @param bounds the bounding box.
     * @param zoom   the zoom level.
     * @return the tile set.
     */
    public static Set<CanonicalTileID> get(final BoundingBox bounds, final int zoom) {
        Set<CanonicalTileID> tiles = new HashSet<CanonicalTileID>();

        if (BoundingBoxUtils.isEmpty(bounds)
                || bounds.south() > Constants.LATITUDE_MAX
                || bounds.west() < -Constants.LATITUDE_MAX) {
            return tiles;
        }

        BoundingBox hull = BoundingBoxUtils.fromCoordinates(
                Point.fromLngLat(bounds.west(), Math.max(bounds.south(), -Constants.LATITUDE_MAX)),
                Point.fromLngLat(bounds.east(), Math.min(bounds.north(), Constants.LATITUDE_MAX)));

        UnwrappedTileID sw = new UnwrappedTileID(hull.southwest(), zoom);
        UnwrappedTileID ne = new UnwrappedTileID(hull.northeast(), zoom);

        // Scanlines.
        for (int x = sw.getX(); x <= ne.getX(); ++x) {
            for (int y = ne.getY(); y <= sw.getY(); ++y) {
                tiles.add(new UnwrappedTileID(zoom, x, y).toCanonicalTileID());
            }
        }

        return tiles;
    }
}
