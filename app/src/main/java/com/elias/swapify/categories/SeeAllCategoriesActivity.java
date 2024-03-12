package com.elias.swapify.categories;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.principalactivities.HomePageActivity;
import com.elias.swapify.R;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Locale;

public class SeeAllCategoriesActivity extends AppCompatActivity {
    private ImageButton btnBack;
    private SearchView searchView;
    private RecyclerView recyclerViewCategories;
    private ImageButton reloadButton;
    private ImageButton profileButton;
    private ArrayList<CategoryModel> categories;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private SomeDetailedCategoryAdapter categoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_categories);

        btnBack = findViewById(R.id.btnBack_all_categories);
        searchView = findViewById(R.id.searchViewAllCategories);
        ImageView closeButton = searchView.findViewById(androidx.appcompat.R.id.search_close_btn);
        closeButton.setImageDrawable(ContextCompat.getDrawable(SeeAllCategoriesActivity.this, R.drawable.ic_clear_no_background));
        closeButton.setBackgroundColor(Color.TRANSPARENT);
        recyclerViewCategories = findViewById(R.id.allCategoriesRecyclerView);
        reloadButton = findViewById(R.id.reload_button_all_categories);
        profileButton = findViewById(R.id.profile_button_all_categories);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        categories = new ArrayList<>();
        fetchCategories();

        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        if (currentUser == null) {
            // User not authenticated, redirect to login page
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
            return;
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                SearchDataManager.getInstance().saveSearch(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("SeeCategoriesActivity", "onQueryTextChange: " + newText);
                filterCategories(newText.toLowerCase(Locale.getDefault()));
                return false;
            }
        });

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllCategoriesActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        reloadButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllCategoriesActivity.this, SeeAllCategoriesActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });

        profileButton.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllCategoriesActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        });
    }

    private void fetchCategories() {
        firestoreDB.collection("CATEGORIES")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (int i = 0; i < task.getResult().size(); i++) {
                            categories.add(task.getResult().getDocuments().get(i).toObject(CategoryModel.class));
                            Log.d("SeeCategoriesActivity", "fetchCategories: " + categories.get(i).getName());
                        }
                        // Set the RecyclerView
                        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
                        //        Log.d("SeeCategoriesActivity", "onCreate: recyclerViewCategories");
                        categoryAdapter = new SomeDetailedCategoryAdapter(this, categories);
                        //        Log.d("SeeCategoriesActivity", "onCreate: categoryAdapter");
                        recyclerViewCategories.setAdapter(categoryAdapter);
                        //        Log.d("SeeCategoriesActivity", "onCreate: setAdapter");
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.d("SeeCategoriesActivity", "Error getting documents: ", task.getException());
                    }
                });
    }

    private void filterCategories(String text) {
        Log.d("SeeCategoriesActivity", "filterCategories: " + text);
        ArrayList<CategoryModel> filteredList = new ArrayList<>();
        for (CategoryModel category : categories) {
            if (category.getName().toLowerCase(Locale.getDefault()).contains(text)) {
                filteredList.add(category);
            }
        }
        categoryAdapter.filterList(filteredList);
    }
}

