package com.mapbox.services.android.navigation.v5.eh.horizon.graph;

import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;

/**
 * Matched edges with the distance.
 */
public class MatchResult {
    private final double distance;
    private final Edge edge;


    /**
     * @param distance the distance in m
     * @param edge     the edge
     */
    public MatchResult(final double distance, final Edge edge) {
        this.distance = distance;
        this.edge = edge;
    }

    /**
     * @return the edge
     */
    public Edge getEdge() {
        return edge;
    }

    /**
     * @return the distance in m
     */
    public double getDistance() {
        return distance;
    }
}
