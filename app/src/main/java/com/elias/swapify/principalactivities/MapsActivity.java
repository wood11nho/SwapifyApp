package com.elias.swapify.principalactivities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.util.Log;
import android.widget.ToggleButton;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.elias.swapify.R;
import com.elias.swapify.events.EventModel;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.items.SeeAllItemsActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    private GoogleMap myMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private HashMap<String, LatLng> locationCoordinates = new HashMap<>();
    private FloatingActionButton fabBack, fabToggleLocation;
    private ToggleButton toggleItems, toggleEvents;
    private boolean isLocationEnabled = false;
    private List<Marker> itemMarkers = new ArrayList<>();
    private List<Marker> eventMarkers = new ArrayList<>();
    private EventModel currentEvent; // Store the current event for which permissions are being requested

    // Permission request with the new Activity Result API
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    initMap();
                } else {
                    showDefaultLocation();
                }
            });

    private final ActivityResultLauncher<String[]> calendarPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean writeCalendarGranted = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    writeCalendarGranted = result.getOrDefault(Manifest.permission.WRITE_CALENDAR, false);
                }
                Boolean readCalendarGranted = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    readCalendarGranted = result.getOrDefault(Manifest.permission.READ_CALENDAR, false);
                }
                if (writeCalendarGranted != null && readCalendarGranted != null && writeCalendarGranted && readCalendarGranted) {
                    // Permissions granted, create the calendar reminder
                    if (currentEvent != null) {
                        createCalendarReminder(currentEvent);
                    }
                } else {
                    Toast.makeText(this, "Calendar permissions are required to create reminders.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        fabBack = findViewById(R.id.back_button);
        fabBack.setOnClickListener(v -> finish());

        fabToggleLocation = findViewById(R.id.fab_toggle_location);
        fabToggleLocation.setOnClickListener(v -> navigateToUserLocation());

        toggleItems = findViewById(R.id.toggleItems);
        toggleEvents = findViewById(R.id.toggleEvents);

        toggleItems.setOnCheckedChangeListener((buttonView, isChecked) -> updateMarkersVisibility());
        toggleEvents.setOnCheckedChangeListener((buttonView, isChecked) -> updateMarkersVisibility());

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
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                new AlertDialog.Builder(this)
                        .setTitle("Location Permission Needed")
                        .setMessage("This app needs the Location permission to show your location on the map.")
                        .setPositiveButton("OK", (dialog, which) -> requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION))
                        .create()
                        .show();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
    }

    private void showDefaultLocation() {
        LatLng defaultLocation = new LatLng(45.9432, 24.9668); // Romania Center
        if (myMap != null) {
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 6));
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

        // Disable the default location button
        myMap.getUiSettings().setMyLocationButtonEnabled(false);

        showDefaultLocation(); // Show Romania initially

        // Delay zoom to user's location by 1 second
        new Handler().postDelayed(() -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                myMap.setMyLocationEnabled(true);

                // Get the last known location and move the camera to it
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                    if (location != null) {
                        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                        isLocationEnabled = true;
                    }
                });
            }
        }, 1000);

        fetchAndDisplayItems();
        fetchAndDisplayEvents();
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
                                        .title(entry.getKey())
                                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))); // Red for items
                                if (marker != null) {
                                    marker.setTag(entry.getValue());  // Store the item count in the marker's tag
                                    itemMarkers.add(marker);
                                }
                            }
                        }
                    } else {
                        Log.d("MapsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchAndDisplayEvents() {
        FirebaseFirestore.getInstance().collection("EVENTS")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            double latitude = document.getDouble("latitude");
                            double longitude = document.getDouble("longitude");
                            String eventName = document.getString("name");
                            LatLng eventLocation = new LatLng(latitude, longitude);
                            String eventDescription = document.getString("description");
                            Timestamp timestamp = document.getTimestamp("timestamp");
                            Marker marker = myMap.addMarker(new MarkerOptions()
                                    .position(eventLocation)
                                    .title(eventName)
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))); // Green for events
                            if (marker != null) {
                                marker.setTag(new EventModel(eventName, eventDescription, latitude, longitude, timestamp));
                                eventMarkers.add(marker);
                            }
                        }
                    } else {
                        Log.d("MapsActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void updateMarkersVisibility() {
        boolean showItems = toggleItems.isChecked();
        boolean showEvents = toggleEvents.isChecked();

        for (Marker marker : itemMarkers) {
            marker.setVisible(showItems);
        }
        for (Marker marker : eventMarkers) {
            marker.setVisible(showEvents);
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        // Retrieve the tag to check if it's an item or event marker
        Object tag = marker.getTag();
        if (tag instanceof Integer) {
            // Item marker
            Integer itemCount = (Integer) tag;
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
        } else if (tag instanceof EventModel) {
            // Event marker
            EventModel event = (EventModel) tag;
            currentEvent = event;
            requestCalendarPermission();
        }
        return true;
    }

    private void requestCalendarPermission() {
        calendarPermissionLauncher.launch(new String[]{
                Manifest.permission.WRITE_CALENDAR,
                Manifest.permission.READ_CALENDAR
        });
    }

    private void createCalendarReminder(EventModel event) {
        Calendar beginTime = Calendar.getInstance();
        beginTime.setTime(event.getTimestamp().toDate());
        Calendar endTime = (Calendar) beginTime.clone();
        endTime.add(Calendar.HOUR, 1);

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, event.getName())
                .putExtra(CalendarContract.Events.DESCRIPTION, event.getDescription())
                .putExtra(CalendarContract.Events.EVENT_LOCATION, event.getLatitude() + ", " + event.getLongitude());

        // Check if there is an app that can handle this intent
        Log.d("MapsActivity", "Intent: " + intent.toString());
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e("MapsActivity", "No app found to handle the intent.");
            Toast.makeText(this, "No calendar app found to create reminder.", Toast.LENGTH_SHORT).show();
        }
    }


    private void navigateToUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
                    myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
                }
            });
        } else {
            requestLocationPermission();
        }
    }

    static class GeolocationItem {
        String nume;
        double latitudine;
        double longitudine;
    }
}
