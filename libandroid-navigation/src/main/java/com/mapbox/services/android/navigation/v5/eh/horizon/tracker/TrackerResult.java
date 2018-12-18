package com.mapbox.services.android.navigation.v5.eh.horizon.tracker;

import com.mapbox.geojson.Point;

/**
 * {@link EHorizonTracker} result.
 */
public interface TrackerResult {

    /**
     * @return the location, matched or raw.
     */
    Point location();
}
