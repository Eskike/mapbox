package com.mapbox.services.android.navigation.v5.eh.horizon;

import com.mapbox.services.android.navigation.v5.eh.geo.BoundingBoxUtils;
import com.mapbox.services.android.navigation.v5.eh.osm.WayType;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * The RoadBook Edge.
 */
public class Edge {
    /**
     * Null node ID, when the Edge doesn't have a node.
     */
    public static final int NULL_NODE = -1;

    private final long id;
    private final double length;
    private final long nodeIDIn;
    private final long nodeIDOut;
    private final long counterpartId;
    private final boolean inverse;
    private final long osmWayId;

    // Copied from DrivablePath
    private int osmMaxSpeed;
    private WayType osmWayType;
    private LineString centerLine;

    private BoundingBox bounds;

    /**
     * Construct an Edge with a given unique ID.
     *
     * @param id        the unique ID.
     * @param length    length in meters of the Edge.
     * @param nodeIDIn  the inbound node ID.
     * @param nodeIDOut the outbound node ID.
     */
    public Edge(final long id, final double length, final long nodeIDIn, final long nodeIDOut) {
        this(id, length, nodeIDIn, nodeIDOut, -1, false, -1, null);
    }

    /**
     * Construct an Edge with a given unique ID.
     *
     * @param id            the unique ID.
     * @param length        length in meters of the Edge.
     * @param nodeIDIn      the inbound node ID.
     * @param nodeIDOut     the outbound node ID.
     * @param counterpartId the edge's counterpart
     * @param inverse       inversed from geometry
     * @param osmWayId      the osm way id
     */
    public Edge(final long id, final double length, final long nodeIDIn, final long nodeIDOut,
                final long counterpartId, final boolean inverse, final long osmWayId) {
        this(id, length, nodeIDIn, nodeIDOut, counterpartId, inverse, osmWayId, null);
    }

    /**
     * Constructs an edge.
     *
     * @param id            the unique ID.
     * @param length        length in meters of the Edge.
     * @param nodeIDIn      the inbound node ID.
     * @param nodeIDOut     the outbound node ID.
     * @param counterpartId the edge's counterpart
     * @param inverse       edge is inverted
     * @param osmWayId      the osm way id
     * @param bounds        the bounding box
     */
    public Edge(final long id, final double length, final long nodeIDIn,
                final long nodeIDOut, final long counterpartId, final boolean inverse,
                final long osmWayId, final BoundingBox bounds) {
        this.osmWayId = osmWayId;
        assert length > 0;
        this.id = id;
        this.length = length;
        this.nodeIDIn = nodeIDIn;
        this.nodeIDOut = nodeIDOut;
        this.counterpartId = counterpartId;
        this.inverse = inverse;
        this.bounds = bounds;
    }

    private Edge(final Builder builder) {
        id = builder.id;
        length = builder.length;
        nodeIDIn = builder.nodeIDIn;
        nodeIDOut = builder.nodeIDOut;
        counterpartId = builder.counterpartId;
        inverse = builder.inverse;
        osmWayId = builder.osmWayId;
        bounds = builder.bounds;
        osmMaxSpeed = builder.osmMaxSpeed;
        centerLine = builder.centerLine;
        osmWayType = builder.osmWayType;
    }

    /**
     * @return the unique ID.
     */
    public long getId() {
        return id;
    }

    /**
     * @return the length in meters of the Edge.
     */
    public double getLength() {
        return length;
    }

    /**
     * @return the inbound node ID.
     */
    public long getNodeIDIn() {
        return nodeIDIn;
    }

    /**
     * @return the outbound node ID.
     */
    public long getNodeIDOut() {
        return nodeIDOut;
    }

    /**
     * @return the osm way id
     */
    public long getOsmWayId() {
        return osmWayId;
    }

    /**
     * @return the bounding box geometry of the edge.
     */
    public BoundingBox getBounds() {
        return bounds;
    }

    /**
     * Set the bounding box geometry of the edge.
     *
     * @param bounds the point list of the geometry.
     */
    public void setBounds(final BoundingBox bounds) {
        this.bounds = bounds;
    }

