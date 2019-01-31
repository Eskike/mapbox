package com.mapbox.services.android.navigation.v5.navigation;

import android.os.AsyncTask;

import com.google.gson.Gson;
import com.mapbox.navigator.Navigator;
import com.mapbox.navigator.RouterResult;

class OfflineAttributesRetrievalTask extends AsyncTask<OfflineAttributes, Void, OfflineAttributesResult> {

  private final Navigator navigator;
  private final OnOfflineAttributesFoundCallback callback;

  OfflineAttributesRetrievalTask(Navigator navigator, OnOfflineAttributesFoundCallback callback) {
    this.navigator = navigator;
    this.callback = callback;
  }

  @Override
  protected OfflineAttributesResult doInBackground(OfflineAttributes... offlineAttributes) {
    String valhallaJson = offlineAttributes[0].toJson();

    RouterResult result;
    synchronized (navigator) {
      result = navigator.getTraceAttributes(valhallaJson);
    }

    return generateFrom(result.getJson());
  }

  @Override
  protected void onPostExecute(OfflineAttributesResult result) {
    callback.onOfflineAttributesFound(result);
  }

  private OfflineAttributesResult generateFrom(String jsonResponse) {
    Gson gson = new Gson();
    return gson.fromJson(jsonResponse, OfflineAttributesResult.class);
  }
}

