package com.elias.swapify.principalactivities;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.elias.swapify.R;
import com.elias.swapify.categories.CategoryAdapter;
import com.elias.swapify.categories.SeeAllCategoriesActivity;
import com.elias.swapify.chatbot.ChatbotActivity;
import com.elias.swapify.chats.AllChatsActivity;
import com.elias.swapify.chats.ChatActivity;
import com.elias.swapify.events.AddEventDialogFragment;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.ProfileActivity;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.elias.swapify.items.AddItemActivity;
import com.elias.swapify.items.ItemAdapter;
import com.elias.swapify.items.ItemModel;
import com.elias.swapify.items.SeeAllItemsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;

public class HomePageActivity extends AppCompatActivity {

    private TextView tvWelcomeMessage;
    private ImageButton reloadButton;
    private ImageButton profileButton;
    private ImageButton chatButton;
    private MaterialButton addItemButton;
    private MaterialButton addEventButton;
    private MaterialButton seeAllItemsButton;
    private MaterialButton seeAllCategoriesButton;
    private ArrayList<ItemModel> items;
    private ArrayList<String> categories;
    private RecyclerView recyclerViewItems;
    private RecyclerView recyclerViewCategories;
    private ItemAdapter itemAdapter;
    private CategoryAdapter categoryAdapter;
    private EditText editTextSearch;
    private ImageButton imageButtonSearch;
    private VideoView videoView;
    private ImageButton signOutButton;
    private SwipeRefreshLayout swipeRefreshLayout;
    private FloatingActionButton fabMaps, fabSupport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);

        signOutButton = findViewById(R.id.signOutButton);

        // Start quickly the LoadingScreenActivity
        Intent loadingScreenIntent = new Intent(this, LoadingScreenActivity.class);
        startActivity(loadingScreenIntent);

        if (!FirebaseUtil.isUserLoggedIn()) {
            redirectTo(LoginActivity.class);
            return;
        }

        if (getIntent().getExtras()!=null){
            // from chat notification
            Toast.makeText(this, "Chat notification", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("receiverId", getIntent().getStringExtra("receiverId"));
            intent.putExtra("otherPersonName", getIntent().getStringExtra("otherPersonName"));
            startActivity(intent);
        }

        // Ask for notification permission
        askNotificationPermission();

        // Set the welcome message with the username
        tvWelcomeMessage = findViewById(R.id.tvWelcomeMessage);
        String currentUserId = FirebaseUtil.getCurrentUserId();
        if (currentUserId != null) {
            FirestoreUtil.fetchUserData(currentUserId, new FirestoreUtil.OnUserDataFetchedListener() {
                @Override
                public void onUserDataFetched(String username) {
                    String welcomeMessage = getResources().getString(R.string.welcome_message, username);
                    tvWelcomeMessage.setText(welcomeMessage);
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(HomePageActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                }
            });
        }

        reloadButton = findViewById(R.id.reloadPageButton);
        chatButton = findViewById(R.id.chat_button);
        profileButton = findViewById(R.id.profile_button);
        addItemButton = findViewById(R.id.addItemButton);
        addEventButton = findViewById(R.id.addEventButton);
        seeAllItemsButton = findViewById(R.id.seeAllItemsButton);
        seeAllCategoriesButton = findViewById(R.id.seeAllCategoriesButton);
        editTextSearch = findViewById(R.id.editTextSearch);
        imageButtonSearch = findViewById(R.id.imageButtonSearch);
        videoView = findViewById(R.id.videoView);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        fabMaps = findViewById(R.id.fabMaps);
        fabSupport = findViewById(R.id.fabSupport);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Intent intent = new Intent(HomePageActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish(); // finish the current activity to remove it from the stack
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        items = new ArrayList<>();
        categories = new ArrayList<>();

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

        addEventButton.setOnClickListener(v -> {
            AddEventDialogFragment addEventDialogFragment = AddEventDialogFragment.newInstance();
            addEventDialogFragment.show(getSupportFragmentManager(), "AddEventDialogFragment");
        });

        seeAllItemsButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, SeeAllItemsActivity.class);
            startActivity(intent);
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

        signOutButton.setOnClickListener(v -> {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUtil.signOut();
                    redirectTo(LoginActivity.class);
                } else {
                    Log.e("HomePageActivity", "Error deleting FCM token: " + task.getException());
                }
            });
        });

        fabMaps.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, MapsActivity.class);
            startActivity(intent);
        });

        fabSupport.setOnClickListener(v -> {
            Intent intent = new Intent(HomePageActivity.this, ChatbotActivity.class);
            startActivity(intent);
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
        FirestoreUtil.fetchItemsFromFirestore(new FirestoreUtil.OnItemsFetchedListener() {
            @Override
            public void onItemsFetched(ArrayList<ItemModel> items) {
                HomePageActivity.this.items.clear();
                HomePageActivity.this.items.addAll(items);
                itemAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomePageActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });

        // Fetch categories from Firestore
        FirestoreUtil.fetchCategoriesFromFirestore(new FirestoreUtil.OnCategoriesFetchedListener() {
            @Override
            public void onCategoriesFetched(ArrayList<String> categories) {
                HomePageActivity.this.categories.clear();
                HomePageActivity.this.categories.addAll(categories);
                categoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(HomePageActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void redirectTo(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializeVideoView();
    }

    private void initializeVideoView() {
        Uri uri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.video_shopping);
        videoView.setVideoURI(uri);
        // Check if the video is not already playing before starting
        if (!videoView.isPlaying()) {
            videoView.start();
        }
        videoView.setOnCompletionListener(mp -> videoView.start());
    }

    private void toggleAppThemeChange() {
        SharedPreferences appSettingsPref = getSharedPreferences("AppSettings", MODE_PRIVATE);
        boolean isNightModeOn = appSettingsPref.getBoolean("NightMode", false);
        SharedPreferences.Editor editor = appSettingsPref.edit();
        editor.putBoolean("NightMode", !isNightModeOn);
        editor.apply();

        // Apply the updated theme preference app-wide
        AppCompatDelegate.setDefaultNightMode(!isNightModeOn ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
    }

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Notifications are disabled for Swapify", Toast.LENGTH_LONG).show();
                }
                // Proceed with other app logic after permission handling
                checkUserStatus();
            });

    private void askNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notification permission already granted", Toast.LENGTH_SHORT).show();
                // Proceed with other app logic
                checkUserStatus();
            } else {
                // Show a dialog to ask the user for permission
                new MaterialAlertDialogBuilder(this)
                        .setTitle("Notification Permission")
                        .setMessage("Swapify would like to send you notifications. Is that okay?")
                        .setPositiveButton("Yes", (dialog, which) ->
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS))
                        .setNegativeButton("No", (dialog, which) -> {
                            Toast.makeText(this, "Notifications are disabled for Swapify", Toast.LENGTH_LONG).show();
                            // Proceed with other app logic
                            checkUserStatus();
                        })
                        .show();
            }
        } else {
            // For older Android versions, assume permission is granted
            checkUserStatus();
        }
    }

    private void checkUserStatus() {
        if (!FirebaseUtil.isUserLoggedIn()) {
            redirectTo(LoginActivity.class);
        }
    }
}