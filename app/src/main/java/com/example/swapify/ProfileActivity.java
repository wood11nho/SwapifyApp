package com.example.swapify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgProfilePic;
    private TextView txtName, txtUsername, txtEmail, txtPhone_number, txtBio, txtCity, txtCounty;
    private Button editProfileButton;
    private MaterialButton logoutButton;
    private ImageButton btnBack;

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
        txtCounty = findViewById(R.id.county_text);
        txtCity = findViewById(R.id.city_text);
        editProfileButton = findViewById(R.id.edit_profile_button);
        logoutButton = findViewById(R.id.logout_button);
        btnBack = findViewById(R.id.btnBack);

        SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        Log.d("profileactivity", preferences.toString());
        String name = preferences.getString("name", "");
        String username = preferences.getString("username", "");
        String email = preferences.getString("email", "");
        String phone_number = preferences.getString("phone_number", "");
        String bio = preferences.getString("bio", "");
        String county = preferences.getString("county", "");
        String city = preferences.getString("city", "");

        txtName.setText("Name: " + name);
        txtUsername.setText("Username: " + username);
        txtEmail.setText("Email: " + email);
        txtPhone_number.setText("Phone Number: " + phone_number);
        txtBio.setText("Bio: " + bio);
        txtCounty.setText("County: " + county);
        txtCity.setText("City: " + city);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        editProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
                startActivity(intent);
                finish();
            }
        });

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Clear the user preferences
                SharedPreferences preferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = preferences.edit();
                editor.clear();
                editor.apply();

                // Redirect the user to the login screen
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

    }
}
