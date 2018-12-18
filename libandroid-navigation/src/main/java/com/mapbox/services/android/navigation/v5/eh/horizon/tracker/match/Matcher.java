package com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match;

import com.mapbox.services.android.navigation.v5.eh.cheapruler.CheapRuler;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;
import com.mapbox.geojson.Point;

/**
 * Factory functions to create specific {@link Match} implementations.
 */
public final class Matcher {
    private Matcher() {
    }

    /**
     * Create a {@link Match} with only location information.
     *
     * @param ruler            the current ruler
     * @param edge             the proposed edge
     * @param newPosition     the new position
     * @param referenceBearing the reference bearing (from the location history)
     * @return a {@link Match}
     */
    public static Match forLocation(final CheapRuler ruler,
                                    final Edge edge,
                                    final Point newPosition,
                                    final double referenceBearing) {
        return new LocationHistoryMatch(ruler, edge, newPosition, referenceBearing);
    }

    /**
     * Create a {@link Match} for a proposed edge, relative to a current edge, given some
     * position information.
     *
     * @param ruler            the current ruler
     * @param currentEdge      the current edge
     * @param candidateEdge    the proposed edge
     * @param previousPosition the previous position
     * @param newPosition      the new position
     * @return a {@link Match}
     */
    public static Match forEdgeAndLocation(final CheapRuler ruler,
                                           final Edge currentEdge,
                                           final Edge candidateEdge,
                                           final Point previousPosition,
                                           final Point newPosition) {
        return new EdgeAndLocationMatch(ruler, currentEdge, candidateEdge, previousPosition, newPosition);
    }
}
