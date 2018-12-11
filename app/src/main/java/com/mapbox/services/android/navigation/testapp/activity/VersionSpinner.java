package com.mapbox.services.android.navigation.testapp.activity;

import android.content.Context;
import android.content.res.Resources;
import android.os.Environment;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;

import com.mapbox.services.android.navigation.testapp.R;
import com.mapbox.services.android.navigation.v5.navigation.MapboxOfflineRouter;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VersionSpinner extends AppCompatSpinner {
  private MapboxOfflineRouter offlineRouter;
  private String offlineDisabledText;

  public VersionSpinner(Context context) {
    this(context, null);
  }

  public VersionSpinner(Context context, int mode) {
    this(context, null, android.support.v7.appcompat.R.attr.spinnerStyle, mode);
  }

  public VersionSpinner(Context context, AttributeSet attrs) {
    this(context, attrs, android.support.v7.appcompat.R.attr.spinnerStyle);
  }

  public VersionSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, -1);
  }

  public VersionSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode) {
    this(context, attrs, defStyleAttr, mode, null);
  }

  public VersionSpinner(Context context, AttributeSet attrs, int defStyleAttr, int mode, Resources.Theme popupTheme) {
    super(context, attrs, defStyleAttr, mode, popupTheme);
    initialize();
  }

  public boolean isOfflineSelected() {
    return !offlineDisabledText.equals(getSelectedItem());
  }

  private void initialize() {
    offlineDisabledText = getContext().getString(R.string.offline_disabled);
    File file = new File(Environment.getExternalStorageDirectory(), "offline");
    List<String> list = buildFileList(new File(file, "tiles"));

    setAdapter(new ArrayAdapter<>(getContext(), R.layout.support_simple_spinner_dropdown_item, list));
    setSelection(0);

  }

  private List<String> buildFileList(File file) {
    List<String> list;
    if (file.list() != null && file.list().length != 0) {
      list = new ArrayList<>(Arrays.asList(file.list()));
    } else {
      list = new ArrayList<>();
    }
    list.add(0, offlineDisabledText);
    return list;
  }

}
