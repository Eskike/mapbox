package com.mapbox.services.android.navigation.v5.eh;

import com.mapbox.services.android.navigation.v5.eh.geo.CanonicalTileID;
import com.mapbox.services.android.navigation.v5.eh.horizon.EHorizon;
import com.mapbox.services.android.navigation.v5.eh.horizon.graph.MatchResult;
import com.mapbox.geojson.Point;

import java.util.Collection;
import java.util.List;

/**
 * Listen to updates to the EHorizon.
 */
public interface EHorizonListener {

    /**
     * Gets updated with the current collection of edges.
     *
     * @param update the current horizon
     */
    void onUpdate(EHorizonUpdate update);

    /**
     * Ehorizon updte.
     */
    interface EHorizonUpdate {
        Collection<CanonicalTileID> tileIds();

        Point getPosition();
    }

    /**
     * Base for all {@link EHorizonUpdate}s.
     */
    abstract class BaseEHorizonUpdate implements EHorizonUpdate {
        private final Collection<CanonicalTileID> tileIDs;
        private final Point position;

        BaseEHorizonUpdate(final Point position, final Collection<CanonicalTileID> tileIDs) {
            this.position = position;
            this.tileIDs = tileIDs;
        }

        @Override
        public Collection<CanonicalTileID> tileIds() {
            return tileIDs;
        }

        @Override
        public Point getPosition() {
            return position;
        }
    }

    /**
     * An update to the horizon.
     */
    class MatchedUpdate extends BaseEHorizonUpdate implements EHorizonUpdate {
        private final EHorizon horizon;

        public MatchedUpdate(final Point position, final EHorizon horizon,
                             final Collection<CanonicalTileID> tileIDs) {
            super(position, tileIDs);
            this.horizon = horizon;
        }

        public EHorizon horizon() {
            return horizon;
        }
    }

    /**
     * Contains possible matches and their distances.
     */
    class UnMatchedUpdate extends BaseEHorizonUpdate implements EHorizonUpdate {
        private final List<MatchResult> possibleMatches;

        public UnMatchedUpdate(final Point position, final List<MatchResult> possibleMatches,
                        final Collection<CanonicalTileID> tileIDs) {
            super(position, tileIDs);
            this.possibleMatches = possibleMatches;
        }

        public List<MatchResult> possibleMatches() {
            return possibleMatches;
        }
    }
}
