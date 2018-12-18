package com.mapbox.services.android.navigation.v5.eh.horizon.graph;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.MutableNetwork;
import com.google.common.graph.NetworkBuilder;
import com.mapbox.services.android.navigation.v5.eh.horizon.DrivablePath;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;
import com.mapbox.services.android.navigation.v5.eh.logging.Logger;
import com.mapbox.geojson.Point;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mapbox.services.android.navigation.v5.eh.geo.TurfExtensions.pointToLineDistance;

/**
 * Graph that manages the road topology. Used for querying
 * the visible road network within a range.
 */
public class Graph {
    private static final Logger LOGGER = new Logger(Graph.class);
    private MutableNetwork<Long, Edge> graph;
    private Map<Long, Edge> edgeMap = new HashMap<>();

    /**
     * Creates a Graph that structure to manage the
     * road network topology.
     */
    public Graph() {
        graph = NetworkBuilder
                .directed()
                // https://www.openstreetmap.org/node/360891540#map=19/48.19915/11.44762
                .allowsParallelEdges(true)
                .build();
    }

    /**
     * Adds an Edge to the graph connected by to nodes.
     *
     * @param e the Edge.
     */
    public void addEdge(final Edge e) {
        long in = e.getNodeIDIn();
        long out = e.getNodeIDOut();

        if (in == out) {
            LOGGER.debug("Discarding invalid edge id=%d", e.getId());
            return;
        }

        // Nodes are signed and we use -NODE_ID - 1 to create
        // a unique terminator node id.
        if (in == Edge.NULL_NODE) {
            in = -out - 1;
        }

        if (out == Edge.NULL_NODE) {
            out = -in - 1;
        }

//        // Merge Shapes if IDs are the same. Happens when
//        // Edges are in multiple tiles.
//        try {
//            Set<Edge> edges = graph.outEdges(in);
//            for (Edge edge : edges) {
//                if (edge.getId() == e.getId()) {
//                    e.getShapes().forEach(edge::addShape);
//                    return;
//                }
//            }
//        } catch (IllegalArgumentException exception) {
//            // Node not in the graph.
//        }

        if (graph.addEdge(in, out, e)) {
            edgeMap.put(e.getId(), e);
        } else {
            LOGGER.debug("Did not add duplicate Edge: %s", e.getId());
        }
    }

    /**
     * Removes an Edge from the graph and the connecting nodes in case
     * they get orphaned.
     *
     * @param e the Edge to be removed.
     */
    public void removeEdge(final Edge e) {
        assert graph.edges().contains(e);

        EndpointPair<Long> nodes = graph.incidentNodes(e);
        graph.removeEdge(e);

        if (graph.outDegree(nodes.target()) == 0 && graph.inDegree(nodes.target()) == 0) {
            graph.removeNode(nodes.target());
        }

        if (graph.outDegree(nodes.source()) == 0 && graph.inDegree(nodes.source()) == 0) {
            graph.removeNode(nodes.source());
        }

        edgeMap.remove(e.getId());
    }

    /**
     * Get all the Edges connected to a root Edge that could be reached
     * within a specified maximum distance.
     * <p>
     * The current algorithm is based on Uniform Cost Search and assumes
     * no previous knowledge of the topology of the graph. Each node
     * reachable within the distance will be visited once, unless in case
     * of a circular reference.
     *
     * @param e           the root Edge.
     * @param t           how far in root Edge the search should start from.
     * @param maxDistance the maximum distance to be transversed.
     * @return a set of Edges reachable from the root node at the
     * maximum distance.
     */
    public Set<Edge> getOutEdgesAtMaxDistanceFromEdge(final Edge e, final double t, final double maxDistance) {
        Set<Edge> explored = new HashSet<>();
        Set<Long> exclude = new HashSet<>();

        if (maxDistance <= 0) {
            return explored;
        }

        // Shift to compensate for the t value.
        double totalMaxDistance = maxDistance + t;

        LinkedList<Double> distance = new LinkedList<>();
        LinkedList<Edge> frontier = new LinkedList<>();

        distance.add(0.);
        frontier.add(e);

        while (!frontier.isEmpty()) {
            Edge current = frontier.remove();
            double currentDistance = distance.remove();

            // Handle circular references
            if (exclude.contains(current.getId())) {
                LOGGER.info("CIRCLES!!!!!");
                continue;
            }

            // Don't visit again
            exclude.add(current.getId());

            // Don't double back
            if (current.getCounterpartId() > 0) {
                exclude.add(current.getCounterpartId());
            }

            double tValue = currentDistance + current.getLength();
            double remaining = totalMaxDistance - currentDistance;

            // TODO
            explored.add(current);
//            explored.add(filterEdge(current, current == e ? t : 0,
//                    remaining > current.getLength() ? current.getLength() : remaining));

            if (tValue >= totalMaxDistance) {
                continue;
            }

            List<Edge> outEdges = graph.outEdges(graph.incidentNodes(current).target())
                    .stream()
                    // Handle circular references
                    .distinct()
                    .filter(edge -> !exclude.contains(edge.getId()))
                    .collect(Collectors.toList());
            outEdges.forEach(edge -> distance.add(tValue));
            frontier.addAll(outEdges);
        }

        return explored;
    }

