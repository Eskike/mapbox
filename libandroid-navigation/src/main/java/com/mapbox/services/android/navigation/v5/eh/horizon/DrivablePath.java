package com.mapbox.services.android.navigation.v5.eh.horizon;


import com.mapbox.services.android.navigation.v5.eh.osm.WayType;
import com.mapbox.geojson.Point;

import java.util.List;
import java.util.Set;

/**
 * Drivable path. Shared between inverse edges.
 */
public class DrivablePath {
    private final long id;
    private final Set<Long> edgeIds;
    private final List<Point> points;
    private final String name;
    private final WayType type;
    private final long osmWayId;
    private final int osmMaxSpeed;

    /**
     * Create a new DrivablePath.
     *
     * @param id          path id
     * @param points      the points
     * @param osmWayId    the osm way id
     * @param type        the osm highway type
     * @param name        the name, if any
     * @param osmMaxSpeed osm max speed
     * @param edgeIds     the associated edges
     */
    public DrivablePath(final long id, final List<Point> points,
                        final long osmWayId, final WayType type, final String name,
                        final int osmMaxSpeed, final Set<Long> edgeIds) {
        this.id = id;
        this.points = points;
        this.osmWayId = osmWayId;
        this.type = type;
        this.name = name;
        this.osmMaxSpeed = osmMaxSpeed;
        this.edgeIds = edgeIds;
    }

    /**
     * @return the linestring points
     */
    public List<Point> getPoints() {
        return points;
    }

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @return the osm way id
     */
    public long getOsmWayId() {
        return osmWayId;
    }

    /**
     * @return the osm max speed
     */
    public int getOsmMaxSpeed() {
        return osmMaxSpeed;
    }

    /**
     * @return the way name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the osm highway type
     */
    public WayType getType() {
        return type;
    }

    /**
     * @return associated edges
     */
    public Set<Long> getEdgeIds() {
        return edgeIds;
    }

    /**
     * Add an edge association.
     *
     * @param edgeId the edge id to associate
     */
    public void addEdge(final Long edgeId) {
        this.edgeIds.add(edgeId);
    }
}
