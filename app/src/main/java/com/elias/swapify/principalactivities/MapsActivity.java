package com.elias.swapify.principalactivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.elias.swapify.R;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.items.SeeAllItemsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<String, LatLng> locationCoordinates = new HashMap<>();
    FloatingActionButton fab;

    // Permission request with the new Activity Result API
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initMap();
                } else {
                    showDefaultLocation();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fab = findViewById(R.id.back_button);
        fab.setOnClickListener(v -> finish());

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        loadGeolocationData();
        requestLocationPermission();
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            initMap();
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void showDefaultLocation() {
        LatLng defaultLocation = new LatLng(44.4268, 26.1025); // Bucharest, for example
        if (myMap != null) {
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
        }
    }

    private void loadGeolocationData() {
        String json = null;
        try {
            InputStream is = getAssets().open("counties_and_cities/geolocation.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        Type listType = new TypeToken<List<GeolocationItem>>(){}.getType();
        List<GeolocationItem> geolocationItems = new Gson().fromJson(json, listType);

        for (GeolocationItem item : geolocationItems) {
            locationCoordinates.put(item.nume, new LatLng(item.latitudine, item.longitudine));
        }
    }

    private void initMap() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void applyMapStyle(GoogleMap googleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            boolean success = googleMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            this, R.raw.style_json)); // Create a raw resource folder and add your json file

            if (!success) {
                Log.e("MapsActivity", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("MapsActivity", "Can't find style. Error: ", e);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMarkerClickListener(this);
        applyMapStyle(myMap); // Apply custom style to the map
        fetchAndDisplayItems();
    }

    private void fetchAndDisplayItems() {
        FirebaseFirestore.getInstance().collection("ITEMS")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Temporary map to store item counts per location
                        HashMap<String, Integer> itemCountsPerLocation = new HashMap<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            if (!Objects.equals(document.getString("itemUserId"), FirebaseUtil.getCurrentUserId())) {
                                String locationName = document.getString("itemLocation");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    itemCountsPerLocation.put(locationName, itemCountsPerLocation.getOrDefault(locationName, 0) + 1);
                                }
                            }
                        }

                        // Now, create markers with item counts stored in their tags
                        for (Map.Entry<String, Integer> entry : itemCountsPerLocation.entrySet()) {
                            LatLng coordinates = locationCoordinates.get(entry.getKey());
                            if (coordinates != null) {
                                Marker marker = myMap.addMarker(new MarkerOptions()
                                        .position(coordinates)
                                        .title(entry.getKey()));
                                if (marker != null) {
                                    marker.setTag(entry.getValue());  // Store the item count in the marker's tag
                                }
                            }
                        }
                        LatLng romaniaCenter = new LatLng(45.9432, 24.9668);
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romaniaCenter, 6));
                    } else {
                        Log.d("MapsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the item count from the marker's tag
        Integer itemCount = (Integer) marker.getTag();
        String title = itemCount + " items found at " + marker.getTitle();

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("Do you want to see all items from " + marker.getTitle() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    Intent intent = new Intent(MapsActivity.this, SeeAllItemsActivity.class);
                    intent.putExtra("location", marker.getTitle());
                    startActivity(intent);
                })
                .setNegativeButton("No", null)
                .show();
        return true;
    }

    static class GeolocationItem {
        String nume;
        double latitudine;
        double longitudine;
    }
}