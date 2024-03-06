package com.example.swapify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.squareup.picasso.Picasso;

public class FullDetailItemActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private ImageView itemImage;
    private TextView itemName;
    private TextView itemCategory;
    private TextView isForTrade;
    private TextView isForSale;
    private TextView isForAuction;
    private TextView itemPrice;
    private TextView itemDescription;
    private ImageView itemOwnerImage;
    private TextView itemOwnerName;
    private TextView itemOwnerCounty;
    private ImageButton backButton;
    private ImageButton chatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.full_details_item_layout);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();

        itemImage = findViewById(R.id.fullDetailItemImage);
        itemCategory = findViewById(R.id.fullDetailItemCategory);
        itemName = findViewById(R.id.fullDetailItemName);
        isForTrade = findViewById(R.id.tradeTextView);
        isForSale = findViewById(R.id.sellTextView);
        isForAuction = findViewById(R.id.auctionTextView);
        itemPrice = findViewById(R.id.fullDetailItemPrice);
        itemDescription = findViewById(R.id.fullDetailItemDescription);
        itemOwnerImage = findViewById(R.id.userProfileImage);
        itemOwnerName = findViewById(R.id.userNameTextView);
        itemOwnerCounty = findViewById(R.id.userCountyTextView);
        backButton = findViewById(R.id.btnBack_full_detail_item);
        chatButton = findViewById(R.id.messageUserButton);

        Intent currentIntent = getIntent();
        String itemNameString = currentIntent.getStringExtra("itemName");
        int itemPriceInt = currentIntent.getIntExtra("itemPrice", 0);
        String itemCateogryString = currentIntent.getStringExtra("itemCategory");
        String itemDescriptionString = currentIntent.getStringExtra("itemDescription");
        String itemImageUrl = currentIntent.getStringExtra("itemImage");
        boolean isForTradeBoolean = currentIntent.getBooleanExtra("itemIsForTrade", false);
        boolean isForSaleBoolean = currentIntent.getBooleanExtra("itemIsForSale", false);
        boolean isForAuctionBoolean = currentIntent.getBooleanExtra("itemIsForAuction", false);
        String itemOwnerId = currentIntent.getStringExtra("itemUserId");

        // Set the data to the views
        itemName.setText(itemNameString);
        itemCategory.setText(itemCateogryString);
        String itemPriceAux = itemPriceInt + " RON";
        itemPrice.setText(itemPriceAux);
        itemDescription.setText(itemDescriptionString);

        // If the item doesn't have an image the String will look like "" or maybe null
        if (itemImageUrl != null && !itemImageUrl.equals("")) {
            Picasso.get().load(itemImageUrl).into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_question_mark);
        }

        setUserFullName(itemOwnerId);
        setUserCounty(itemOwnerId);

        // Customize the views based on the item's type
        if (isForTradeBoolean) {
            isForTrade.setBackgroundColor(getResources().getColor(R.color.purple));
            isForTrade.setTextColor(getResources().getColor(R.color.white));
        }

        if (isForSaleBoolean) {
            isForSale.setBackgroundColor(getResources().getColor(R.color.purple));
            isForSale.setTextColor(getResources().getColor(R.color.white));
        }

        if (isForAuctionBoolean) {
            isForAuction.setBackgroundColor(getResources().getColor(R.color.purple));
            isForAuction.setTextColor(getResources().getColor(R.color.white));
        }

        // Set the item owner's profile image
        setItemOwnerImage(itemOwnerId);


        backButton.setOnClickListener(v -> {
            // Go back to the previous activity
            finish();
        });

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(FullDetailItemActivity.this, ChatActivity.class);
            intent.putExtra("userId", getIntent().getStringExtra("itemUserId"));
            startActivity(intent);
        });


    }

    private void setUserFullName(String userId) {
        Log.d(userId, "getUserFullName: " + userId);
        DocumentReference docRef = firestoreDB.collection("USERS").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String fullName = document.getString("name");
                    // Do something with fullName, such as setting it to a TextView
                    itemOwnerName.setText(fullName);
                } else {
                    Log.d("FullDetailItemActivity", "No such document");
                }
            } else {
                Log.d("FullDetailItemActivity", "get failed with ", task.getException());
            }
        });
    }

    private void setUserCounty(String userId) {
        Log.d(userId, "getUserCounty: " + userId);
        DocumentReference docRef = firestoreDB.collection("USERS").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String county = document.getString("county");
                    // Do something with county, such as setting it to a TextView
                    itemOwnerCounty.setText(county);
                } else {
                    Log.d("FullDetailItemActivity", "No such document");
                }
            } else {
                Log.d("FullDetailItemActivity", "get failed with ", task.getException());
            }
        });
    }

    private void setItemOwnerImage(String userId) {
        Log.d(userId, "getItemOwnerImage: " + userId);
        DocumentReference docRef = firestoreDB.collection("USERS").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String imageUrl = document.getString("profilepicture");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        // Load and display the owner's profile image using Glide
                        Glide.with(this)
                                .load(imageUrl)
                                .into(itemOwnerImage);
                    }
                } else {
                    Log.d("FullDetailItemActivity", "No such document");
                }
            } else {
                Log.d("FullDetailItemActivity", "get failed with ", task.getException());
            }
        });
    }


}
