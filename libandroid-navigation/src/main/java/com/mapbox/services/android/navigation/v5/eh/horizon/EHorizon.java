package com.mapbox.services.android.navigation.v5.eh.horizon;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Electronic Horizon.
 */
public class EHorizon {

    /**
     * A node in the {@link EHorizon}.
     */
    public static class Node {
        private final Segment segment;
        private final double probability;
        private final double bearingDiff;

        /**
         * Create a node.
         *
         * @param segment     the contained segement
         * @param probability the probability
         * @param bearingDiff the bearing difference
         */
        public Node(final Segment segment, final double probability, final double bearingDiff) {
            this.segment = segment;
            this.probability = probability;
            this.bearingDiff = bearingDiff;
        }

        /**
         * @return the Segment
         */
        public Segment segment() {
            return segment;
        }

        /**
         * @return the probability
         */
        public double probability() {
            return probability;
        }

        /**
         * @return the bearing difference
         */
        public double bearingDifference() {
            return bearingDiff;
        }
    }

    /**
     * A Segment in the {@link EHorizon}.
     */
    public static class Segment {
        private final Edge edge;
        private final List<Node> out = new ArrayList<>();

        /**
         * Create a Segment.
         *
         * @param edge the contained edge
         */
        public Segment(final Edge edge) {
            this.edge = edge;
        }

        /**
         * @return the edge
         */
        public Edge edge() {
            return edge;
        }

        /**
         * @return the out nodes
         */
        public List<Node> out() {
            return out;
        }
    }

    private final Segment start;

    /**
     * Create a new {@link EHorizon}.
     *
     * @param start the possible paths
     */
    public EHorizon(final Segment start) {
        this.start = start;
    }

    /**
     * @return the current edge
     */
    public Edge current() {
        return start.edge;
    }

    /**
     * @return the start of the horizon
     */
    public Segment start() {
        return start;
    }

    /**
     * @return the most probable path
     */
    public List<Edge> mostProbablePath() {
        List<Edge> mpp = new ArrayList<>();

        Segment current = start;
        while (current != null) {
            // Prevent cycles?
            mpp.add(current.edge);
            current = current.out
                    .stream()
                    .max(Comparator.comparingDouble(o -> o.probability))
                    .map(n -> n.segment)
                    .orElse(null);
        }


        return mpp;
    }
}
