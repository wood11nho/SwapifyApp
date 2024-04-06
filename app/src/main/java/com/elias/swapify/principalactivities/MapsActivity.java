package com.elias.swapify.principalactivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.elias.swapify.R;
import com.elias.swapify.items.SeeAllItemsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Arrays;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap myMap;
    private final int FINE_PERMISSION_CODE = 1;
    Location currentLocation;
    FusedLocationProviderClient fusedLocationProviderClient;
    List<LocationItem> locationItems = Arrays.asList(
            new LocationItem("Bucharest", new LatLng(44.4268, 26.1025), 2), // Bucharest is listed twice with a total count of 2
            new LocationItem("Galati", new LatLng(45.4354, 28.0073), 1),
            new LocationItem("Craiova", new LatLng(44.3302, 23.7949), 1),
            new LocationItem("Iasi", new LatLng(47.1585, 27.6014), 1),
            new LocationItem("Cluj-Napoca", new LatLng(46.7712, 23.6236), 1)
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_maps);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, so request it
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_PERMISSION_CODE);
        } else {
            // Permission has already been granted, continue as usual
            initMap();
        }
    }

    private void initMap() {
        // Initialize your map here
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLastLocation();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void getLastLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    currentLocation = task.getResult();
                    if (myMap != null) {
                        LatLng currentLatLng = new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude());
                        myMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 14)); // Changed to zoom in a bit on the current location
                    }
                } else {
                    // Handle the case where the location is null (e.g. could not be determined)
                }
            });
        } else {
            // Permission not granted, handle accordingly
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted
                initMap();
            } else {
                // Permission was denied or request was cancelled
                // You can disable certain features or inform the user as needed
                // For now, let's just use a default location
                LatLng defaultLocation = new LatLng(44.4268, 26.1025); // Bucharest, for example
                myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 10));
            }
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMarkerClickListener(this);

        // Zoom into Romania
        LatLng romaniaCenter = new LatLng(45.9432, 24.9668); // Approximate center of Romania
        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(romaniaCenter, 6));

        // Add circles and markers for each location item
        for (LocationItem item : locationItems) {
            // Add a marker with the item count
            myMap.addMarker(new MarkerOptions()
                    .position(item.coordinates)
                    .title(item.cityName)
                    .snippet("Items: " + item.itemCount));

            // Add a circle to represent the area of each city
            myMap.addCircle(new CircleOptions()
                    .center(item.coordinates)
                    .radius(10000) // Radius in meters
                    .strokeWidth(0f) // We don't need a border
                    .fillColor(0x5500ff00)); // A semi-transparent green
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Create an AlertDialog Builder
        new AlertDialog.Builder(this)
                .setTitle("View Items")
                .setMessage("Do you want to see all items from " + marker.getTitle() + "?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    // User clicked yes, so let's start SeeAllItemsActivity
                    Intent intent = new Intent(MapsActivity.this, SeeAllItemsActivity.class);
                    intent.putExtra("query", marker.getTitle());
                    startActivity(intent);
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // User clicked no, dismiss the dialog
                    dialog.dismiss();
                })
                .show();
        return true; // We handle the click event so return true
    }
}

// A simple class to hold your locations and item counts
class LocationItem {
    String cityName;
    LatLng coordinates;
    int itemCount;

    LocationItem(String cityName, LatLng coordinates, int itemCount) {
        this.cityName = cityName;
        this.coordinates = coordinates;
        this.itemCount = itemCount;
    }
}