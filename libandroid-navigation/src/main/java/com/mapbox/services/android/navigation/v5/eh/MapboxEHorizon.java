package com.mapbox.services.android.navigation.v5.eh;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.mapbox.geojson.Point;

public class MapboxEHorizon {

  private static final int ZOOM = 15;
  private final String accessToken;
  private final MapEngine engine;

  public MapboxEHorizon(@NonNull String accessToken) {
    this.accessToken = accessToken;
    engine = buildMapEngine();
  }

  public void start() {
    engine.start();
  }

  public void updateConfiguration(@Nullable Configuration configuration) {
    Configuration config = configuration;
    if (config == null) {
      config = buildDefaultConfiguration();
    }
    engine.updateConfiguration(config);
  }

  public void registerListener(EHorizonListener listener) {
    engine.registerEHorizonListener(listener);
  }

  public void unregisterListener(EHorizonListener listener) {
    engine.unregisterEHorizonListener(listener);
  }

  public void updatePosition(Point position) {
    MapEngine.State state = new MapEngine.State(position);
    engine.updateState(state);
  }

  @NonNull
  private MapEngineImpl buildMapEngine() {
    return new MapEngineImpl(
      ZOOM,
      "https://api.mapbox.com/v4/ivovandongen.d58ubas3/%s/%s/%s.mvt?access_token=%s",
      accessToken
    );
  }

  @NonNull
  private Configuration buildDefaultConfiguration() {
    return new Configuration()
      .withHorizonDistance(1000)
      .withUpdateFrequency(2000)
      .withHorizonExpansion(HorizonExpansion.LIMITED);
  }
}
