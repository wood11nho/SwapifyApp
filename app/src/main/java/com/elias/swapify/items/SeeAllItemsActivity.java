package com.elias.swapify.items;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.principalactivities.HomePageActivity;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.ProfileActivity;
import com.elias.swapify.R;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Objects;
public class SeeAllItemsActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private SearchView searchView;
    private RecyclerView recyclerViewItems;
    private ImageButton reloadButton;
    private ImageButton profileButton;
    private ArrayList<ItemModel> items;
    private ArrayList<ItemModel> auxItems; // I use this just because items should always contain all items,
    // and auxItems should be the one that gets filtered first time, because otherwise, if you come here
    // with a search query that doesn't return any results, and then you delete the query,
    // you won't see any items, because items will be empty
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private SomeDetailedItemAdapter itemAdapter;
    private boolean isInitialQueryTextChange = true;
    private TextView tvAllItems;
    private ImageButton toggleFiltersButton;
    private ConstraintLayout filtersLayout;
    private Spinner categoryFilterSpinner, locationFilterSpinner;
    private EditText edtMinPriceFilter, edtMaxPriceFilter;
    private Button applyFiltersButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_items);

        btnBack = findViewById(R.id.btnBack_all_items);

        searchView = findViewById(R.id.searchViewAllItems);
        searchView.setQueryHint("Search items, categories, locations");
        ImageView searchIcon = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        searchIcon.setImageDrawable(ContextCompat.getDrawable(SeeAllItemsActivity.this, R.drawable.ic_clear_no_background));
        searchIcon.setBackgroundColor(Color.TRANSPARENT);

        recyclerViewItems = findViewById(R.id.allItemsRecyclerView);
        reloadButton = findViewById(R.id.reload_button_all_items);
        profileButton = findViewById(R.id.profile_button_all_items);
        tvAllItems = findViewById(R.id.allItemsTitle);
        toggleFiltersButton = findViewById(R.id.toggle_filters_button);
        filtersLayout = findViewById(R.id.filter_layout);
        categoryFilterSpinner = findViewById(R.id.spinnerCategoryFilter);
        locationFilterSpinner = findViewById(R.id.spinnerLocationFilter);
        edtMinPriceFilter = findViewById(R.id.etMinPriceFilter);
        edtMaxPriceFilter = findViewById(R.id.etMaxPriceFilter);
        applyFiltersButton = findViewById(R.id.applyFilterButton);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        fetchCategoriesForSpinner(categoryFilterSpinner);
        fetchLocationsForSpinner(locationFilterSpinner);

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
            finish();
        });

        reloadButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllItemsActivity.this, SeeAllItemsActivity.class);
            // Still add the extra "query" or "category" if it exists
            if (getIntent().hasExtra("query")) {
                intent.putExtra("query", getIntent().getStringExtra("query"));
            }
            else if (getIntent().hasExtra("category")) {
                intent.putExtra("category", getIntent().getStringExtra("category"));
            }
            else if (getIntent().hasExtra("location")) {
                intent.putExtra("location", getIntent().getStringExtra("location"));
            }
            startActivity(intent);
            finish();
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllItemsActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // clear the activity stack
            startActivity(intent);
            finish();
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchDataManager.getInstance().saveSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (!isInitialQueryTextChange) {
                    filterItems(newText.toLowerCase(Locale.getDefault()));
                } else{
                    isInitialQueryTextChange = false;
                }
                return false;
            }
        });

        toggleFiltersButton.setOnClickListener(v -> {
            if (filtersLayout.getVisibility() == ConstraintLayout.VISIBLE) {
                filtersLayout.setVisibility(ConstraintLayout.GONE);
                toggleFiltersButton.setImageResource(R.drawable.ic_show_filters);
            } else {
                filtersLayout.setVisibility(ConstraintLayout.VISIBLE);
                toggleFiltersButton.setImageResource(R.drawable.ic_hide_filters);
            }
        });

        applyFiltersButton.setOnClickListener(v -> filterItemsWithFilters());

        if(getIntent().hasExtra("query") || getIntent().hasExtra("category") || getIntent().hasExtra("location")) {
            if (getIntent().hasExtra("query")) {
                String query = getIntent().getStringExtra("query");
                searchView.setQuery(query, true);
                searchView.clearFocus();
                fetchFilteredItems(query);
                isInitialQueryTextChange = false;
            }
            else if (getIntent().hasExtra("category")) {
                String category = getIntent().getStringExtra("category");
                tvAllItems.setText("Items in " + category + " category");
                fetchCategoryItems(category);
                isInitialQueryTextChange = false;
            }
            else if (getIntent().hasExtra("location")) {
                String location = getIntent().getStringExtra("location");
                tvAllItems.setText("Items found in " + location);
                fetchLocationItems(location);
                isInitialQueryTextChange = false;
            }
        }
        else {
            fetchItems();
        }
    }

    private void fetchCategoriesForSpinner(Spinner spinner) {
        firestoreDB.collection("CATEGORIES").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> categories = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String category = documentSnapshot.getString("name");
                        categories.add(category);
                    }
                    categories.add(0, "All categories");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0);
                })
                .addOnFailureListener(e -> {
                    // Show a toast message of failure
                });
    }

    private void fetchLocationsForSpinner(Spinner spinner){
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> locations = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        item.setItemId(documentSnapshot.getId());
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
                            continue;
                        }
                        if (!locations.contains(item.getItemLocation())) {
                            locations.add(item.getItemLocation());
                        }
                    }
                    locations.add(0, "All locations");
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setAdapter(adapter);
                    spinner.setSelection(0);
                })
                .addOnFailureListener(e -> {
                    // Show a toast message of failure
                });
    }

    private void fetchItems() {
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        item.setItemId(documentSnapshot.getId());
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
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

    private void filterItemsWithFilters() {
        String category = categoryFilterSpinner.getSelectedItem().toString();
        String location = locationFilterSpinner.getSelectedItem().toString();
        String minPrice = edtMinPriceFilter.getText().toString();
        String maxPrice = edtMaxPriceFilter.getText().toString();

        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        item.setItemId(documentSnapshot.getId());
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
                            continue;
                        }
                        if (!category.equals("All categories") && !item.getItemCategory().equals(category)) {
                            continue;
                        }
                        if (!location.equals("All locations") && !item.getItemLocation().equals(location)) {
                            continue;
                        }
                        if (!minPrice.isEmpty() && !maxPrice.isEmpty()) {
                            if (item.getItemPrice() < Double.parseDouble(minPrice) || item.getItemPrice() > Double.parseDouble(maxPrice)) {
                                continue;
                            }
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
                        item.setItemId(documentSnapshot.getId());
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
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
                        item.setItemId(documentSnapshot.getId());
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
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

    private void fetchLocationItems(String location) {
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        assert item != null;
                        item.setItemId(documentSnapshot.getId());
                        if (item.getItemUserId().equals(Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid())) {
                            continue;
                        }
                        if (item.getItemLocation().equals(location)) {
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
                if (item.getItemName().toLowerCase().contains(text.toLowerCase()) ||
                        item.getItemCategory().toLowerCase().contains(text.toLowerCase()) ||
                        item.getItemLocation().toLowerCase().contains(text.toLowerCase())) {
                    filteredItems.add(item);
                }
            }
            itemAdapter.filterList(filteredItems);
        }
    }
}