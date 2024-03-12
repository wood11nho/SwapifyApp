package com.elias.swapify.principalactivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.R;
import com.elias.swapify.categories.CategoryAdapter;
import com.elias.swapify.categories.CategoryModel;
import com.elias.swapify.categories.SeeAllCategoriesActivity;
import com.elias.swapify.chats.AllChatsActivity;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.ProfileActivity;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.elias.swapify.items.AddItemActivity;
import com.elias.swapify.items.ItemAdapter;
import com.elias.swapify.userpreferences.ItemInteractionManager;
import com.elias.swapify.items.ItemModel;
import com.elias.swapify.userpreferences.PostedItemsManager;
import com.elias.swapify.items.SeeAllItemsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Objects;

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
    private EditText editTextSearch;
    private ImageButton imageButtonSearch;
    private boolean isNightModeOn;
    private SharedPreferences appSettingsPref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appSettingsPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        isNightModeOn = appSettingsPref.getBoolean("NightMode", false);

        if (isNightModeOn) {
            // Change to dark theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            // Change to light theme
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

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
        editTextSearch = findViewById(R.id.editTextSearch);
        imageButtonSearch = findViewById(R.id.imageButtonSearch);

//        if (isNightModeOn) {
//            toggleNightModeButton.setImageResource(R.drawable.ic_day);
//        }
//        else {
//            toggleNightModeButton.setImageResource(R.drawable.ic_night);
//        }

        items = new ArrayList<>();
        categories = new ArrayList<>();

        SearchDataManager.initializeInstance(firestoreDB, currentUser.getUid());
        ItemInteractionManager.initializeInstance(firestoreDB, currentUser.getUid());
        PostedItemsManager.initializeInstance(firestoreDB, currentUser.getUid());

        reloadButton.setOnClickListener(v -> {
//            // Change background tint of menu button to purple
//            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.purple));
//            // Change background tint of profile button to grey
//            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.grey));
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
//            // Change background tint of profile button to purple
//            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.purple));
//            // Change background tint of menu button to grey
//            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(HomePageActivity.this, R.color.grey));
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

        imageButtonSearch.setOnClickListener(
                v -> {
                    String query = editTextSearch.getText().toString();
                    if (query.isEmpty()) {
                        Toast.makeText(HomePageActivity.this, "Please enter a search query", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        // Save the search query for user preferences
                        SearchDataManager.getInstance().saveSearch(query);

                        Intent intent = new Intent(HomePageActivity.this, SeeAllItemsActivity.class);
                        intent.putExtra("query", query);
                        startActivity(intent);
                        finish(); // finish the current activity to remove it from the stack
                    }
                }
        );

//        toggleNightModeButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                isNightModeOn = !isNightModeOn;
//                toggleAppThemeChange();
//            }
//        });

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

//    private void toggleAppThemeChange() {
//        SharedPreferences.Editor editor = appSettingsPref.edit();
//        editor.putBoolean("NightMode", isNightModeOn);
//        editor.apply();
//
//        if (isNightModeOn) {
//            toggleNightModeButton.setImageResource(R.drawable.ic_day);
//        } else {
//            toggleNightModeButton.setImageResource(R.drawable.ic_night);
//        }
//
//        recreate();
//    }

    private void fetchUserData(String userId) {
        firestoreDB.collection("USERS").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        String welcomeMessage = getResources().getString(R.string.welcome_message, username);
                        tvWelcomeMessage.setText(welcomeMessage);

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
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
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
        final int[] count = {0};

        firestoreDB.collection("CATEGORIES")
                .orderBy("numberOfItems", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    categories.clear(); // Clear the categories list to avoid duplicates when updating the UI
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CategoryModel category = documentSnapshot.toObject(CategoryModel.class);
                        assert category != null;
                        if (count[0] == 3) {
                            break;
                        }
                        categories.add(category.getName());
                        count[0]++;
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
