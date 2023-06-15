package com.example.swapify;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private ImageButton menuButton;
    private ImageButton profileButton;
    private MaterialButton addItemButton;
    private ArrayList<ItemModel> items;
    private DBObject db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        SessionManager sessionManager = new SessionManager(this);

        if (!sessionManager.isLoggedIn()) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
            return;
        }

        // Get the username from the session manager
        String username = sessionManager.getUsername();

        // Set the welcome message with the username
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        tvWelcomeMessage.setText("Hi, " + username + " \uD83D\uDE03!");

        menuButton = findViewById(R.id.menu_button);
        profileButton = findViewById(R.id.profile_button);
        addItemButton = findViewById(R.id.addItemButton);

        db = new DBObject(this);

        items = db.getAllItems();

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change background tint of menu button to purple
                menuButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.purple));
                // Change background tint of profile button to grey
                profileButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.grey));
                Intent intent = new Intent(HomePageActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish(); // finish the current activity to remove it from the stack
            }
        });

        profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Change background tint of profile button to purple
                profileButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.purple));
                // Change background tint of menu button to grey
                menuButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.grey));
                Intent intent = new Intent(HomePageActivity.this, ProfileActivity.class);
                startActivity(intent);
            }
        });

        addItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(HomePageActivity.this, AddItemActivity.class);
                startActivity(intent);
            }
        });

        RecyclerView recommendationRecyclerView = findViewById(R.id.recommendationRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recommendationRecyclerView.setLayoutManager(layoutManager);

//        RecommendationAdapter recommendationAdapter = new RecommendationAdapter();
//        recommendationRecyclerView.setAdapter(recommendationAdapter);
//
//        // Get the recommended items from the database or any other source
//        ArrayList<ItemModel> recommendedItems = new ArrayList<>();
//
//        // Set the recommended items on the adapter
//        recommendationAdapter.setItems(recommendedItems);
    }

}
