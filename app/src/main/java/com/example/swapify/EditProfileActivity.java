package com.example.swapify;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEdtText;
    private EditText usernameEdtText;
    private EditText emailEdtText;
    private EditText phone_numberEdtText;
    private EditText bioEdtText;
    private Spinner countySpinner;
    private Spinner citySpinner;

    private SharedPreferences userPreferences;

    private List<Pair<String, String>> countiesGlobal = new ArrayList<>();
    private List<String> citiesGlobal = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedIstanceState){
        super.onCreate(savedIstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEdtText = findViewById(R.id.name_edit_text);
        usernameEdtText = findViewById(R.id.username_edit_text);
        emailEdtText = findViewById(R.id.email_edit_text);
        phone_numberEdtText = findViewById(R.id.phone_number_edit_text);
        bioEdtText = findViewById(R.id.bio_edit_text);
        countySpinner = findViewById(R.id.county_spinner);
        citySpinner = findViewById(R.id.city_spinner);

        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String name = userPreferences.getString("name", "");
        String username = userPreferences.getString("username", "");
        String email = userPreferences.getString("email", "");
        String phone_number = userPreferences.getString("phone_number", "");
        String bio = userPreferences.getString("bio", "");
        String county = userPreferences.getString("county", "");
        String city = userPreferences.getString("city", "");

        if(!name.isEmpty())
            nameEdtText.setText(name);
        if(!username.isEmpty())
            usernameEdtText.setText(username);
        if(!email.isEmpty())
            emailEdtText.setText(email);
        if(!phone_number.isEmpty())
            phone_numberEdtText.setText(phone_number);
        if(!bio.isEmpty())
            bioEdtText.setText(bio);


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
            SQLiteDatabase db = new DBObject(this).getWritableDatabase();

            String newEmail = emailEdtText.getText().toString();
            String newPhoneNumber = phone_numberEdtText.getText().toString();

            // Check if the new email is already taken
            boolean isEmailTaken = isEmailTaken(newEmail, username);
            if (isEmailTaken) {
                return;
            }

            // Check if the new phone number is already taken
            boolean isPhoneNumberTaken = isPhoneNumberTaken(newPhoneNumber, username);
            if (isPhoneNumberTaken) {

                return;
            }

            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString("name", nameEdtText.getText().toString());
            editor.putString("username", usernameEdtText.getText().toString());
            editor.putString("email", emailEdtText.getText().toString());
            editor.putString("phone_number", phone_numberEdtText.getText().toString());
            editor.putString("bio", bioEdtText.getText().toString());
            editor.putString("county", countySpinner.getSelectedItem().toString());
            editor.putString("city", citySpinner.getSelectedItem().toString());
            editor.apply();

            ContentValues values = new ContentValues();
            values.put("name", nameEdtText.getText().toString());
            values.put("username", usernameEdtText.getText().toString());
            values.put("email", emailEdtText.getText().toString());
            values.put("phone_number", phone_numberEdtText.getText().toString());
            values.put("bio", bioEdtText.getText().toString());
            values.put("county", countySpinner.getSelectedItem().toString());
            values.put("city", citySpinner.getSelectedItem().toString());
            db.update("users", values, "username = ?", new String[]{username});

            db.close();

            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private boolean isPhoneNumberTaken(String newPhoneNumber, String currentUsername) {
        SQLiteDatabase db = new DBObject(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE phone_number = ? AND username != ?", new String[]{newPhoneNumber, currentUsername});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "This phone number is already taken", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    private boolean isEmailTaken(String newEmail, String currentUsername) {
        SQLiteDatabase db = new DBObject(this).getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM users WHERE email = ? AND username != ?", new String[]{newEmail, currentUsername});
        if (cursor.getCount() > 0) {
            Toast.makeText(this, "This email is already taken", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }
}
