package com.mapbox.services.android.navigation.v5.eh;

import com.mapbox.geojson.Point;

import java.util.Objects;

/**
 * Main interface for the MapEngine.
 */
public interface MapEngine {

    /**
     * The MapEngine's state.
     */
    class State {
        private final Point position;

        /**
         * Create a new State.
         *
         * @param position the position of the vehicle.
         */
        public State(final Point position) {
            this.position = position;
        }

        /**
         * @return the position
         */
        public Point getPosition() {
            return position;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            State state = (State) o;
            return Objects.equals(position, state.position);
        }

        @Override
        public int hashCode() {
            return Objects.hash(position);
        }
    }

    /**
     * Blocks until the engine is ready.
     *
     * @return this
     */
    MapEngineImpl start();

    /**
     * Update the current state of the vehicle.
     *
     * @param state the new State
     */
    void updateState(State state);

    /**
     * Update the Configuration.
     *
     * @param configuration the (partial) configuration
     */
    void updateConfiguration(Configuration configuration);

    /**
     * @return the current configuration
     */
    Configuration getConfiguration();

    /**
     * Register a {@link EHorizonListener}.
     *
     * @param listener the listener
     */
    void registerEHorizonListener(EHorizonListener listener);

    /**
     * Unregister a {@link EHorizonListener}.
     *
     * @param listener the listener
     */
    void unregisterEHorizonListener(EHorizonListener listener);
}
