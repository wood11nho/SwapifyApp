package com.example.swapify;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEdtText;
    private EditText usernameEdtText;
    private EditText emailEdtText;
    private EditText phone_numberEdtText;
    private EditText bioEdtText;
    private Spinner countySpinner;
    private Spinner citySpinner;

    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;

    private List<Pair<String, String>> countiesGlobal = new ArrayList<>();
    private List<String> citiesGlobal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedIstanceState) {
        super.onCreate(savedIstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEdtText = findViewById(R.id.name_edit_text);
        usernameEdtText = findViewById(R.id.username_edit_text);
        emailEdtText = findViewById(R.id.email_edit_text);
        phone_numberEdtText = findViewById(R.id.phone_number_edit_text);
        bioEdtText = findViewById(R.id.bio_edit_text);
        countySpinner = findViewById(R.id.county_spinner);
        citySpinner = findViewById(R.id.city_spinner);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        String userId = firebaseAuth.getCurrentUser().getUid();
        fetchUserData(userId);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://roloca.coldfuse.io/judete"); // Replace with your API endpoint URL
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    String inputLine;
                    StringBuffer response = new StringBuffer();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String countyName = jsonObject.getString("nume");
                        String countyCode = jsonObject.getString("auto");
                        countiesGlobal.add(new Pair<>(countyName, countyCode));
                        Log.d("COUNTY", countiesGlobal.get(i).first + " " + countiesGlobal.get(i).second);
                    }

                    // Update spinner adapter after adding all counties
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final List<String> countyNames = new ArrayList<>();
                            for (Pair<String, String> county : countiesGlobal) {
                                countyNames.add(county.first);
                            }

                            ArrayAdapter<String> countyAdapter = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_spinner_item, countyNames);
                            countyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            countySpinner.setAdapter(countyAdapter);
                            countyAdapter.notifyDataSetChanged();

                            // Set the selected county
                            String county = countySpinner.getSelectedItem().toString();
                            if (!county.isEmpty()) {
                                int countyIndex = countyNames.indexOf(county);
                                countySpinner.setSelection(countyIndex);
                            }
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

        countySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Pair<String, String> selectedCounty = countiesGlobal.get(position);
                if (selectedCounty == null) {
                    return;
                }
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            URL url = new URL("https://roloca.coldfuse.io/orase/" + selectedCounty.second);
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestMethod("GET");
                            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                            String inputLine;
                            StringBuffer response = new StringBuffer();
                            while ((inputLine = in.readLine()) != null) {
                                response.append(inputLine);
                            }
                            in.close();

                            JSONArray jsonArray = new JSONArray(response.toString());
                            final List<String> cities = new ArrayList<>();
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject = jsonArray.getJSONObject(i);
                                String cityName = jsonObject.getString("nume");
                                cities.add(cityName);
                                citiesGlobal.add(cityName);
                            }

                            // update the city spinner with the fetched data
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_spinner_item, cities);
                                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    citySpinner.setAdapter(cityAdapter);
                                    cityAdapter.notifyDataSetChanged();

                                    // set the selected city
                                    String city = citySpinner.getSelectedItem().toString();
                                    if (!city.isEmpty()) {
                                        int cityIndex = cities.indexOf(city);
                                        citySpinner.setSelection(cityIndex);
                                    }
                                }
                            });
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            String newPhoneNumber = phone_numberEdtText.getText().toString();
            String username = usernameEdtText.getText().toString();

            // Check if the new phone number is already taken
            isPhoneNumberTaken(newPhoneNumber, username);

        });

    }

    private void isPhoneNumberTaken(String newPhoneNumber, String currentUsername) {
        firestoreDB.collection("USERS")
                .whereEqualTo("phonenumber", newPhoneNumber)
                .whereNotEqualTo("username", currentUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // The phone number is already taken
                        Toast.makeText(EditProfileActivity.this, "This phone number is already taken", Toast.LENGTH_SHORT).show();
                    } else {
                        // The phone number is available, check if the email is taken
                        isEmailTaken(emailEdtText.getText().toString(), currentUsername);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Log.e("FirestoreQueryError", "Failed to check phone number. Error: " + e.getMessage());
                    Toast.makeText(EditProfileActivity.this, "Failed to check phone number. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void isEmailTaken(String newEmail, String currentUsername) {
        firestoreDB.collection("USERS")
                .whereEqualTo("email", newEmail)
                .whereNotEqualTo("username", currentUsername)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // The email is already taken
                        Toast.makeText(EditProfileActivity.this, "This email is already taken", Toast.LENGTH_SHORT).show();
                    } else {
                        // Both email and phone number are available, update user data
                        updateUserFirestoreData();
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Log.e("FirestoreQueryError", "Failed to check email. Error: " + e.getMessage());
                    Toast.makeText(EditProfileActivity.this, "Failed to check email. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateUserFirestoreData() {
        String name = nameEdtText.getText().toString();
        String username = usernameEdtText.getText().toString();
        String email = emailEdtText.getText().toString();
        String phone_number = phone_numberEdtText.getText().toString();
        String bio = bioEdtText.getText().toString();
        String county = countySpinner.getSelectedItem().toString();
        String city = citySpinner.getSelectedItem().toString();

        String userId = firebaseAuth.getCurrentUser().getUid();
        firestoreDB.collection("USERS").document(userId).update(
                        "name", name,
                        "username", username,
                        "email", email,
                        "phonenumber", phone_number,
                        "bio", bio,
                        "county", county,
                        "city", city
                )
                .addOnSuccessListener(aVoid -> {
                    // Update the UI with the updated user data
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchUserData(String userId) {
        firestoreDB.collection("USERS").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String phone_number = documentSnapshot.getString("phonenumber");
                        String bio = documentSnapshot.getString("bio");
                        String county = documentSnapshot.getString("county");
                        String city = documentSnapshot.getString("city");

                        // Update the UI with fetched user data
                        nameEdtText.setText(name);
                        usernameEdtText.setText(username);
                        emailEdtText.setText(email);
                        phone_numberEdtText.setText(phone_number);
                        bioEdtText.setText(bio);

                        // Set the selected county and city in the spinners
                        setCountyAndCitySelection(county, city);
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    // For simplicity, we won't handle the error here. You can add appropriate error handling.
                });
    }

    private void setCountyAndCitySelection(String county, String city) {
        // Set the selected county in the spinner
        int countyIndex = 0;
        for (int i = 0; i < countiesGlobal.size(); i++) {
            Pair<String, String> countyPair = countiesGlobal.get(i);
            if (countyPair.first.equals(county)) {
                countyIndex = i;
                break;
            }
        }
        countySpinner.setSelection(countyIndex);

        // Set the selected city in the spinner
        int cityIndex = 0;
        for (int i = 0; i < citiesGlobal.size(); i++) {
            String cityString = citiesGlobal.get(i);
            if (cityString.equals(city)) {
                cityIndex = i;
                break;
            }
        }
        citySpinner.setSelection(cityIndex);
    }
}
