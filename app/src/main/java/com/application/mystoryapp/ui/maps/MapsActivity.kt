package com.application.mystoryapp.ui.maps

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.application.mystoryapp.R
import com.application.mystoryapp.data.retrofit.ApiConfig

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.application.mystoryapp.databinding.ActivityMapsBinding
import com.application.mystoryapp.pref.UserPreference
import com.application.mystoryapp.pref.dataStore
import com.google.android.gms.maps.model.LatLngBounds
import kotlinx.coroutines.launch

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var userPreference: UserPreference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize UserPreference
        userPreference = UserPreference.getInstance(applicationContext.dataStore)

        // create back button on actionbar
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isIndoorLevelPickerEnabled = true
            isCompassEnabled = true
            isMapToolbarEnabled = true
        }

        fetchAndDisplayStories()
    }

    companion object {
        private const val TAG = "MapsActivity"
    }

    private fun fetchAndDisplayStories() {
        lifecycleScope.launch {
            try {
                // Get token from UserPreference
                userPreference.getSession().collect { user ->
                    val token = "Bearer ${user.token}"
                    val response = ApiConfig.getApiService().getStories(token, null, null, 1)

                    if (!response.error!! && response.listStory.isNotEmpty()) {
                        val boundsBuilder = LatLngBounds.Builder()
                        var hasValidLocation = false

                        response.listStory.forEach { story ->
                            val lat = story.lat
                            val lon = story.lon

                            if (lat != null && lon != null) {
                                hasValidLocation = true
                                val position = LatLng(lat.toDouble(), lon.toDouble())
                                mMap.addMarker(
                                    MarkerOptions()
                                        .position(position)
                                        .title(story.name)
                                        .snippet(story.description)  // Add description to marker
                                )
                                boundsBuilder.include(position)
                            }
                        }

                        // Only adjust camera if we have at least one valid location
                        if (hasValidLocation) {
                            val bounds = boundsBuilder.build()
                            val padding = resources.getDimensionPixelSize(R.dimen.map_padding)
                            val cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, padding)

                            mMap.animateCamera(cameraUpdate)
                        }
                    } else {
                        Log.d(TAG, "No stories found with location.")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error fetching stories: ${e.message}")
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed() // Navigate back
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}