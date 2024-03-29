package com.example.swapify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private ImageButton reloadButton;
    private ImageButton profileButton;
    private ImageButton chatButton;
    private MaterialButton addItemButton;
    private MaterialButton seeAllItemsButton;
    private MaterialButton seeAllCategoriesButton;
    private ArrayList<ItemModel> items;
    private ArrayList<String> categories;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private RecyclerView recyclerViewItems;
    private RecyclerView recyclerViewCategories;
    private ItemAdapter itemAdapter;
    private CategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Start quickly the LoadingScreenActivity
        Intent loadingScreenIntent = new Intent(this, LoadingScreenActivity.class);
        startActivity(loadingScreenIntent);

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

        reloadButton = findViewById(R.id.reloadPageButton);
        chatButton = findViewById(R.id.chat_button);
        profileButton = findViewById(R.id.profile_button);
        addItemButton = findViewById(R.id.addItemButton);
        seeAllItemsButton = findViewById(R.id.seeAllItemsButton);
        seeAllCategoriesButton = findViewById(R.id.seeAllCategoriesButton);

        items = new ArrayList<>();
        categories = new ArrayList<>();

        reloadButton.setOnClickListener(v -> {
            // Change background tint of menu button to purple
            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.purple));
            // Change background tint of profile button to grey
            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.grey));
            Intent intent = new Intent(HomePageActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AllChatsActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        profileButton.setOnClickListener(v -> {
            // Change background tint of profile button to purple
            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.purple));
            // Change background tint of menu button to grey
            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.grey));
            Intent intent = new Intent(HomePageActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        addItemButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, AddItemActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        seeAllItemsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SeeAllItemsActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        seeAllCategoriesButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SeeAllCategoriesActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        // Initialize the RecyclerView for items and its adapter with horizontal layout
        recyclerViewItems = findViewById(R.id.recyclerViewItems);
        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        itemAdapter = new ItemAdapter(items); // Pass the items list to the adapter
        recyclerViewItems.setAdapter(itemAdapter);

        // Initialize the RecyclerView for categories and its adapter with horizontal layout
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(categories); // Pass the categories list to the adapter
        recyclerViewCategories.setAdapter(categoryAdapter);

        // Fetch items from Firestore
        fetchItemsFromFirestore();

        // Fetch categories from Firestore
        fetchCategoriesFromFirestore();
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
                        // I should get only the items that are not mine
                        assert item != null;
                        if (item.getItemUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
                            continue;
                        }
                        items.add(item);
                    }
                    // Notify the adapter that the data has changed
                    itemAdapter.notifyItemChanged(items.size());
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    // For simplicity, we won't handle the error here. You can add appropriate error handling.
                });
    }

    private void fetchCategoriesFromFirestore(){
        firestoreDB.collection("CATEGORIES").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categories.clear(); // Clear the categories list to avoid duplicates when updating the UI
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String category = documentSnapshot.getString("name");
                        categories.add(category);
                    }
                    // Notify the adapter that the data has changed
                    categoryAdapter.notifyItemChanged(categories.size());
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    // For simplicity, we won't handle the error here. You can add appropriate error handling.
                });
    }
}
