package com.mapbox.services.android.navigation.testapp.activity.navigationui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mapbox.services.android.navigation.testapp.R;
import com.mapbox.services.android.navigation.ui.v5.NavigationView;
import com.mapbox.services.android.navigation.ui.v5.OnNavigationReadyCallback;

public abstract class NavigationActivity extends AppCompatActivity implements OnNavigationReadyCallback {
  private NavigationView navigationView;

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    setTheme(R.style.Theme_AppCompat_Light_NoActionBar);
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_offline_navigation_view);
    navigationView = findViewById(R.id.navigationView);

    navigationView.onCreate(savedInstanceState);
    navigationView.initialize(this);
  }

  @Override
  public abstract void onNavigationReady(boolean isRunning);


}