    /**
     * Same as {@link #getOutEdgesAtMaxDistanceFromEdge(Edge, double, double)} but uses
     * the Edge ID as starting point.
     *
     * @param edgeID      the root Edge ID.
     * @param t           how far in root Edge the search should start from.
     * @param maxDistance the maximum distance to be transversed.
     * @return a set of Edges reachable from the root node at the
     * maximum distance.
     */
    public Set<Edge> getOutEdgesAtMaxDistanceFromEdgeID(final long edgeID, final double t, final double maxDistance) {
        // FIXME: O(n), super slow!
        Optional<Edge> edge = getEdge(edgeID);

        if (edge.isPresent()) {
            return getOutEdgesAtMaxDistanceFromEdge(edge.get(), t, maxDistance);
        } else {
            return new HashSet<>();
        }
    }

    /**
     * @param edge the edge
     * @return the out edges
     */
    public Set<Edge> getOutEdges(final Edge edge) {
        try {
            Long target = graph.incidentNodes(edge).target();
            return graph.outEdges(target);
        } catch (IllegalArgumentException e) {
            LOGGER.error(e, "Could not find out edges for edge: %s", edge);
            return new HashSet<>();
        }
    }

    /**
     * Returns the edges connected to the given Node id.
     *
     * @param nodeId the node
     * @return the edges
     */
    public Set<Edge> getConnectedEdges(final long nodeId) {
        return graph.incidentEdges(nodeId);
    }

    /**
     * Get a set of Edges with bounding boxes containing a given coordinate. This
     * method is expensive and will navigate the whole graph.
     *
     * @param coordinate the coordinate.
     * @return the set of Edges that contains the coordinate.
     */
    public Set<Edge> match(final Point coordinate) {
        return graph
                .edges()
                .stream()
                .filter(e -> e.contains(coordinate))
                .collect(Collectors.toSet());
    }

    /**
     * Get a weighted match (by distance).
     *
     * @param coordinate the coodinate to match to
     * @param limit      the max number of results
     * @return the weighted matches
     */
    public List<MatchResult> weightedMatch(final Point coordinate, final int limit) {
        return graph
                .edges()
                .stream()
                .filter(Edge::hasCenterLine)
                .map(edge -> new MatchResult(
                        pointToLineDistance(
                                coordinate,
                                edge.getCenterLine(),
                                "meters"),
                        edge))
                .sorted(Comparator.comparingDouble(MatchResult::getDistance))
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * @return all edges contained in the graph.
     */
    public Set<Edge> edges() {
        return graph.edges();
    }

    /**
     * @param id the edge id
     * @return the edge if present
     */
    public Optional<Edge> getEdge(final long id) {
        return Optional.ofNullable(edgeMap.get(id));
    }

    /**
     * Add the drivable paths to the edges contained in the graph.
     *
     * @param paths the paths
     * @// TODO: 11/11/2018 This should be done somewhere else
     */
    public void addDrivablePaths(final Collection<DrivablePath> paths) {
        paths.forEach(path -> {
            path.getEdgeIds().forEach(edgeId -> {
                getEdge(edgeId).ifPresent(edge -> {
                    edge.setDrivablePath(path);
                });
            });
        });
    }

    /**
     * @return all nodes contained in the graph.
     */
    public Set<Long> nodes() {
        return graph.nodes();
    }

    @Override
    public String toString() {
        return String.format("nodes=%d, edges=%d", graph.nodes().size(), graph.edges().size());
    }
}
