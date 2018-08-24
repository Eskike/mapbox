package com.mapbox.services.android.navigation.ui.v5.instruction;

import android.view.View;

import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel;

public interface MapButton {

  void subscribe(NavigationViewModel navigationViewModel);

  void addOnClickListener(View.OnClickListener onClickListener);
}
