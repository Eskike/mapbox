package com.mapbox.services.android.navigation.v5.navigation;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class OfflineAttributesResult {

  @SerializedName("edges")
  private final List<Edge> edges;

  public OfflineAttributesResult(List<Edge> edges) {
    this.edges = edges;
  }

  public List<Edge> getEdges() {
    return edges;
  }
}
