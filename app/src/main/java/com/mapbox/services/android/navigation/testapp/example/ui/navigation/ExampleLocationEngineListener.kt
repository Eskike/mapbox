package com.mapbox.services.android.navigation.testapp.example.ui.navigation

import android.annotation.SuppressLint
import android.location.Location
import com.mapbox.android.core.location.LocationEngine
import com.mapbox.android.core.location.LocationEngineListener
import com.mapbox.services.android.navigation.testapp.example.ui.ExampleViewModel

class ExampleLocationEngineListener(private val locationEngine: LocationEngine,
                                    private val viewModel: ExampleViewModel) : LocationEngineListener {

  override fun onLocationChanged(location: Location) {
    viewModel.updateLocation(location)
  }

  @SuppressLint("MissingPermission")
  override fun onConnected() {
    locationEngine.requestLocationUpdates()

    if (locationEngine.lastLocation != null) {
      onLocationChanged(locationEngine.lastLocation)
    }
  }
}