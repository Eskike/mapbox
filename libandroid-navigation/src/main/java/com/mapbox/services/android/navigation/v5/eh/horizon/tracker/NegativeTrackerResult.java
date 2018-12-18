package com.mapbox.services.android.navigation.v5.eh.horizon.tracker;

import com.mapbox.geojson.Point;

/**
 * No matching edge could be found yet.
 */
public class NegativeTrackerResult implements TrackerResult {
    private final Point location;

    NegativeTrackerResult(final Point location) {
        this.location = location;
    }

    @Override
    public Point location() {
        return location;
    }

}
