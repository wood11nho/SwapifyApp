package com.example.swapify;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;
import androidx.appcompat.widget.SearchView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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
        recyclerViewCategories = findViewById(R.id.allCategoriesRecyclerView);
        reloadButton = findViewById(R.id.reload_button_all_categories);
        profileButton = findViewById(R.id.profile_button_all_categories);

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

        categories = new ArrayList<>();

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

        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new SomeDetailedCategoryAdapter(this, categories);
        recyclerViewCategories.setAdapter(categoryAdapter);

        fetchCategories();
    }

    private void fetchCategories() {
        firestoreDB.collection("CATEGORIES")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (int i = 0; i < task.getResult().size(); i++) {
                            categories.add(task.getResult().getDocuments().get(i).toObject(CategoryModel.class));
                        }
                        categoryAdapter.notifyDataSetChanged();
                    }
                });
    }

}

