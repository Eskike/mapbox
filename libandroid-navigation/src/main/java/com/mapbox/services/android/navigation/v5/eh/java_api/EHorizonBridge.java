package com.mapbox.services.android.navigation.v5.eh.java_api;

import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.v5.eh.Configuration;
import com.mapbox.services.android.navigation.v5.eh.EHorizonListener;
import com.mapbox.services.android.navigation.v5.eh.HorizonExpansion;
import com.mapbox.services.android.navigation.v5.eh.MapEngine;
import com.mapbox.services.android.navigation.v5.eh.MapEngineImpl;

public class EHorizonBridge implements EHorizonListener {
    private static final int ZOOM = 15;
    MapEngine engine;

    public EHorizonBridge() {
        engine = new MapEngineImpl(
                ZOOM,
                "https://api.mapbox.com/v4/ivovandongen.d58ubas3/%s/%s/%s.mvt?access_token=%s",
                "pk.eyJ1IjoiaXZvdmFuZG9uZ2VuIiwiYSI6ImNpbzVpdG10eDAwM3R1bWtubGw0Y2dsb3kifQ.S0KL6oRTIrz12tQw1etMcg");

        Configuration config = engine.getConfiguration();
        config = config
                .withHorizonDistance(1000) // meters?
                .withUpdateFrequency(200)  // ms?
                .withHorizonExpansion(HorizonExpansion.LIMITED); // or FULL
        engine.updateConfiguration(config);
    }

    public void start() {
        engine.registerEHorizonListener(this);
        engine.start();
    }

    public void stop() {
        engine.unregisterEHorizonListener(this);
    }

    @Override
    public void onUpdate(EHorizonUpdate update) {

    }

    // Call on an interval when GPS position updates
    public void updatePosition(Point position) {
        MapEngine.State state = new MapEngine.State(position);
        engine.updateState(state);
    }
}