    /**
     * Check if a coordinate is inside an Edge. Useful for map-matching.
     *
     * @param point the coordinate.
     * @return true if contained, false otherwise.
     */
    public boolean contains(final Point point) {
        return BoundingBoxUtils.contains(bounds, point);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Edge edge = (Edge) o;

        return id == edge.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "id=%d, t=%.5f, osm_id=%d, in=%s, out=%s", id, length, osmWayId, nodeIDIn, nodeIDOut
        );
    }

    /**
     * Set the associated drivable path.
     *
     * @param drivablePath the drivable path
     */
    public void setDrivablePath(final DrivablePath drivablePath) {
        // Update center line geometry
        if (drivablePath != null) {

            List<Point> points = new LinkedList<>(drivablePath.getPoints());

            if (isInverse()) {
                Collections.reverse(points);
            }

            centerLine = LineString.fromLngLats(points);


            // Update other redundant properties
            osmMaxSpeed = drivablePath.getOsmMaxSpeed();
            osmWayType = drivablePath.getType();
        } else {
            centerLine = null;
            osmMaxSpeed = -1;
            osmWayType = null;
        }
    }

    /**
     * @return true if this is the inverse of the associated drivable path's direction
     */
    public boolean isInverse() {
        return inverse;
    }

    /**
     * @return the center line of the edge.
     */
    public LineString getCenterLine() {
        return centerLine;
    }

    /**
     * @return true if a center line is present
     */
    public boolean hasCenterLine() {
        return centerLine != null;
    }

    /**
     * @return the edge id of the counter part, if any
     */
    public long getCounterpartId() {
        return counterpartId;
    }

    /**
     * @return the osm max speed
     */
    public int getOsmMaxSpeed() {
        return osmMaxSpeed;
    }

    /**
     * @return the {@link WayType}
     */
    public WayType getOsmWayType() {
        return osmWayType != null ? osmWayType : WayType.UNKNOWN;
    }

    /**
     * @return new, empty builder
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * @param copy to copy
     * @return pre-filled builder
     */
    public static Builder newBuilder(final Edge copy) {
        Builder builder = new Builder();
        builder.id = copy.getId();
        builder.length = copy.getLength();
        builder.nodeIDIn = copy.getNodeIDIn();
        builder.nodeIDOut = copy.getNodeIDOut();
        builder.counterpartId = copy.getCounterpartId();
        builder.inverse = copy.isInverse();

        builder.osmWayId = copy.getOsmWayId();
        builder.osmMaxSpeed = copy.osmMaxSpeed;
        builder.osmWayType = copy.osmWayType;

        builder.centerLine = LineString.fromLngLats(new ArrayList<>(copy.getCenterLine().coordinates()));
        builder.bounds = BoundingBox.fromPoints(copy.getBounds().southwest(), copy.bounds.northeast());

        return builder;
    }

    /**
     * {@code Edge} builder static inner class.
     */
    public static final class Builder {
        private long id;
        private double length;
        private long nodeIDIn;
        private long nodeIDOut;
        private long counterpartId;
        private boolean inverse;
        private long osmWayId;
        private int osmMaxSpeed;
        private LineString centerLine;
        private BoundingBox bounds;
        private WayType osmWayType;

        private Builder() {
        }

        /**
         * Sets the {@code id} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this Builder
         */
        public Builder withId(final long id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code length} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param length the {@code length} to set
         * @return a reference to this Builder
         */
        public Builder withLength(final double length) {
            this.length = length;
            return this;
        }

        /**
         * Sets the {@code nodeIDIn} and returns a reference to this
         * Builder so that the methods can be chained together.
         *
         * @param nodeIDIn the {@code nodeIDIn} to set
         * @return a reference to this Builder
         */
        public Builder withNodeIDIn(final long nodeIDIn) {
            this.nodeIDIn = nodeIDIn;
            return this;
        }

        /**
         * Sets the {@code nodeIDOut} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param nodeIDOut the {@code nodeIDOut} to set
         * @return a reference to this Builder
         */
        public Builder withNodeIDOut(final long nodeIDOut) {
            this.nodeIDOut = nodeIDOut;
            return this;
        }

        /**
         * Sets the {@code counterpartId} and returns a reference to this
         * Builder so that the methods can be chained together.
         *
         * @param counterpartId the {@code counterpartId} to set
         * @return a reference to this Builder
         */
        public Builder withCounterpartId(final long counterpartId) {
            this.counterpartId = counterpartId;
            return this;
        }

        /**
         * Sets the {@code inverse} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param inverse the {@code inverse} to set
         * @return a reference to this Builder
         */
        public Builder withInverse(final boolean inverse) {
            this.inverse = inverse;
            return this;
        }

        /**
         * Sets the {@code osmWayId} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param osmWayId the {@code osmWayId} to set
         * @return a reference to this Builder
         */
        public Builder withOsmWayId(final long osmWayId) {
            this.osmWayId = osmWayId;
            return this;
        }

        /**
         * Sets the {@code osmWayType} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param osmWayType the {@code osmWayType} to set
         * @return a reference to this Builder
         */
        public Builder withOsmWayType(final WayType osmWayType) {
            this.osmWayType = osmWayType;
            return this;
        }

        /**
         * Sets the {@code centerLine} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param centerLine the {@code centerLine} to set
         * @return a reference to this Builder
         */
        public Builder withCenterLine(final LineString centerLine) {
            this.centerLine = centerLine;
            return this;
        }

        /**
         * Sets the {@code bounds} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param bounds the {@code bounds} to set
         * @return a reference to this Builder
         */
        public Builder withBounds(final BoundingBox bounds) {
            this.bounds = bounds;
            return this;
        }

        /**
         * Sets the {@code osmMaxSpeed} and returns a reference to this Builder
         * so that the methods can be chained together.
         *
         * @param osmMaxSpeed the {@code osmMaxSpeed} to set
         * @return a reference to this Builder
         */
        public Builder withOsmMaxSpeed(final int osmMaxSpeed) {
            this.osmMaxSpeed = osmMaxSpeed;
            return this;
        }

        /**
         * Returns a {@code Edge} built from the parameters previously set.
         *
         * @return a {@code Edge} built with parameters of this {@code Edge.Builder}
         */
        public Edge build() {
            return new Edge(this);
        }
    }
}
