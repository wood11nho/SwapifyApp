package com.elias.swapify.items;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.R;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.elias.swapify.users.ProfileActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyAdsActivity extends AppCompatActivity implements EditItemDialogFragment.OnItemUpdatedListener {
    ImageButton btnBack;
    RecyclerView recyclerViewMyAds;
    ImageButton btnReload;
    ImageButton btnProfile;
    EditItemAdapter editItemAdapter;
    ArrayList<ItemModel> itemsPostedByUser = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ads);

        btnBack = findViewById(R.id.btnBack_my_ads);
        recyclerViewMyAds = findViewById(R.id.myAdsRecyclerView);
        btnReload = findViewById(R.id.reload_button_my_ads);
        btnProfile = findViewById(R.id.profile_button_my_ads);
        btnBack.setOnClickListener(v -> finish());
        btnReload.setOnClickListener(v -> {
            Intent intent = new Intent(MyAdsActivity.this, MyAdsActivity.class);
            startActivity(intent);
            finish();
        });
        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MyAdsActivity.this, ProfileActivity.class);
            startActivity(intent);
            finish();
        });
        recyclerViewMyAds.setLayoutManager(new LinearLayoutManager(this));
        editItemAdapter = new EditItemAdapter(this, itemsPostedByUser);
        recyclerViewMyAds.setAdapter(editItemAdapter);

        fetchUserItems();
    }

    @Override
    public void onItemUpdated(){
        fetchUserItems();
    }

    private void fetchUserItems() {
        FirestoreUtil.fetchUserItems(FirebaseUtil.getCurrentUserId(), new FirestoreUtil.OnUserItemsFetchedListener() {
            @Override
            public void onUserItemsFetched(List<DocumentSnapshot> userItems) {
                editItemAdapter.updateItems(userItems);
            }

            @Override
            public void onError(String error) {
                // Handle the error, e.g., show a toast or log it
            }
        });
    }
}