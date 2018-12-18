package com.mapbox.services.android.navigation.v5.eh.horizon.tracker.match;

final class Constants {

    private Constants() {
    }

    static final double THRESHOLD_MATCH_DISTANCE_M = 10;
    static final double THRESHOLD_MATCH_BEARING_OFFSET = 30;

    static final int WEIGHT_SAME_EDGE = 10;
    static final double WEIGHT_BEARING = .5;
    static final double WEIGHT_DISTANCE = 1.;

}
