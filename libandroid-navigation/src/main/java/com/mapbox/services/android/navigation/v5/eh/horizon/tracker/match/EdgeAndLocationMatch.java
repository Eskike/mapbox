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
import static com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match.Constants.WEIGHT_SAME_EDGE;

final class EdgeAndLocationMatch extends BaseMatch {

    private final double distance;
    private final PointOnLineResult pointOnLine;
    private final double bearingOffset;
    private final CostFunction costFunction;

    EdgeAndLocationMatch(final CheapRuler ruler, final Edge previousEdge,
                         final Edge edge, final Point previousPosition,
                         final Point newPosition) {
        super(edge);

        List<Point> line = edge.getCenterLine().coordinates();

        // Pre-calculate match values
        pointOnLine = ruler.pointOnLine(line, newPosition);

        // Distance from the position and the matched position
        distance = ruler.distance(newPosition, pointOnLine.point());

        // Bearing of the matching segment
        double bearing = ruler.bearing(
                line.get(pointOnLine.index()),
                line.get(pointOnLine.index() + 1));

        // Offset with the reference bearing
        bearingOffset = Bearing.diff(
                ruler.bearing(previousPosition, newPosition),
                bearing,
                true
        );

        costFunction = new CumulativeCostFunction()
                .withCost(WEIGHT_SAME_EDGE, () -> previousEdge.getId() == edge.getId() ? 0 : 1)
                .withCost(WEIGHT_BEARING, () -> bearingOffset)
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
