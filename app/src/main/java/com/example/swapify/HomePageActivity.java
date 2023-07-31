package com.example.swapify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapify.ItemModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private ImageButton menuButton;
    private ImageButton profileButton;
    private MaterialButton addItemButton;
    private ArrayList<ItemModel> items;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login page
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
            return;
        }

        // Set the welcome message with the username
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        fetchUserData(currentUser.getUid());

        menuButton = findViewById(R.id.menu_button);
        profileButton = findViewById(R.id.profile_button);
        addItemButton = findViewById(R.id.addItemButton);

        items = new ArrayList<>();

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

        // Initialize the RecyclerView and its adapter with horizontal layout
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        itemAdapter = new ItemAdapter(items); // Pass the items list to the adapter
        recyclerView.setAdapter(itemAdapter);

        // Fetch items from Firestore
        fetchItemsFromFirestore();
    }

    private void fetchUserData(String userId) {
        firestoreDB.collection("USERS").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        tvWelcomeMessage.setText("Hi, " + name + " \uD83D\uDE03!");
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    // For simplicity, we won't handle the error here. You can add appropriate error handling.
                });
    }

    private void fetchItemsFromFirestore() {
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear(); // Clear the items list to avoid duplicates when updating the UI
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        items.add(item);
                    }
                    // Notify the adapter that the data has changed
                    itemAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    // For simplicity, we won't handle the error here. You can add appropriate error handling.
                });
    }
}
