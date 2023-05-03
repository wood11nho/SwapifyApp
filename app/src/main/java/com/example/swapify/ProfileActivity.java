package com.example.swapify;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgProfilePic;
    private TextView txtName, txtUsername, txtEmail, txtPhone_number, txtBio, txtCountry;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        imgProfilePic = findViewById(R.id.profile_picture);
        txtName = findViewById(R.id.name_text);
        txtUsername = findViewById(R.id.username_text);
        txtEmail = findViewById(R.id.email_text);
        txtPhone_number = findViewById(R.id.phone_number_text);
        txtBio = findViewById(R.id.bio_text);
        txtCountry = findViewById(R.id.country_text);

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        String name = preferences.getString("name", "");
        String username = preferences.getString("username", "");
        String email = preferences.getString("email", "");
        String phone_number = preferences.getString("phone_number", "");
        String bio = preferences.getString("bio", "");
        String country = preferences.getString("country", "");

        txtName.setText("Name: " + name);
        txtUsername.setText("Username: " + username);
        txtEmail.setText("Email: " + email);
        txtPhone_number.setText("Phone Number: " + phone_number);
        txtBio.setText("Bio: " + bio);
        txtCountry.setText("Country: " + country);
    }
}
