package com.mapbox.services.android.navigation.v5.eh;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.mapbox.geojson.BoundingBox;
import com.mapbox.services.android.navigation.v5.eh.cheapruler.CheapRuler;
import com.mapbox.services.android.navigation.v5.eh.geo.CanonicalTileID;
import com.mapbox.services.android.navigation.v5.eh.geo.TileCover;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;
import com.mapbox.services.android.navigation.v5.eh.horizon.graph.Graph;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.EHorizonTracker;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.PositiveTrackerResult;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.TrackerResult;
import com.mapbox.services.android.navigation.v5.eh.logging.Logger;
import com.mapbox.services.android.navigation.v5.eh.loop.Looper;
import com.mapbox.services.android.navigation.v5.eh.loop.StartupLatch;
import com.mapbox.services.android.navigation.v5.eh.storage.AsyncRequest;
import com.mapbox.services.android.navigation.v5.eh.storage.DefaultFileSource;
import com.mapbox.services.android.navigation.v5.eh.storage.FileSource;
import com.mapbox.services.android.navigation.v5.eh.storage.Resource;
import com.mapbox.services.android.navigation.v5.eh.storage.Response;
import com.mapbox.services.android.navigation.v5.eh.utils.Debouncer;
import com.mapbox.services.android.navigation.v5.eh.vt.VectorTileParser;
import com.mapbox.services.android.navigation.v5.eh.vectortile.VectorTile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main interface for the MapEngine.
 */
public class MapEngineImpl implements MapEngine, MapEngineDebug {
    private static final Logger LOGGER = new Logger(MapEngineImpl.class);
    private static final int DEFAULT_STATE_UPDATE_INTERVAL = 200;

    // Endpoint configuration
    private final String endpoint;
    private final String token;
    private final int zoom;

    private FileSource fileSource = new DefaultFileSource();
    private final Map<CanonicalTileID, AsyncRequest> requests = new HashMap<>();

    private final Multimap<CanonicalTileID, Edge> edges = ArrayListMultimap.create();
    private final Graph graph = new Graph();

    private final Set<EHorizonListener> horizonListeners = new HashSet<>();
    private final Executor threadPool = Executors.newFixedThreadPool(4);
    private int horizonDistance;

    // Current vehicle state
    private MapEngine.State state;

    // Horizon tracker
    private final EHorizonTracker tracker = new EHorizonTracker();

    private final Debouncer<MapEngine.State> stateUpdateDebouncer;
    private Looper looper;

    private void doUpdateState(final State newState) {
        this.state = newState;

        LOGGER.debug("Updating state to position %s", this.state.getPosition());

        // Determine the area we want to cover
        BoundingBox area = CheapRuler.forLatitude(this.state.getPosition().latitude(),
                CheapRuler.Unit.METERS).bufferPoint(this.state.getPosition(), this.horizonDistance);

        // Calculate tileCover
        Set<CanonicalTileID> canonicalTileIDS = TileCover.get(area, zoom);

        // Start loading
        for (CanonicalTileID tileID : canonicalTileIDS) {
            // Don't re-request state tile
            if (requests.keySet().contains(tileID)) {
                LOGGER.info("Already requested tile %s", tileID);
                continue;
            }

            // Don't have it yet, request
            requests.put(
                    tileID,
                    fileSource.request(
                            new Resource(Resource.Kind.Tile, resolveURL(tileID)),
                            response -> looper.post(() -> handleTileResponse(tileID, response))
                    )
            );
        }

        // Cancel outstanding requests no longer in the tile cover
        cleanupOutstandingRequests(canonicalTileIDS);

        // Remove graph edges that are no longer referenced
        cleanupGraph(canonicalTileIDS);

        // Update listeners
        updateHorizonListeners();
    }

    private void updateHorizonListeners() {
        LOGGER.debug("Updating EHorizon listeners (%s)", horizonListeners.size());

        if (horizonListeners.size() == 0) {
            return;
        }

        // Get a horizon from the tracker
        TrackerResult trackerResult = tracker.horizon(state.getPosition());

        if (trackerResult instanceof PositiveTrackerResult) {
            PositiveTrackerResult matched = (PositiveTrackerResult) trackerResult;

            for (EHorizonListener listener : horizonListeners) {
                listener.onUpdate(
                        new EHorizonListener.MatchedUpdate(
                                trackerResult.location(),
                                matched.horizon(),
                                requests.keySet())
                );
            }
        } else {
            // TODO: make this more useful
            for (EHorizonListener listener : horizonListeners) {
                listener.onUpdate(
                        new EHorizonListener.UnMatchedUpdate(
                                trackerResult.location(),
                                new ArrayList<>(),
                                requests.keySet())
                );
            }
        }
    }

