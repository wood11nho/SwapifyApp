package com.elias.swapify.users;

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

import com.bumptech.glide.Glide;
import com.elias.swapify.items.MyAdsActivity;
import com.elias.swapify.principalactivities.HomePageActivity;
import com.elias.swapify.R;
import com.elias.swapify.wishlists.WishlistActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {
    private ImageView imgProfilePic;
    private TextView txtName, txtUsername, txtEmail, txtPhone_number, txtBio, txtCity, txtCounty;
    private Button editProfileButton, seeMyAdsButton;
    private MaterialButton logoutButton;
    private ImageButton btnBack, btnWishlist;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private String userCounty;
    private String userCity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        imgProfilePic = findViewById(R.id.profile_picture);
        txtName = findViewById(R.id.name_text);
        txtUsername = findViewById(R.id.username_text);
        txtEmail = findViewById(R.id.email_text);
        txtPhone_number = findViewById(R.id.phone_number_text);
        txtBio = findViewById(R.id.bio_text);
        txtCounty = findViewById(R.id.county_text);
        txtCity = findViewById(R.id.city_text);
        editProfileButton = findViewById(R.id.edit_profile_button);
        seeMyAdsButton = findViewById(R.id.see_my_ads_button);
        logoutButton = findViewById(R.id.logout_button);
        btnBack = findViewById(R.id.btnBack);
        btnWishlist = findViewById(R.id.btnWishlist);

        String userId = firebaseAuth.getCurrentUser().getUid();
        fetchUserData(userId);

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
                intent.putExtra("userCounty", userCounty);
                intent.putExtra("userCity", userCity);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
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

                // Sign out the user
                firebaseAuth.signOut();

                // Redirect the user to the login screen
                Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });

        btnWishlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, WishlistActivity.class);
                startActivity(intent);
            }
        });

        seeMyAdsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, MyAdsActivity.class);
                startActivity(intent);
            }
        });

    }

    private void fetchUserData(String userId) {
        firestoreDB.collection("USERS").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch user data from Firestore document
                        String name = documentSnapshot.getString("name");
                        String username = documentSnapshot.getString("username");
                        String email = documentSnapshot.getString("email");
                        String phone_number = documentSnapshot.getString("phonenumber");
                        String bio = documentSnapshot.getString("bio");
                        String county = documentSnapshot.getString("county");
                        String city = documentSnapshot.getString("city");
                        String profile_picture = documentSnapshot.getString("profilepicture");


                        // Update the UI with fetched user data
                        txtName.setText("Name: " + name);
                        txtUsername.setText("Username: " + username);
                        txtEmail.setText("Email: " + email);
                        txtPhone_number.setText("Phone Number: " + phone_number);
                        txtBio.setText("Bio: " + bio);
                        txtCounty.setText("County: " + county);
                        txtCity.setText("City: " + city);
                        userCounty = county;
                        userCity = city;

                        if (profile_picture != null && !profile_picture.isEmpty()) {
                            Glide.with(this)
                                    .load(profile_picture)
                                    .placeholder(R.mipmap.defaultpicture)
                                    .error(R.mipmap.defaultpicture)
                                    .into(imgProfilePic);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("ProfileActivity", "Error fetching user data: " + e.getMessage());
                });
    }


}
