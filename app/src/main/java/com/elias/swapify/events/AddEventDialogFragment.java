package com.elias.swapify.events;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.elias.swapify.R;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddEventDialogFragment extends DialogFragment {

    private EditText etEventName, etEventDescription;
    private TextView tvChosenLocation, tvChosenDateTime;
    private Button btnAddEvent, btnChooseLocation, btnChooseDateTime;
    private double latitude = 0.0, longitude = 0.0;
    private Timestamp timestamp;

    public static AddEventDialogFragment newInstance() {
        return new AddEventDialogFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_event, container, false);

        etEventName = view.findViewById(R.id.etEventName);
        etEventDescription = view.findViewById(R.id.etEventDescription);
        tvChosenLocation = view.findViewById(R.id.tvChosenLocation);
        tvChosenDateTime = view.findViewById(R.id.tvChosenDateTime);
        btnAddEvent = view.findViewById(R.id.btnAddEvent);
        btnChooseLocation = view.findViewById(R.id.btnChooseLocation);
        btnChooseDateTime = view.findViewById(R.id.btnChooseDateTime);

        btnAddEvent.setOnClickListener(v -> addEvent());
        btnChooseLocation.setOnClickListener(v -> chooseLocationOnMap());
        btnChooseDateTime.setOnClickListener(v -> chooseDateTime());

        return view;
    }

    private void addEvent() {
        String name = etEventName.getText().toString();
        String description = etEventDescription.getText().toString();

        if (name.isEmpty() || description.isEmpty() || latitude == 0.0 || longitude == 0.0 || timestamp == null) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        EventModel event = new EventModel(name, description, latitude, longitude, timestamp);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("EVENTS")
                .add(event)
                .addOnSuccessListener(documentReference -> dismiss())
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    private void chooseLocationOnMap() {
        Intent intent = new Intent(getContext(), MapPickerActivity.class);
        startActivityForResult(intent, 1);
    }

    private void chooseDateTime() {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(), (view1, hourOfDay, minute) -> {
                calendar.set(year, month, dayOfMonth, hourOfDay, minute);
                timestamp = new Timestamp(calendar.getTime());
                tvChosenDateTime.setText("Date and Time: " + calendar.getTime());
                tvChosenDateTime.setVisibility(View.VISIBLE);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true);
            timePickerDialog.show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            latitude = data.getDoubleExtra("latitude", 0.0);
            longitude = data.getDoubleExtra("longitude", 0.0);
            fetchNameOfLocation(latitude, longitude);
        }
    }

    private void fetchNameOfLocation(double latitude, double longitude) {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressName = address.getAddressLine(0);
                tvChosenLocation.setText("Location: " + addressName); // "Location: 123 Main St, City, State, Country"
                tvChosenLocation.setVisibility(View.VISIBLE);
            } else {
                tvChosenLocation.setText("Latitude: " + latitude + ", Longitude: " + longitude);
                tvChosenLocation.setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            tvChosenLocation.setText("Latitude: " + latitude + ", Longitude: " + longitude);
            tvChosenLocation.setVisibility(View.VISIBLE);
        }
    }
}
