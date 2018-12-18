package com.mapbox.services.android.navigation.v5.eh.vt;

import com.mapbox.geojson.BoundingBox;
import com.mapbox.services.android.navigation.v5.eh.geo.CanonicalTileID;
import com.mapbox.services.android.navigation.v5.eh.horizon.DrivablePath;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;
import com.mapbox.services.android.navigation.v5.eh.logging.Logger;
import com.mapbox.services.android.navigation.v5.eh.osm.WayType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import static com.mapbox.services.android.navigation.v5.vectortile.VectorTile.Tile;
import static com.mapbox.services.android.navigation.v5.vectortile.VectorTile.Tile.Feature;
import static com.mapbox.services.android.navigation.v5.vectortile.VectorTile.Tile.Layer;
import static com.mapbox.services.android.navigation.v5.vectortile.VectorTile.Tile.Value;

/**
 * Parses RoadBook data encoded in the Mapbox Vector Tile
 * format into data structures that can be used in the Graph.
 */
public final class VectorTileParser {
    private static final Logger LOGGER = new Logger(VectorTileParser.class);
    private final Set<Edge> edges = new HashSet<>();
    private final Set<DrivablePath> drivablePaths = new HashSet<>();
    private CanonicalTileID tileID;

    /**
     * Creates a parsers. Needed because the parser maintains
     * state, that is reset on every parser call.
     */
    public VectorTileParser() {
    }

    /**
     * Parses a vector tile returning a set of {@link Edge}s.
     *
     * @param id   the tile id.
     * @param tile the tile buffer.
     */
    public void parseTile(final CanonicalTileID id, final Tile tile) {
        // Reset state
        this.tileID = id;
        edges.clear();
        drivablePaths.clear();

        // Parse features
        for (Layer layer : tile.getLayersList()) {
            for (Feature feature : layer.getFeaturesList()) {
                parseFeature(layer, feature);
            }
        }
    }

    /**
     * @return all drivable paths
     */
    public Collection<DrivablePath> getDrivablePaths() {
        return drivablePaths;
    }

    /**
     * @return all edges
     */
    public Collection<Edge> getEdges() {
        return edges;
    }

    private void parseFeature(final Layer layer, final Feature feature) {
        switch (layer.getName()) {
            case "drivablePaths":
                parseDrivablePath(layer, feature);
                break;
            case "edges":
                parseEdge(layer, feature);
                break;
            default:
                LOGGER.warn("Ignoring feature in layer: %s", layer.getName());
                break;
        }
    }

    private void parseDrivablePath(final Layer layer, final Feature feature) {
        List<Point> points = new ArrayList<>();
        Iterator<Integer> geom = feature.getGeometryList().iterator();

        try {
            Point.Builder cursor = Point.newBuilder().withX(0).withY(0);
            int currentCommand = geom.next();

            while (nextCommandMoveTo(currentCommand)) {
                int commandCount = getCommandCount(currentCommand);
                if (commandCount != 1) {
                    return;
                }

                points.add(toLlaPoint(layer.getExtent(), decodePoint(cursor, geom)));
                currentCommand = geom.next();

                while (nextCommandLineTo(currentCommand)) {
                    commandCount = getCommandCount(currentCommand);
                    for (int i = 0; i < commandCount; ++i) {
                        points.add(toLlaPoint(layer.getExtent(), decodePoint(cursor, geom)));
                    }
                    currentCommand = geom.next();
                }

                if (!nextCommandClosePath(currentCommand)) {
                    return;
                }

                currentCommand = geom.next();
            }
        } catch (NoSuchElementException e) {
            // Expected, just move on.
        }

        long id = feature.getId();
        long[] edgeIds = null;

        List<Integer> tags = feature.getTagsList();
        int size = tags.size();
        String name = null;
        String type = null;
        Long osmWayId = null;
        Integer osmMaxSpeed = null;

        int i = 0;
        while (i < size) {
            String key = layer.getKeys(tags.get(i++));
            Value value = layer.getValues(tags.get(i++));

            switch (key) {
                case "id":
                    id = value.getIntValue();
                    break;
                case "edge_ids":
                    if (value.hasIntValue()) {
                        // Singular edge (one-way)
                        edgeIds = new long[]{value.getIntValue()};
                    } else {
                        // Multiple edges, separated by ':'
                        String[] edgeIdsS = value.getStringValue().split(":");
                        edgeIds = new long[edgeIdsS.length];
                        for (int k = 0; k < edgeIdsS.length; k++) {
                            edgeIds[k] = Long.parseLong(edgeIdsS[k]);
                        }
                    }
                    break;
                case "name":
                    name = value.getStringValue();
                    break;
                case "type":
                    type = value.getStringValue();
                    break;
                case "destination":
                    //TODO
                    break;
                case "osm_way_id":
                    osmWayId = value.getIntValue();
                    break;
                case "osm_oneway":
                    //TODO:
                    break;
                case "maxspeed":
                    osmMaxSpeed = Math.toIntExact(value.getIntValue());
                    break;
                default:
                    LOGGER.error("Unknown property: %s", key);
                    // Should never happen
                    assert false;
                    break;
            }
        }

        // Validate
        if (edgeIds == null) {
            LOGGER.error("No edge ids for drivable path %s, osm way id: %s", id, osmWayId);
            return;
        }

        DrivablePath drivablePath = new DrivablePath(id,
                points.stream().map(
                        p -> com.mapbox.geojson.Point.fromLngLat(p.getX(), p.getY())).collect(Collectors.toList()
                ),
                osmWayId, WayType.valueOfOptional(type).orElse(null),
                name, osmMaxSpeed,
                Arrays.stream(edgeIds).boxed().collect(Collectors.toSet()));

        if (!drivablePaths.add(drivablePath)) {
            LOGGER.warn("Did not add drivable path %s to set as it already exists", drivablePath.getId());
        }
    }


