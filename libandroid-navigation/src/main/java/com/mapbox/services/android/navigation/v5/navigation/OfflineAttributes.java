package com.mapbox.services.android.navigation.v5.navigation;

import android.location.Location;

import java.util.List;

public class OfflineAttributes {

  private final List<Location> trace;
  private final List<String> attributes;

  public OfflineAttributes(List<Location> trace, List<String> attributes) {
    this.trace = trace;
    this.attributes = attributes;
  }

  String toJson() {

    String startJson = "{\"shape\":[";

    for (Location location : trace) {
      startJson += "{\n" +
        "      \"lat\": " + location.getLatitude() + ",\n" +
        "      \"lon\": " + location.getLongitude() + "\n" +
        "    },";
    }

    startJson = startJson.substring(0, startJson.length() - 1);
    startJson += "],";

    String baseJson = "\n" +
      "  \"costing\": \"auto\",\n" +
      "  \"shape_match\": \"map_snap\",\n" +
      "  \"filters\": {\n" +
      "    \"attributes\": [\n" +
      "      \"edge.names\",\n" +
      "      \"edge.speed_limit\"\n" +
      "    ],\n" +
      "    \"action\": \"include\"\n" +
      "  }\n" +
      "}";

    return startJson + baseJson;
  }


}
