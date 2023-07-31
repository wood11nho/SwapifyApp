package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class AddItemActivity extends AppCompatActivity {

    private ImageView itemPictureImageView;
    private Button changePictureButton;
    private EditText itemNameEditText;
    private EditText itemDescriptionEditText;
    private Spinner itemCategorySpinner;
    private EditText itemPriceEditText;
    private RadioGroup itemTypeRadioGroup;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize views
        itemPictureImageView = findViewById(R.id.item_picture);
        changePictureButton = findViewById(R.id.change_picture_button);
        itemNameEditText = findViewById(R.id.editText_itemName);
        itemDescriptionEditText = findViewById(R.id.editText_itemDescription);
        itemCategorySpinner = findViewById(R.id.spinner_itemCategory);
        itemPriceEditText = findViewById(R.id.editText_itemPrice);
        itemTypeRadioGroup = findViewById(R.id.radioGroup_itemType);
        saveButton = findViewById(R.id.save_button);

        fetchCategoriesFromFirestore();

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    // Get the input values
                    String itemName = itemNameEditText.getText().toString();
                    String itemDescription = itemDescriptionEditText.getText().toString();
                    String itemCategory;
                    if (itemCategorySpinner.getSelectedItem() == null) {
                        itemCategory = "";
                    } else {
                        itemCategory = itemCategorySpinner.getSelectedItem().toString();
                    }
                    int itemPrice = Integer.parseInt(itemPriceEditText.getText().toString());
                    int selectedItemTypeId = itemTypeRadioGroup.getCheckedRadioButtonId();
                    Log.d("Selected item type id: ", String.valueOf(selectedItemTypeId));
                    boolean itemForTrade = selectedItemTypeId == R.id.radioButton_trade;
                    boolean itemForSale = selectedItemTypeId == R.id.radioButton_sale;
                    boolean itemForAuction = selectedItemTypeId == R.id.radioButton_auction;

                    // Get the current user's ID from FirebaseAuth
                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    if (currentUser == null) {
                        // Handle the case when the user is not logged in
                        return;
                    }
                    String userId = currentUser.getUid();

                    // Add the data to Firestore
                    FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
                    ItemModel item = new ItemModel(itemName, itemDescription, itemCategory, itemPrice, "", itemForTrade, itemForSale, itemForAuction, userId);
                    firestoreDB.collection("ITEMS").add(item)
                            .addOnSuccessListener(documentReference -> {
                                // Data added successfully, go back to the previous activity
                                Intent intent = new Intent(AddItemActivity.this, HomePageActivity.class);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors that occur during data upload
                                Log.e("AddItemActivity", "Error adding item: " + e.getMessage());
                            });
                }
            }
        });
    }

    private boolean validateInput() {
        // TODO: Implement input validation logic here
        return true;
    }

    private void fetchCategoriesFromFirestore() {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("CATEGORIES")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> categories = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String categoryName = documentSnapshot.getString("name");
                        categories.add(categoryName);
                    }
                    // Step 2: Populate the spinner with the fetched categories
                    populateCategoriesInSpinner(categories);
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Log.e("AddItemActivity", "Error fetching categories: " + e.getMessage());
                });
    }

    private void populateCategoriesInSpinner(ArrayList<String> categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemCategorySpinner.setAdapter(adapter);
    }


}
