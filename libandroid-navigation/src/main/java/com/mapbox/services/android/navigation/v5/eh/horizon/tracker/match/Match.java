package com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match;

import com.mapbox.services.android.navigation.v5.eh.cheapruler.PointOnLineResult;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;

/**
 * A match is binary on one hand (matches or doesn't) and also has
 * an associated cost to rank matches.
 */
public interface Match {

    /**
     * @return true if this is a match
     */
    boolean matches();

    /**
     * @return the cost
     */
    double cost();

    /**
     * @return the edge this match represents
     */
    Edge edge();

    /**
     * @return the {@link PointOnLineResult} of the match
     */
    PointOnLineResult pointOnLine();
}
