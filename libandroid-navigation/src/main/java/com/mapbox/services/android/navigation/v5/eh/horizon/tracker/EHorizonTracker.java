package com.mapbox.services.android.navigation.v5.eh.horizon.tracker;

import com.mapbox.services.android.navigation.v5.eh.cheapruler.CheapRuler;
import com.mapbox.services.android.navigation.v5.eh.horizon.EHorizon;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;
import com.mapbox.services.android.navigation.v5.eh.horizon.graph.Graph;
import com.mapbox.services.android.navigation.v5.eh.horizon.graph.MatchResult;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.cost.CostFunction;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.cost.CumulativeCostFunction;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Match;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Matcher;
import com.mapbox.services.android.navigation.v5.eh.logging.Logger;
import com.mapbox.services.android.navigation.v5.eh.osm.WayType;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mapbox.services.android.navigation.v5.eh.geo.Bearing.HALF_ROTATION;
import static com.mapbox.services.android.navigation.v5.eh.geo.Bearing.RIGHT_ANGLE;
import static com.mapbox.services.android.navigation.v5.eh.geo.Bearing.diff;

/**
 * Tracker for the Ehorizon. Takes location history and edge history into account
 * when determining the new matched location, mpp and horizon.
 */
public class EHorizonTracker {
    private static final Logger LOGGER = new Logger(EHorizonTracker.class);

    private static final int MATCH_LIMIT = 3;

    private static final int DEFAULT_HORIZON_DISTANCE = 400;
    private static final int IMPLICIT_RESET_THRESHOLD = 100;

    // State
    private CheapRuler ruler;
    private Graph graph;
    private Point previousPosition;
    private Edge edge;

    // Configuration
    private int maxDistanceT = DEFAULT_HORIZON_DISTANCE;
    private boolean fullExpansion = false;

    /**
     * Calculate the new horizon. Graph must be set.
     *
     * @param newPosition the new, raw position
     * @return the {@link TrackerResult}
     */
    public TrackerResult horizon(final Point newPosition) {
        // Update the ruler
        ruler = CheapRuler.forLatitude(newPosition.latitude(), CheapRuler.Unit.METERS);

        if (previousPosition != null && ruler.distance(previousPosition, newPosition) > IMPLICIT_RESET_THRESHOLD) {
            LOGGER.info("Reset position");
            previousPosition = null;
            edge = null;
        }

        TrackerResult result;
        if (graph == null || (edge == null && previousPosition == null) || previousPosition == newPosition) {
            // Can't do anything yet
            LOGGER.info("Cannot determine EHorizon yet, returning raw position");
            result = new NegativeTrackerResult(newPosition);
        } else {
            result = match(newPosition);

            if (result instanceof PositiveTrackerResult) {
                LOGGER.info("Result Positive - Position: %s, Edge: %s", result.location().coordinates(),
                        ((PositiveTrackerResult) result).horizon().current());
            } else {
                LOGGER.info("Result Negative - Position: %s", result.location().coordinates());
            }
        }

        // Finally set previous newPosition
        previousPosition = newPosition;

        return result;
    }

    /**
     * Set the current edge graph.
     *
     * @param graph the graph.
     */
    public void graph(final Graph graph) {
        this.graph = graph;
    }

    /**
     * Switch full graph expansion on or off.
     *
     * @param enable full expansion if true
     */
    public void fullGraphExpansion(final boolean enable) {
        this.fullExpansion = enable;
    }

    /**
     * @return if full expansion is used for the horizon
     */
    public boolean fullGraphExpansion() {
        return this.fullExpansion;
    }

    /**
     * Set the horizon distance to calculate.
     *
     * @param distance the distance in meters
     */
    public void horizonDistance(final int distance) {
        this.maxDistanceT = distance;
    }

    /**
     * @return the current horizon distance in m
     */
    public int horizonDistance() {
        return maxDistanceT;
    }

