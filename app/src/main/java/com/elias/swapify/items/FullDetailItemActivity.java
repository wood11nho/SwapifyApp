package com.elias.swapify.items;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.elias.swapify.charity.FullDetailCharityActivity;
import com.elias.swapify.chats.ChatActivity;
import com.elias.swapify.R;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Date;

public class FullDetailItemActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private ImageView itemImage;
    private TextView itemName;
    private TextView itemCategory;
    private TextView isForTrade;
    private TextView isForSale;
    private TextView isForCharity;
    private TextView itemPrice;
    private TextView itemDescription;
    private ImageView itemOwnerImage;
    private TextView itemOwnerName;
    private TextView itemOwnerCounty;
    private TextView itemLocation;
    private ImageButton backButton;
    private ImageButton chatButton;
    private ImageButton wishlistButton;
    private LinearLayout charityLayout;
    private ImageView charityImage;
    private TextView charityName;
    private TextView charityDescription;

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
        isForCharity = findViewById(R.id.charityTextView);
        itemPrice = findViewById(R.id.fullDetailItemPrice);
        itemDescription = findViewById(R.id.fullDetailItemDescription);
        itemOwnerImage = findViewById(R.id.userProfileImage);
        itemOwnerName = findViewById(R.id.userNameTextView);
        itemOwnerCounty = findViewById(R.id.userCountyTextView);
        backButton = findViewById(R.id.btnBack_full_detail_item);
        chatButton = findViewById(R.id.messageUserButton);
        wishlistButton = findViewById(R.id.addToWishlistButton);
        itemLocation = findViewById(R.id.fullDetailItemLocation);
        charityLayout = findViewById(R.id.charityll);
        charityImage = findViewById(R.id.charityItemImage);
        charityName = findViewById(R.id.charityItemName);
        charityDescription = findViewById(R.id.charityItemDescription);


        Intent currentIntent = getIntent();
        String itemNameString = currentIntent.getStringExtra("itemName");
        int itemPriceInt = currentIntent.getIntExtra("itemPrice", 0);
        String itemCateogryString = currentIntent.getStringExtra("itemCategory");
        String itemDescriptionString = currentIntent.getStringExtra("itemDescription");
        String itemImageUrl = currentIntent.getStringExtra("itemImage");
        boolean isForTradeBoolean = currentIntent.getBooleanExtra("itemIsForTrade", false);
        boolean isForSaleBoolean = currentIntent.getBooleanExtra("itemIsForSale", false);
        boolean isForCharityBoolean = currentIntent.getBooleanExtra("itemIsForCharity", false);
        String itemOwnerId = currentIntent.getStringExtra("itemUserId");
        String itemId = currentIntent.getStringExtra("itemId");
        String itemLocationString = currentIntent.getStringExtra("itemLocation");
        String itemCharityId = currentIntent.getStringExtra("itemCharityId");

        // Set the data to the views
        itemName.setText(itemNameString);
        itemCategory.setText(itemCateogryString);
        String itemPriceAux = itemPriceInt + " RON";
        itemPrice.setText(itemPriceAux);
        itemDescription.setText(itemDescriptionString);
        itemLocation.setText(itemLocationString);

        // If the item doesn't have an image the String will look like "" or maybe null
        if (itemImageUrl != null && !itemImageUrl.equals("")) {
            Glide.with(this)
                    .load(itemImageUrl)
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.ic_question_mark)
                    .error(R.drawable.ic_question_mark)
                    .into(itemImage);
        } else {
            itemImage.setImageResource(R.drawable.ic_question_mark);
        }

        setUserFullName(itemOwnerId);
        setUserCounty(itemOwnerId);

        // Customize the views based on the item's type
        if (isForTradeBoolean) {
            isForTrade.setBackgroundColor(getResources().getColor(R.color.purple));
            isForTrade.setTextColor(getResources().getColor(R.color.white));
            charityLayout.setVisibility(LinearLayout.GONE);
        }

        if (isForSaleBoolean) {
            isForSale.setBackgroundColor(getResources().getColor(R.color.purple));
            isForSale.setTextColor(getResources().getColor(R.color.white));
            charityLayout.setVisibility(LinearLayout.GONE);
        }

        if (isForCharityBoolean) {
            isForCharity.setBackgroundColor(getResources().getColor(R.color.purple));
            isForCharity.setTextColor(getResources().getColor(R.color.white));
            fetchCharityData(itemCharityId);
            charityLayout.setVisibility(LinearLayout.VISIBLE);
        }

        // Set the item owner's profile image
        setItemOwnerImage(itemOwnerId);

        // If item is already in wishlist, change the icon to @drawable/ic_remove_from_wishlist
        FirestoreUtil.isItemInWishlist(
                FirebaseUtil.getCurrentUserId(),
                itemId,
                new FirestoreUtil.WishlistCheckCallback() {
                    @Override
                    public void onItemInWishlist(boolean isInWishlist) {
                        if (isInWishlist) {
                            wishlistButton.setImageResource(R.drawable.ic_remove_from_wishlist);
                        } else {
                            wishlistButton.setImageResource(R.drawable.ic_add_to_wishlist);
                        }
                    }
                }
        );

        backButton.setOnClickListener(v -> {
            // Go back to the previous activity
            finish();
        });

        chatButton.setOnClickListener(v -> {
            Intent intent = new Intent(FullDetailItemActivity.this, ChatActivity.class);
            intent.putExtra("receiverId", getIntent().getStringExtra("itemUserId"));
            intent.putExtra("otherPersonName", itemOwnerName.getText().toString());
            startActivity(intent);
        });

        wishlistButton.setOnClickListener(v -> {
            // Add the item to the user's wishlist or remove it if it's already there
            FirestoreUtil.isItemInWishlist(
                    FirebaseUtil.getCurrentUserId(),
                    itemId,
                    new FirestoreUtil.WishlistCheckCallback() {
                        @Override
                        public void onItemInWishlist(boolean isInWishlist) {
                            if (isInWishlist) {
                                removeFromWishlist(itemId);
                                wishlistButton.setImageResource(R.drawable.ic_add_to_wishlist);
                            } else {
                                addToWishlist(itemId);
                                wishlistButton.setImageResource(R.drawable.ic_remove_from_wishlist);
                            }
                        }
                    }
            );
        });

        charityLayout.setOnClickListener(v -> {
            Intent intent = new Intent(FullDetailItemActivity.this, FullDetailCharityActivity.class);
            intent.putExtra("charityId", itemCharityId);
            startActivity(intent);
        });

    }

    private void fetchCharityData(String charityId) {
        DocumentReference docRef = firestoreDB.collection("CHARITIES").document(charityId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String charityNameString = document.getString("charityName");
                    String charityDescriptionString = document.getString("charityDescription");
                    String charityImageString = document.getString("charityImage");

                    charityName.setText(charityNameString);
                    charityDescription.setText(charityDescriptionString);

                    // Load it with Glide if it is an image
                    if (charityImageString != null && !charityImageString.isEmpty()) {
                        Glide.with(this)
                                .load(charityImageString)
                                .into(charityImage);
                    }
                } else {
                    Toast.makeText(FullDetailItemActivity.this, "Failed to fetch charity data.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(FullDetailItemActivity.this, "Failed to fetch charity data.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setUserFullName(String userId) {
        DocumentReference docRef = firestoreDB.collection("USERS").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String fullName = document.getString("name");
                    // Do something with fullName, such as setting it to a TextView
                    itemOwnerName.setText(fullName);
                } else {
                }
            } else {
            }
        });
    }

    private void setUserCounty(String userId) {
        DocumentReference docRef = firestoreDB.collection("USERS").document(userId);
        docRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    String county = document.getString("county");
                    // Do something with county, such as setting it to a TextView
                    itemOwnerCounty.setText(county);
                } else {
                }
            } else {
            }
        });
    }

    private void setItemOwnerImage(String userId) {
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
                }
            } else {
            }
        });
    }

    private void addToWishlist(String itemId) {
        // Add the item to the user's wishlist
        FirestoreUtil.addItemToWishlist(
                FirebaseUtil.getCurrentUserId(),
                itemId,
                new Date(), // Current date as the addedOn date
                new FirestoreUtil.WishlistUpdateCallback() {
                    @Override
                    public void onWishlistUpdated() {
                        // Handle the successful update, maybe show a message to the user
                        Toast.makeText(FullDetailItemActivity.this, "Item added to wishlist!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWishlistUpdateFailed(Exception e) {
                        // Handle the error, maybe show an error message to the user
                        Toast.makeText(FullDetailItemActivity.this, "Failed to add item to wishlist.", Toast.LENGTH_SHORT).show();
                        Log.e("Wishlist", "Error adding item to wishlist", e);
                    }
                }
        );
    }

    private void removeFromWishlist(String itemId) {
        // Remove the item from the user's wishlist
        FirestoreUtil.removeItemFromWishlist(
                FirebaseUtil.getCurrentUserId(),
                itemId,
                new FirestoreUtil.WishlistUpdateCallback() {
                    @Override
                    public void onWishlistUpdated() {
                        // Handle the successful update, maybe show a message to the user
                        Toast.makeText(FullDetailItemActivity.this, "Item removed from wishlist!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWishlistUpdateFailed(Exception e) {
                        // Handle the error, maybe show an error message to the user
                        Log.e("Wishlist", "Error removing item from wishlist", e);
                        Toast.makeText(FullDetailItemActivity.this, "Failed to remove item from wishlist.", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }
}
