package com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match;

import com.mapbox.services.android.navigation.v5.eh.cheapruler.PointOnLineResult;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;

import static com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Constants.THRESHOLD_MATCH_BEARING_OFFSET;
import static com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Constants.THRESHOLD_MATCH_DISTANCE_M;

abstract class BaseMatch implements Match {

    private final Edge edge;

    BaseMatch(final Edge edge) {
        this.edge = edge;
    }

    boolean matches(final double distance, final double bearingOffset) {
        return distance < THRESHOLD_MATCH_DISTANCE_M
                && bearingOffset < THRESHOLD_MATCH_BEARING_OFFSET;
    }

    boolean isPassed(final PointOnLineResult pointOnLine) {
        return pointOnLine.t() == 1 && pointOnLine.index() == edge().getCenterLine().coordinates().size() - 2;
    }

    @Override
    public Edge edge() {
        return edge;
    }
}
