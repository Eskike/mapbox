package com.mapbox.services.android.navigation.v5.eh.horizon.tracker;

import com.mapbox.services.android.navigation.v5.eh.horizon.EHorizon;
import com.mapbox.geojson.Point;

/**
 * Match found.
 */
public class PositiveTrackerResult implements TrackerResult {
    private final Point location;
    private final EHorizon horizon;

    PositiveTrackerResult(final Point location, final EHorizon horizon) {
        this.location = location;
        this.horizon = horizon;
    }

    @Override
    public Point location() {
        return location;
    }

    /**
     * @return the matching EHorizon
     */
    public EHorizon horizon() {
        return horizon;
    }
}
