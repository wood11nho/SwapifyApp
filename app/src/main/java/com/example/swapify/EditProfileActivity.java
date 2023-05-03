package com.example.swapify;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Arrays;
import java.util.List;

public class EditProfileActivity extends AppCompatActivity {

    private EditText nameEdtText;
    private EditText usernameEdtText;
    private EditText emailEdtText;
    private EditText phone_numberEdtText;
    private EditText bioEdtText;
    private Spinner citySpinner;

    private SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedIstanceState){
        super.onCreate(savedIstanceState);
        setContentView(R.layout.activity_edit_profile);

        nameEdtText = findViewById(R.id.name_edit_text);
        usernameEdtText = findViewById(R.id.username_edit_text);
        emailEdtText = findViewById(R.id.email_edit_text);
        phone_numberEdtText = findViewById(R.id.phone_number_edit_text);
        bioEdtText = findViewById(R.id.bio_edit_text);
        citySpinner = findViewById(R.id.city_spinner);

        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        String name = userPreferences.getString("name", "");
        String username = userPreferences.getString("username", "");
        String email = userPreferences.getString("email", "");
        String phone_number = userPreferences.getString("phone_number", "");
        String bio = userPreferences.getString("bio", "");
        String city = userPreferences.getString("city", "");

        if(!name.isEmpty())
            nameEdtText.setText(name);
        if(!username.isEmpty())
            usernameEdtText.setText(username);
        if(!email.isEmpty())
            emailEdtText.setText(email);
        if(!phone_number.isEmpty())
            phone_numberEdtText.setText(phone_number);
        bioEdtText.setText(bio);
        if(!city.isEmpty())
            citySpinner.setSelection(((ArrayAdapter<String>)citySpinner.getAdapter()).getPosition(city));

        List<String> cities = Arrays.asList(getResources().getStringArray(R.array.cities_array));
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, cities);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(adapter);

        Button saveButton = findViewById(R.id.save_button);
        saveButton.setOnClickListener(v -> {
            SQLiteDatabase db = new DBObject(this).getWritableDatabase();

            SharedPreferences.Editor editor = userPreferences.edit();
            editor.putString("name", nameEdtText.getText().toString());
            editor.putString("username", usernameEdtText.getText().toString());
            editor.putString("email", emailEdtText.getText().toString());
            editor.putString("phone_number", phone_numberEdtText.getText().toString());
            editor.putString("bio", bioEdtText.getText().toString());
            editor.putString("city", citySpinner.getSelectedItem().toString());
            editor.apply();

            ContentValues values = new ContentValues();
            values.put("name", nameEdtText.getText().toString());
            values.put("username", usernameEdtText.getText().toString());
            values.put("email", emailEdtText.getText().toString());
            values.put("phone_number", phone_numberEdtText.getText().toString());
            values.put("bio", bioEdtText.getText().toString());
            values.put("city", citySpinner.getSelectedItem().toString());
            db.update("users", values, "username = ?", new String[]{username});

            db.close();

            Intent intent = new Intent(EditProfileActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });

    }

}
