package com.example.swapify;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private String profilePictureUrl = "";
    private ImageView imgProfilePic;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        imgProfilePic = findViewById(R.id.profile_picture);
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
                    AssetManager assetManager = getAssets();
                    String filePath = "counties_and_cities/counties.json";
                    InputStream is = assetManager.open(filePath);
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    is.close();

                    String jsonContent = stringBuilder.toString();

                    JSONArray jsonArray = new JSONArray(jsonContent);

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String countyName = jsonObject.getString("nume");
                        String countyCode = jsonObject.getString("auto");
                        countiesGlobal.add(new Pair<>(countyName, countyCode));
                        Log.d("County", countyName + " " + countyCode);
                    }

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
                        try{
                            AssetManager assetManager = getAssets();
                            String filePath = "counties_and_cities/cities" + selectedCounty.second + ".json";
                            InputStream is = assetManager.open(filePath);
                            StringBuilder stringBuilder = new StringBuilder();
                            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

                            String line;
                            while ((line = bufferedReader.readLine()) != null) {
                                stringBuilder.append(line);
                            }
                            bufferedReader.close();
                            is.close();

                            String jsonContent = stringBuilder.toString();

                            JSONObject jsonObject = new JSONObject(jsonContent);
                            JSONArray jsonArray = jsonObject.getJSONArray("cities");
                            citiesGlobal.clear();

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                                String cityName = jsonObject1.getString("nume");
                                citiesGlobal.add(cityName);
                                Log.d("City", cityName);
                            }

                            // Update the UI with the fetched cities
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final List<String> cityNames = new ArrayList<>(citiesGlobal);

                                    ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_spinner_item, cityNames);
                                    cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    citySpinner.setAdapter(cityAdapter);
                                    cityAdapter.notifyDataSetChanged();

                                    // Set the selected city
                                    String city = citySpinner.getSelectedItem().toString();
                                    if (!city.isEmpty()) {
                                        int cityIndex = cityNames.indexOf(city);
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
                    // Log request.auth.uid to see the user who triggered the function
                    Log.e("USER", FirebaseAuth.getInstance().getCurrentUser().getUid());
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
                        "city", city,
                        "profilepicture", profilePictureUrl
                )
                .addOnSuccessListener(aVoid -> {
                    // Update the UI with the updated user data
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Toast.makeText(EditProfileActivity.this, "Failed to update profile. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    public void openAvatarSelectionDialog(View view) {
        // In this method, you can show a dialog or start an activity to display a list of classic avatars.
        // When the user selects an avatar, call the updateUserFirestoreData method with the selected avatar's URL.

        // For simplicity, let's assume we have a list of avatar URLs
        List<String> classicAvatars = Arrays.asList(
                "https://robohash.org/8ad07233fd5e288ed6ed2c997e4590b1?set=set4&bgset=bg1&size=200x200",
                "https://robohash.org/92ddbe68f1eb993d27733087b3c0feea?set=set4&bgset=bg1&size=200x200",
                "https://robohash.org/5adb1181664f6be7034e845fc7cba87b?set=set4&bgset=bg1&size=200x200",
                "https://robohash.org/3047b2f6f2c4207d360c858cd8fd0559?set=set4&bgset=bg1&size=200x200",
                "https://robohash.org/3047b2f6f2c4207d360c858cd8fd0559?set=set2&bgset=bg1&size=200x200",
                "https://robohash.org/0f8fcb99ac925f90a401bd6e3456478e?set=set2&bgset=bg1&size=200x200",
                "https://robohash.org/1d8e186be25a806084c1bc385218f57b?set=set2&bgset=bg1&size=200x200",
                "https://robohash.org/2e4612a69e112dcab64026c9ca85f049?set=set3&bgset=bg1&size=200x200",
                "https://robohash.org/98ce7bb20baca8864ead1313b700b3d4?set=set3&bgset=bg1&size=200x200",
                "https://robohash.org/9c5615494d9d9cfa79dfa45cb6a4cad1?set=set1&bgset=bg1&size=200x200",
                "https://robohash.org/654d51bf7f78228b268bd9e8e2f2d063?set=set1&bgset=bg1&size=200x200",
                "https://robohash.org/110befb4bd5e353c2ba86dd195ddc91d?set=set1&bgset=bg1&size=200x200"
        );

        // Show a dialog with classic avatars to choose from
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Avatar");

        // Use the custom adapter to display the avatars
        AvatarAdapter avatarAdapter = new AvatarAdapter(this, classicAvatars);
        builder.setAdapter(avatarAdapter, (dialog, which) -> {
            // The 'which' argument contains the index position of the selected item
            profilePictureUrl = classicAvatars.get(which);
            Glide.with(this)
                    .load(profilePictureUrl)
                    .placeholder(R.mipmap.default_profile_picture)
                    .error(R.mipmap.default_profile_picture)
                    .into(imgProfilePic);
        });
        builder.show();
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
                        String profilePicture = documentSnapshot.getString("profilepicture");

                        // Update the UI with fetched user data
                        nameEdtText.setText(name);
                        usernameEdtText.setText(username);
                        emailEdtText.setText(email);
                        phone_numberEdtText.setText(phone_number);
                        bioEdtText.setText(bio);

                        // Set the selected county and city in the spinners
                        setCountyAndCitySelection(county, city);

                        if(profilePicture != null && !profilePicture.isEmpty()) {
                            Glide.with(this)
                                    .load(profilePicture)
                                    .placeholder(R.mipmap.default_profile_picture)
                                    .error(R.mipmap.default_profile_picture)
                                    .into(imgProfilePic);
                        }
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
        // First, fetch the cities for the selected county
        Pair<String, String> selectedCounty = countiesGlobal.get(countyIndex);
        if (selectedCounty == null) {
            return;
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                try{
                    AssetManager assetManager = getAssets();
                    String filePath = "counties_and_cities/cities" + selectedCounty.second + ".json";
                    InputStream is = assetManager.open(filePath);
                    StringBuilder stringBuilder = new StringBuilder();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        stringBuilder.append(line);
                    }
                    bufferedReader.close();
                    is.close();

                    String jsonContent = stringBuilder.toString();

                    JSONObject jsonObject = new JSONObject(jsonContent);
                    JSONArray jsonArray = jsonObject.getJSONArray("cities");
                    citiesGlobal.clear();

                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject1 = jsonArray.getJSONObject(i);
                        String cityName = jsonObject1.getString("nume");
                        citiesGlobal.add(cityName);
                        Log.d("City", cityName);
                    }

                    // Update the UI with the fetched cities
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            final List<String> cityNames = new ArrayList<>(citiesGlobal);

                            ArrayAdapter<String> cityAdapter = new ArrayAdapter<>(EditProfileActivity.this, android.R.layout.simple_spinner_item, cityNames);
                            cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            citySpinner.setAdapter(cityAdapter);
                            cityAdapter.notifyDataSetChanged();

                            // Set the selected city
                            int cityIndex = cityNames.indexOf(city);
                            citySpinner.setSelection(cityIndex);
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
