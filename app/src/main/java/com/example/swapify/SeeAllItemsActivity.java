package com.example.swapify;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

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

public class SeeAllItemsActivity extends AppCompatActivity{
    private ImageButton btnBack;
    private SearchView searchView;
    private RecyclerView recyclerViewItems;
    private ImageButton reloadButton;
    private ImageButton profileButton;
    private ArrayList<ItemModel> items;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private SomeDetailedItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_see_all_items);

        btnBack = findViewById(R.id.btnBack_all_items);
        searchView = findViewById(R.id.searchViewAllItems);
        recyclerViewItems = findViewById(R.id.allItemsRecyclerView);
        reloadButton = findViewById(R.id.reload_button_all_items);
        profileButton = findViewById(R.id.profile_button_all_items);

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

        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(SeeAllItemsActivity.this, HomePageActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        reloadButton.setOnClickListener(v -> {
            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.purple));
            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.grey));

            Intent intent = new Intent(SeeAllItemsActivity.this, SeeAllItemsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // clear the activity stack
            startActivity(intent);
            finish();
        });

        profileButton.setOnClickListener(v -> {
            profileButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.purple));
            reloadButton.setBackgroundTintList(ContextCompat.getColorStateList(SeeAllItemsActivity.this, R.color.grey));

            Intent intent = new Intent(SeeAllItemsActivity.this, ProfileActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK); // clear the activity stack
            startActivity(intent);
            finish();
        });

//        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
//            @Override
//            public boolean onQueryTextSubmit(String query) { return false; }
//
//            @Override
//            public boolean onQueryTextChange(String newText) {
//                itemAdapter.getFilter().filter(newText);
//                return false;
//            }
//        });

        recyclerViewItems.setLayoutManager(new LinearLayoutManager(this));
        itemAdapter = new SomeDetailedItemAdapter(items);
        recyclerViewItems.setAdapter(itemAdapter);

        fetchItems();
    }

    private void fetchItems() {
        firestoreDB.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    items.clear();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        items.add(item);
                    }
                    itemAdapter.notifyItemChanged(items.size());
                })
                .addOnFailureListener(e -> {
                    // Show a toast message of failure
                });
    }
}
