package com.mapbox.services.android.navigation.v5.navigation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Edge {

  @SerializedName("names")
  private final List<String> names;

  @SerializedName("speed_limit")
  private final float speedLimit;

  Edge(List<String> names, float speedLimit) {
    this.names = names;
    this.speedLimit = speedLimit;
  }

  public List<String> getNames() {
    return names;
  }

  public float getSpeedLimit() {
    return speedLimit;
  }
}