    private void cleanupOutstandingRequests(final Set<CanonicalTileID> newCanonicalTileIDS) {
        Iterator<Map.Entry<CanonicalTileID, AsyncRequest>> iterator = requests.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<CanonicalTileID, AsyncRequest> entry = iterator.next();
            if (!newCanonicalTileIDS.contains(entry.getKey())) {
                LOGGER.info("Removing request for tile: %s", entry.getKey());
                entry.getValue().cancel();
                iterator.remove();
            }
        }
    }

    private void cleanupGraph(final Set<CanonicalTileID> newCanonicalTileIDS) {
        int edgesBefore = edges.entries().size();

        Iterator<Map.Entry<CanonicalTileID, Edge>> iterator = edges.entries().iterator();

        Set<Edge> edgesToRetain = new HashSet<>();
        Set<Edge> edgesToRemove = new HashSet<>();
        while (iterator.hasNext()) {
            Map.Entry<CanonicalTileID, Edge> entry = iterator.next();
            if (!newCanonicalTileIDS.contains(entry.getKey())) {
                edgesToRemove.add(entry.getValue());
                iterator.remove();
            } else {
                edgesToRetain.add(entry.getValue());
            }
        }

        edgesToRemove
                .stream()
                .filter(edge -> !edgesToRetain.contains(edge))
                .forEach(edge -> {
                    //LOGGER.info("Removing edge %s", edge.getId());
                    try {
                        graph.removeEdge(edge);
                    } catch (Exception e) {
                        LOGGER.warn("Cannot remove edge %s: %s", edge.getId(), e.getMessage());
                    }
                });

        LOGGER.info("Removed %s edges. Total edges: %s", edgesBefore - edges.entries().size(), edges.entries().size());
    }

    private void handleTileResponse(final CanonicalTileID tileID, final Response response) {
        if (response.getError() != null) {
            LOGGER.error("Failed to load tile %s: %s", tileID, response.getError());
            // TODO: retry
            requests.remove(tileID);
            return;
        }

        threadPool.execute(() -> {
            // Decode the tile
            VectorTile.Tile tile;
            try {
                tile = VectorTile.Tile.parseFrom(response.getInputStream());
            } catch (IOException e) {
                LOGGER.error(e, "Could not decode tile: %s", tileID);
                return;
            }

            // Parse the roadbook data
            VectorTileParser parser = new VectorTileParser();
            parser.parseTile(tileID, tile);

            // Post the result back to the main thread
            looper.post(() -> {
                // Update the graph
                parser.getEdges().forEach(graph::addEdge);
                graph.addDrivablePaths(parser.getDrivablePaths());

                // Hold on to the edges here
                edges.putAll(tileID, parser.getEdges());

                // Update listeners
                updateHorizonListeners();
            });
        });
    }


    private String resolveURL(final CanonicalTileID tileID) {
        // TODO externalize
        return String.format(endpoint, tileID.getZ(), tileID.getX(), tileID.getY(), token);
    }

    private Collection<Edge> copy(final Collection<Edge> edgesIn) {
        List<Edge> edgesOut = new ArrayList<>();
        for (Edge edge : edgesIn) {
            edgesOut.add(Edge.newBuilder(edge).build());
        }
        return edgesOut;
    }


    /**
     * Create an instance of the MapEngine.
     *
     * @param zoom     the zoom level to use
     * @param endpoint the vector tile endpoint in String.format format
     * @param token    the endpoint's security token
     */
    public MapEngineImpl(final int zoom, final String endpoint, final String token) {
        this.zoom = zoom;
        this.endpoint = endpoint;
        this.token = token;
        tracker.graph(graph);
        horizonDistance = tracker.horizonDistance();

        stateUpdateDebouncer = new Debouncer<>(
                (state) -> looper.post(
                        () -> doUpdateState(state)),
                DEFAULT_STATE_UPDATE_INTERVAL
        );
    }

    /**
     * Start the loop.
     * <p>
     * Blocks until the {@link Looper} is ready
     *
     * @return this
     */
    @Override
    public MapEngineImpl start() {
        StartupLatch latch = new StartupLatch();
        Thread thread = new Thread(() -> {
            Thread.currentThread().setName("Main Loop");
            looper = Looper.prepareMainLooper();
            looper.post(latch::started);
            looper.run();
        });
        thread.start();
        latch.await();

        return this;
    }

    @Override
    public void updateState(final State state) {
        // Don't process just every call or the system gets overloaded very quickly
        stateUpdateDebouncer.call(state);
    }

    @Override
    public void updateConfiguration(final Configuration configuration) {
        looper.post(() -> {
            if (configuration.updateFrequency() != null) {
                LOGGER.info("Setting update frequency to %s", configuration.updateFrequency());
                stateUpdateDebouncer.interval(configuration.updateFrequency());
            }

            if (configuration.horizonDistance() != null) {
                LOGGER.info("Setting horizon distance to %s", configuration.horizonDistance());
                this.horizonDistance = configuration.horizonDistance();
                tracker.horizonDistance(configuration.horizonDistance());
            }

            if (configuration.horizonExpansion() != null) {
                LOGGER.info("Setting horizon expansion to %s", configuration.horizonExpansion());
                tracker.fullGraphExpansion(HorizonExpansion.FULL.equals(configuration.horizonExpansion()));
            }
        });
    }

    @Override
    public Configuration getConfiguration() {
        CompletableFuture<Configuration> future = looper.ask(() -> new Configuration()
                .withHorizonDistance(tracker.horizonDistance())
                .withHorizonExpansion(
                        tracker.fullGraphExpansion() ? HorizonExpansion.FULL : HorizonExpansion.LIMITED
                )
                .withUpdateFrequency(stateUpdateDebouncer.interval()));

        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerEHorizonListener(final EHorizonListener listener) {
        looper.post(() -> {
            LOGGER.debug("Registering new EHorizon listener");
            horizonListeners.add(listener);
        });
    }

    @Override
    public void unregisterEHorizonListener(final EHorizonListener listener) {
        looper.post(() -> {
            LOGGER.debug("Unregistering EHorizon listener");
            horizonListeners.remove(listener);
        });
    }

    // MapEngineDebug implementations //

    @Override
    public Collection<Edge> getAllCurrentEdges() {
        CompletableFuture<Collection<Edge>> future = looper.ask(() -> copy(graph.edges()));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Set<Edge> getConnectedEdges(final long nodeId) {
        CompletableFuture<Set<Edge>> future = looper.ask(() -> graph.getConnectedEdges(nodeId));
        try {
            return future.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