    TrackerResult match(final Point newPosition) {
        if (edge != null) {
            LOGGER.debug("Checking previous edge %s", edge);
            // Check current edge is still valid and whether the most likely next edge
            // is a better match
            List<Match> candidates = graph
                    .getOutEdges(edge)
                    .stream()
                    // Make sure we don't flip around
                    .filter(e -> e.getId() != edge.getCounterpartId())
                    // Base weight for MPP on direct match || succession probability
                    .map(e -> Matcher.forEdgeAndLocation(ruler, edge, e, previousPosition, newPosition)
                    )
                    .collect(Collectors.toList());
            candidates.add(Matcher.forEdgeAndLocation(ruler, edge, edge, previousPosition, newPosition));

            Optional<Match> mostLikelyMatch = candidates
                    .stream()
                    .filter(Match::matches)
                    .min(Comparator.comparingDouble(Match::cost));

            if (mostLikelyMatch.isPresent()) {
                Match match = mostLikelyMatch.get();
                LOGGER.debug("Found most likely match for edge %s: %s (position: %s)",
                        edge,
                        match.edge(),
                        newPosition.coordinates());


                edge = match.edge();
                return new PositiveTrackerResult(
                        match.pointOnLine().point(),
                        horizon(
                                match.edge(),
                                ruler.distanceAlong(
                                        match.edge().getCenterLine().coordinates(),
                                        match.pointOnLine()
                                )
                        )
                );
            }
        }

        // Always fall back to horizon from location history
        return findHorizonFromLocationHistory(newPosition);
    }

    TrackerResult findHorizonFromLocationHistory(final Point newPosition) {
        assert (previousPosition != null);
        double bearing = ruler.bearing(previousPosition, newPosition);

        Optional<Match> possibleMatch = graph
                // Find initial matches based on the new location in the graph
                .weightedMatch(newPosition, MATCH_LIMIT)
                .stream()
                .map(MatchResult::getEdge)
                // Map to result
                .map(e -> Matcher.forLocation(ruler, e, newPosition, bearing))
                // Filter bearing with certain error threshold
                .filter(Match::matches)
                // Get the minimal cost
                .min(Comparator.comparingDouble(Match::cost));

        if (possibleMatch.isPresent()) {
            Match match = possibleMatch.get();
            LOGGER.debug("Found a horizon for coordinates %s and %s: %s (at: %s)",
                    previousPosition.coordinates(), newPosition.coordinates(),
                    match.edge(), match.pointOnLine().point().coordinates());

            // TODO Store matched point as previous location?
            edge = match.edge();

            return new PositiveTrackerResult(
                    match.pointOnLine().point(),
                    horizon(
                            match.edge(),
                            ruler.distanceAlong(match.edge().getCenterLine().coordinates(), match.pointOnLine())
                    )
            );
        } else {
            // No match on points
            LOGGER.warn("Could not find a horizon for coordinates %s and %s", previousPosition, newPosition);
            return new NegativeTrackerResult(newPosition);
        }
    }

    /**
     * Create a EHorizon for the current state and configuration.
     *
     * @param current the starting Edge
     * @param t       the t in meters to start on this Edge
     * @return the EHorizon
     */
    EHorizon horizon(final Edge current, final double t) {
        // Collect visited edges to maintain a a-clyclic graph in the horizon
        Set<Long> visitedEdges = new HashSet<>();
        visitedEdges.add(current.getId());

        // Clip the start (front and possibly back)
        Edge clippedEdge = clip(current, t, t + maxDistanceT);

        if (clippedEdge.getLength() < maxDistanceT) {
            // Expand on the start edge
            return new EHorizon(
                    expand(new EHorizon.Segment(clippedEdge), clippedEdge.getLength(), visitedEdges, fullExpansion)
            );
        } else {
            // One is enough
            return new EHorizon(new EHorizon.Segment(clippedEdge));
        }
    }