    private void parseEdge(final Layer layer, final Feature feature) {
        List<Point> points = new ArrayList<>();
        Iterator<Integer> geom = feature.getGeometryList().iterator();

        // The edge bounding box is a Polygons but with only one ring.
        try {
            Point.Builder cursor = Point.newBuilder().withX(0).withY(0);
            int currentCommand = geom.next();

            while (nextCommandMoveTo(currentCommand)) {
                int commandCount = getCommandCount(currentCommand);
                if (commandCount != 1) {
                    return;
                }

                points.add(toLlaPoint(layer.getExtent(), decodePoint(cursor, geom)));
                currentCommand = geom.next();

                while (nextCommandLineTo(currentCommand)) {
                    commandCount = getCommandCount(currentCommand);
                    for (int i = 0; i < commandCount; ++i) {
                        points.add(toLlaPoint(layer.getExtent(), decodePoint(cursor, geom)));
                    }
                    currentCommand = geom.next();
                }

                if (!nextCommandClosePath(currentCommand)) {
                    return;
                }

                currentCommand = geom.next();
            }
        } catch (NoSuchElementException e) {
            // Expected, just move on.
        }

        long id = feature.getId();
        long nodeIdIn = Edge.NULL_NODE, nodeIdOut = Edge.NULL_NODE;
        double length = -1;
        long counterPartId = -1;
        boolean inverse = false;
        long osmWayId = -1;

        List<Integer> tags = feature.getTagsList();
        int size = tags.size();

        int i = 0;
        while (i < size) {
            String key = layer.getKeys(tags.get(i++));
            Value value = layer.getValues(tags.get(i++));

            switch (key) {
                case "length":
                    length = value.getDoubleValue();
                    break;
                case "node_id_in":
                    nodeIdIn = value.getIntValue();
                    break;
                case "node_id_out":
                    nodeIdOut = value.getIntValue();
                    break;
                case "id":
                    id = value.getIntValue();
                    break;
                case "counterpart_id":
                    counterPartId = value.getIntValue();
                    break;
                case "inverse_geometry":
                    inverse = value.getBoolValue();
                    break;
                case "osm_way_id":
                    osmWayId = value.getIntValue();
                    break;
                case "osm_oneway":
                    //TODO:
                    break;
                case "osm_way_type":
                    //TODO:
                    break;
                case "maxspeed":
                    //TODO:
                    break;
                case "drivable_path_id":
                    //TODO: Could be removed?
                    break;
                default:
                    LOGGER.error("Unknown property: %s", key);
                    // Should never happen
//                    assert false;
                    break;
            }
        }

        Edge edge = new Edge(id, length, nodeIdIn, nodeIdOut, counterPartId, inverse, osmWayId);

        //CHECKSTYLE:OFF MagicNumber

        edge.setBounds(
                BoundingBox.fromPoints(
                        com.mapbox.geojson.Point.fromLngLat(points.get(3).getX(), points.get(3).getY()),
                        com.mapbox.geojson.Point.fromLngLat(points.get(1).getX(), points.get(1).getY())
                )
        );

        //CHECKSTYLE:ON MagicNumber

        if (!edges.add(edge)) {
            LOGGER.warn("Did not add edge %s to set as it already exists", edge.getId());
        }
    }

    //CHECKSTYLE:OFF MagicNumber

    private Point toLlaPoint(final int extent, Point point) {
        double size = extent * Math.pow(2, tileID.getZ());
        double x0 = extent * tileID.getX();
        double y0 = extent * tileID.getY();
        double y2 = 180 - (point.getY() + y0) * 360 / size;
        return new Point(
                (point.getX() + x0) * 360 / size - 180,
                360 / Math.PI * Math.atan(Math.exp(y2 * Math.PI / 180)) - 90
        );
    }

    //
    // Geometry ZigZag decoding functions.
    //
    // https://github.com/mapbox/vector-tile-base
    //

    private static Point decodePoint(final Point.Builder cursor, final Iterator<Integer> data) {

        cursor.withX(cursor.getX() + zigZagDecode(data.next()));
        cursor.withY(cursor.getY() + zigZagDecode(data.next()));

        return cursor.build();
    }

    private static int zigZagDecode(final int val) {
        return ((val >> 1) ^ (-(val & 1)));
    }

    private static boolean nextCommandMoveTo(final int command) {
        return getCommandID(command) == 1;
    }

    private static boolean nextCommandLineTo(final int command) {
        return getCommandID(command) == 2;
    }

    private static boolean nextCommandClosePath(final int command) {
        return getCommandID(command) == 7;
    }

    private static int getCommandID(final int command) {
        return command & 0x7;
    }

    private static int getCommandCount(final int command) {
        return command >> 3;
    }

    //CHECKSTYLE:ON MagicNumber
}
