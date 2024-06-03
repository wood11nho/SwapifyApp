package com.elias.swapify.events;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.elias.swapify.R;
import com.elias.swapify.secrets.SecretsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class MapPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private FloatingActionButton fabCurrentLocation, fabBack;
    private EditText etSearchLocation;
    private Button btnSaveLocation;
    private PlacesClient placesClient;
    private final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng selectedLocation = null;
    private String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        fabCurrentLocation = findViewById(R.id.fab_toggle_location);
        fabCurrentLocation.setOnClickListener(v -> navigateToUserLocation());
        fabBack = findViewById(R.id.back_button);
        fabBack.setOnClickListener(v -> finish());
        etSearchLocation = findViewById(R.id.et_search_location);
        btnSaveLocation = findViewById(R.id.btn_save_location);
        btnSaveLocation.setOnClickListener(v -> saveLocation());

        // Initialize the Places API
        SecretsManager secretsManager = new SecretsManager(this);
        apiKey = secretsManager.getSecret("com.google.android.geo.API_KEY");
        if (!Places.isInitialized()) {
            Places.initialize(this, apiKey);
        }
        placesClient = Places.createClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        checkLocationPermission();

        // Set up search functionality
        etSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            String query = etSearchLocation.getText().toString();
            searchLocation(query);
            return true;
        });

        // Automatically center the map on user's location after 5 seconds
        new Handler().postDelayed(this::navigateToUserLocation, 5000);
    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnMapClickListener(latLng -> {
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng));
            selectedLocation = latLng;
            btnSaveLocation.setVisibility(View.VISIBLE);
        });

        LatLng defaultLocation = new LatLng(-34, 151);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(defaultLocation));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }
    }

    private void navigateToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                    mMap.clear();
                    mMap.addMarker(new MarkerOptions().position(userLocation));
                    selectedLocation = userLocation;
                    btnSaveLocation.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private void searchLocation(String query) {
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        placesClient.findAutocompletePredictions(request).addOnSuccessListener(response -> {
            for (AutocompletePrediction prediction : response.getAutocompletePredictions()) {
                // Here, show the predictions to the user and let them choose
                String placeId = prediction.getPlaceId();
                String primaryText = prediction.getPrimaryText(null).toString();

                // For demonstration purposes, we'll use the first prediction
                placesClient.fetchPlace(com.google.android.libraries.places.api.net.FetchPlaceRequest.builder(placeId, List.of(Place.Field.LAT_LNG)).build())
                        .addOnSuccessListener(fetchPlaceResponse -> {
                            Place place = fetchPlaceResponse.getPlace();
                            if (place.getLatLng() != null) {
                                LatLng latLng = place.getLatLng();
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                                mMap.clear();
                                mMap.addMarker(new MarkerOptions().position(latLng));
                                selectedLocation = latLng;
                                btnSaveLocation.setVisibility(View.VISIBLE);
                            }
                        }).addOnFailureListener(exception -> Toast.makeText(MapPickerActivity.this, "Place not found: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        }).addOnFailureListener(exception -> Toast.makeText(MapPickerActivity.this, "Autocomplete request failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveLocation() {
        if (selectedLocation != null) {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("latitude", selectedLocation.latitude);
            resultIntent.putExtra("longitude", selectedLocation.longitude);
            setResult(RESULT_OK, resultIntent);
            finish();
        } else {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                        ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }
    }
}
