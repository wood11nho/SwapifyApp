package com.example.swapify;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

// This activity is used to display all the items in the database
// If the user comes here from the home page by pressing the See All Items button, all the items are displayed
// If the user comes here from the search bar, only the items that match the search query are displayed
public class SeeAllItemsActivity extends AppCompatActivity{
    private ImageButton btnBack;
    private SearchView searchView;
    private RecyclerView recyclerViewItems;
    private ImageButton reloadButton;
    private ImageButton profileButton;
    private ArrayList<ItemModel> items;
    private ArrayList<ItemModel> auxItems; // I use this just because items should always contain all items, and auxItems should be
    // the one that gets filtered first time, because otherwise, if you come here with a search query that doesn't return any results,
    // and then you delete the query, you won't see any items, because items will be empty
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private SomeDetailedItemAdapter itemAdapter;
    private boolean isInitialQueryTextChange = true;
    private TextView tvAllItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_items);

        btnBack = findViewById(R.id.btnBack_all_items);
        searchView = findViewById(R.id.searchViewAllItems);
        recyclerViewItems = findViewById(R.id.allItemsRecyclerView);
        reloadButton = findViewById(R.id.reload_button_all_items);
        profileButton = findViewById(R.id.profile_button_all_items);
        tvAllItems = findViewById(R.id.allItemsTitle);

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

        items = new ArrayList<>();
        auxItems = new ArrayList<>();

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllItemsActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        reloadButton.setOnClickListener(v -> {
//            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.purple));
//            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.grey));

            Intent intent = new Intent(SeeAllItemsActivity.this, SeeAllItemsActivity.class);
            // Still add the extra "query" or "category" if it exists
            if (getIntent().hasExtra("query")) {
                intent.putExtra("query", getIntent().getStringExtra("query"));
            }
            else if (getIntent().hasExtra("category")) {
                intent.putExtra("category", getIntent().getStringExtra("category"));
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // clear the activity stack
            startActivity(intent);
            finish();
        });

        profileButton.setOnClickListener(v -> {
//            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.purple));
//            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.grey));

            Intent intent = new Intent(SeeAllItemsActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // clear the activity stack
            startActivity(intent);
            finish();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SeeCategoriesActivity", "onQueryTextChange: " + newText);
                if (!isInitialQueryTextChange) {
                    filterItems(newText.toLowerCase(Locale.getDefault()));
                } else{
                    isInitialQueryTextChange = false;
                }
                return false;
            }
        });

        if(getIntent().hasExtra("query") || getIntent().hasExtra("category")) {
            if (getIntent().hasExtra("query")) {
                String query = getIntent().getStringExtra("query");
                searchView.setQuery(query, true);
                searchView.clearFocus();
                fetchFilteredItems(query);
                isInitialQueryTextChange = false;
            }
            else {
                String category = getIntent().getStringExtra("category");
                tvAllItems.setText(category);
                fetchCategoryItems(category);
                isInitialQueryTextChange = false;
            }
        }
        else {
            fetchItems();
        }
    }

    private void fetchItems() {
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        if (item.getItemUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
                            continue;
                        }
                        items.add(item);
                    }

                    recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
                    itemAdapter = new SomeDetailedItemAdapter(this, items);
                    recyclerViewItems.setAdapter(itemAdapter);

                    itemAdapter.notifyItemChanged(items.size());
                })
                .addOnFailureListener(e -> {
                    // Show a toast message of failure
                });
    }

    private void fetchFilteredItems(String query){
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    auxItems.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        if (item.getItemUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
                            continue;
                        }
                        items.add(item);
                        if (item.getItemName().toLowerCase().contains(query.toLowerCase())) {
                            auxItems.add(item);
                        }
                    }

                    recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
                    itemAdapter = new SomeDetailedItemAdapter(this, auxItems);
                    recyclerViewItems.setAdapter(itemAdapter);

                    itemAdapter.notifyItemChanged(auxItems.size());
                })
                .addOnFailureListener(e -> {
                    // Show a toast message of failure
                });
    }

    private void fetchCategoryItems(String category) {
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        if (item.getItemUserId().equals(firebaseAuth.getCurrentUser().getUid())) {
                            continue;
                        }
                        if (item.getItemCategory().equals(category)) {
                            items.add(item);
                        }
                    }

                    recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
                    itemAdapter = new SomeDetailedItemAdapter(this, items);
                    recyclerViewItems.setAdapter(itemAdapter);

                    itemAdapter.notifyItemChanged(items.size());
                })
                .addOnFailureListener(e -> {
                    // Show a toast message of failure
                });
    }

    private void filterItems(String text) {
        if (itemAdapter != null) {
            ArrayList<ItemModel> filteredItems = new ArrayList<>();
            for (ItemModel item : items) {
                if (item.getItemName().toLowerCase().contains(text.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
            itemAdapter.filterList(filteredItems);
        }
    }

}