    /**
     * Expands a Segment with (snipped) out nodes.
     *
     * @param current       the current Segment
     * @param t             the total t up to here (including current.length)
     * @param visitedEdges  the edges already visited and should not be re-visited
     * @param fullExpansion if true we get a full christmas tree
     * @return the expanded segment
     */
    EHorizon.Segment expand(final EHorizon.Segment current, final double t,
                            final Set<Long> visitedEdges, final boolean fullExpansion) {
        Set<Edge> outEdges = graph.getOutEdges(current.edge())
                .stream()
                // Prevent cyclic graphs
                .filter(e -> !visitedEdges.contains(e.getId()))
                // Filter u-turns
                .filter(e -> current.edge().getId() != e.getCounterpartId())
                // Clip max length
                .map(e -> clip(e, 0, maxDistanceT - t))
                // Re-measure edge
                .map(e -> Edge.newBuilder(e).withLength(ruler.lineDistance(e.getCenterLine().coordinates())).build())
                // Ensure we have at least something to show
                .filter(e -> e.getLength() > 1)
                // TODO: Clipping should lead to valid line strings...
                .filter(e -> e.getCenterLine().coordinates().size() >= 2)
                .collect(Collectors.toSet());

        // Update collection of visited edges
        visitedEdges.addAll(outEdges.stream().map(Edge::getId).collect(Collectors.toList()));

        // Add out edges as nodes
        addNodes(current, outEdges);


        if (fullExpansion) {
            // Expand all paths
            current.out()
                    .forEach(n -> expand(n.segment(), t + n.segment().edge().getLength(), visitedEdges, true));
        } else {
            // Expand mpp
            current.out().stream()
                    .max(Comparator.comparingDouble(EHorizon.Node::probability))
                    .ifPresent(n -> expand(n.segment(), t + n.segment().edge().getLength(), visitedEdges, false));
        }

        return current;
    }

    /**
     * Adds the edges as out nodes to the given segement.
     *
     * @param in  the segment
     * @param out the edges to add as out nodes
     */
    void addNodes(final EHorizon.Segment in, final Collection<Edge> out) {
        // Determine probabilities
        List<Edge> edges = new ArrayList<>(out);
        List<Double> costs = edges
                .stream()
                .map(e -> cost(in.edge(), e))
                .collect(Collectors.toList());

        LOGGER.debug("Costs: %s", Arrays.toString(costs.toArray()));

        // Normalise
        List<Double> probabilities = normalizedProbabilities(
                shiftToPositiveRange(costs)
        );

        LOGGER.debug("Probabilities normalised: %s", Arrays.toString(probabilities.toArray()));

        // Create nodes and add to segment
        for (int i = 0; i < edges.size(); i++) {
            Edge edgeOut = edges.get(i);
            in.out().add(
                    new EHorizon.Node(
                            new EHorizon.Segment(edgeOut),
                            probabilities.get(i),
                            bearingDiff(in.edge(), edgeOut, false)
                    )
            );
        }
    }

    List<Double> shiftToPositiveRange(final List<Double> costs) {
        if (costs.size() < 2) {
            return costs;
        }

        double minCost = costs.stream().mapToDouble(p -> p).min().getAsDouble();
        double lowerBounds = Math.max(1, 1 - minCost);
        return costs.stream()
                .map(p -> lowerBounds + p)
                .collect(Collectors.toList());
    }

    /**
     * Calculates the normalised probabilities from a set of costs.
     *
     * @param costs the costs
     * @return probabilities, each in range (0,1]
     */
    List<Double> normalizedProbabilities(final List<Double> costs) {
        // Guard against empty lists
        if (costs.size() == 0) {
            return Collections.emptyList();
        }

        // A single item always has max probability
        if (costs.size() == 1) {
            return Collections.singletonList(1.);
        }

        // If the max and min values are 0 everything has an equal
        // probability
        double minCost = costs.stream().mapToDouble(p -> p).min().getAsDouble();
        double maxCost = costs.stream().mapToDouble(p -> p).max().getAsDouble();
        if (minCost == maxCost) {
            double cost = 1. / costs.size();
            return costs.stream().map(c -> cost).collect(Collectors.toList());
        }

        List<Double> probabilities = costs.stream()
                .map(p -> p == 0 ? 1 : 1 / p)
                .collect(Collectors.toList());


        double sum = probabilities.stream().mapToDouble(x -> x).sum();

        return probabilities.stream()
                .map(x -> x / sum)
                .collect(Collectors.toList());
    }

