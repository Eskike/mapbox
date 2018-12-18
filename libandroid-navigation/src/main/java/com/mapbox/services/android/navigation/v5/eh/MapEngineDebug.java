package com.mapbox.services.android.navigation.v5.eh;

import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;

import java.util.Collection;
import java.util.Set;

/**
 * Debug interface of the {@link MapEngine}.
 */
public interface MapEngineDebug {

    /**
     * Get all edges in the current graph.
     *
     * @return all edges
     */
    Collection<Edge> getAllCurrentEdges();

    /**
     * Get a subgraph around a node to inspect.
     *
     * @param nodeId the node
     * @return the collection of edges connecting to this node
     */
    Set<Edge> getConnectedEdges(long nodeId);
}
