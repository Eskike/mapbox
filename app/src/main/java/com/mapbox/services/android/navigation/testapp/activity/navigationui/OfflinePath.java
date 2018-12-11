package com.mapbox.services.android.navigation.testapp.activity.navigationui;

import android.os.Environment;

import java.io.File;

public class OfflinePath {

  public File getDefaultPath() {
    return Environment.getExternalStoragePublicDirectory("Offline");
  }
}