    /**
     * Determines a cost from in -> out.
     *
     * @param in  the start edge
     * @param out the out edge
     * @return a number [1, x)
     */
    //CHECKSTYLE:OFF MagicNumber
    double cost(final Edge in, final Edge out) {
        // Bearing change
        double bearingDiff = bearingDiff(in, out, true);

        // Basic costs
        final CumulativeCostFunction cost = new CumulativeCostFunction()
                // Nothing is free
                .withCost(1, () -> 1)
                .withCost(1, wayContinuationCost(in, out))
                .withCost(4, serviceRoadsCost(in, out));

        // Add way-type dependent costs
        switch (in.getOsmWayType()) {
            case MOTORWAY:
            case TRUNK:
                cost
                        .withCost(4, bearingCost(bearingDiff))
                        .withCost(10, wayClassCost(in, out));
                break;
            case PRIMARY:
            case SECONDARY:
                cost
                        .withCost(3, bearingCost(bearingDiff))
                        .withCost(40, highBearingChangeCost(bearingDiff, 120))
                        .withCost(4, wayClassCost(in, out));
                break;
            default:
                cost
                        .withCost(3, bearingCost(bearingDiff))
                        .withCost(15, highBearingChangeCost(bearingDiff, 120))
                        .withCost(4, wayClassCost(in, out));
        }

        if (LOGGER.isTraceEnabled()) {
            LOGGER.trace("Cost:%s", in, out, cost.toString());
        }

        return cost.cost();
    }
    //CHECKSTYLE:ON MagicNumber

    /**
     * Add an binary cost function for not continuing the current OSM way.
     */
    private CostFunction wayContinuationCost(final Edge in, final Edge out) {
        return () -> !(in.getOsmWayId() == out.getOsmWayId() && in.getCounterpartId() != out.getId()) ? 1 : 0;
    }

    /**
     * A linear cost function for the bearing change.
     */
    CostFunction bearingCost(final double bearingDiff) {
        return () -> bearingDiff / HALF_ROTATION;
    }

    /**
     * Add an higher cost for bearing changes nearing U-turn.
     */
    CostFunction highBearingChangeCost(final double bearingDiff, final double startAngle) {
        return () -> bearingDiff > startAngle ? (bearingDiff) / RIGHT_ANGLE : 0;
    }

    /**
     * A linear cost function for roadclass changes (prefer upgrading, same is neutral).
     */
    CostFunction wayClassCost(final Edge in, final Edge out) {
        return () -> -(in.getOsmWayType().getOrder() - out.getOsmWayType().getOrder());
    }

    /**
     * Service roads are rarely used.
     */
    CostFunction serviceRoadsCost(final Edge in, final Edge out) {
        return () -> !WayType.SERVICE.equals(in.getOsmWayType()) && WayType.SERVICE.equals(out.getOsmWayType()) ? 1 : 0;
    }


    double bearingDiff(final Edge in, final Edge out, final boolean abs) {
        List<Point> lineIn = in.getCenterLine().coordinates();
        List<Point> lineOut = out.getCenterLine().coordinates();
        return diff(
                // End of the in-line
                ruler.bearing(lineIn.get(lineIn.size() - 2), lineIn.get(lineIn.size() - 1)),
                // Start of the out-line
                ruler.bearing(lineOut.get(0), lineOut.get(1)),
                // Absolute
                abs
        );
    }

    /**
     * Possibly clip and edge. If clipping occurs, a copy is returned.
     *
     * @param edge   the edge (remains intact always)
     * @param tStart where to start clipping
     * @param tEnd   where to end clipping
     * @return the edge or a clipped copy
     */
    Edge clip(final Edge edge, final double tStart, final double tEnd) {
        if (tStart <= 0 && tEnd >= edge.getLength()) {
            return edge;
        }

        // Make sure we end at the minimum
        double end = Math.min(tEnd, edge.getLength());

        // Snip the start
        LineString clipped = LineString.fromLngLats(
                ruler.lineSliceAlong(tStart, end, edge.getCenterLine().coordinates())
        );
        return Edge.newBuilder(edge)
                .withCenterLine(clipped)
                .withLength(ruler.lineDistance(clipped.coordinates()))
                .build();
    }
}
