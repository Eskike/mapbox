package com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match;

import com.mapbox.services.android.navigation.v5.eh.cheapruler.CheapRuler;
import com.mapbox.services.android.navigation.v5.eh.cheapruler.PointOnLineResult;
import com.mapbox.services.android.navigation.v5.eh.geo.Bearing;
import com.mapbox.services.android.navigation.v5.eh.horizon.Edge;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.cost.CostFunction;
import com.mapbox.services.android.navigation.v5.eh.horizon.tracker.cost.CumulativeCostFunction;
import com.mapbox.geojson.Point;

import java.util.List;

import static com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Constants.WEIGHT_BEARING;
import static com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Constants.WEIGHT_DISTANCE;

final class LocationHistoryMatch extends BaseMatch {
    private final double distance;
    private final PointOnLineResult pointOnLine;
    private final double bearingOffset;
    private final CostFunction costFunction;

    LocationHistoryMatch(final CheapRuler ruler, final Edge edge,
                         final Point rawPosition, final double referenceBearing) {
        super(edge);

        List<Point> line = edge.getCenterLine().coordinates();

        // Pre-calculate match values
        pointOnLine = ruler.pointOnLine(line, rawPosition);

        // Distance from the position and the matched position
        distance = ruler.distance(rawPosition, pointOnLine.point());

        // Bearing of the matching segment
        double bearing = ruler.bearing(
                line.get(pointOnLine.index()),
                line.get(pointOnLine.index() + 1));

        // Offset with the reference bearing
        bearingOffset = Bearing.diff(referenceBearing, bearing, true);

        costFunction = new CumulativeCostFunction()
                .withCost(WEIGHT_BEARING, () -> bearing)
                .withCost(WEIGHT_DISTANCE, () -> distance);
    }

    @Override
    public boolean matches() {
        return super.matches(distance, bearingOffset) && !isPassed(pointOnLine);
    }

    @Override
    public double cost() {
        return costFunction.cost();
    }

    @Override
    public PointOnLineResult pointOnLine() {
        return pointOnLine;
    }
}
