package com.elias.swapify.items;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Spinner;

import com.elias.swapify.principalactivities.HomePageActivity;
import com.elias.swapify.R;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.elias.swapify.userpreferences.PostedItemsManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.util.ArrayList;

public class AddItemActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST_CODE = 101;
    private String itemPictureUrl;
    private ImageView itemPictureImageView;
    private Button changePictureButton;
    private ImageButton backButton;
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
        backButton = findViewById(R.id.btnBack_add_item);
        changePictureButton = findViewById(R.id.change_picture_button);
        itemNameEditText = findViewById(R.id.editText_itemName);
        itemDescriptionEditText = findViewById(R.id.editText_itemDescription);
        itemCategorySpinner = findViewById(R.id.spinner_itemCategory);
        itemPriceEditText = findViewById(R.id.editText_itemPrice);
        itemTypeRadioGroup = findViewById(R.id.radioGroup_itemType);
        saveButton = findViewById(R.id.save_button);

        fetchCategoriesFromFirestore();

        // Set up the back button
        backButton.setOnClickListener(v -> {
            // Go back to the previous activity
            Intent intent = new Intent(AddItemActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish();
        });

        saveButton.setOnClickListener(v -> {
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
                uploadItemToFirestore(itemName, itemDescription, itemCategory, itemPrice, itemPictureUrl, itemForTrade, itemForSale, itemForAuction, userId);
            }
        });

        changePictureButton.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            // Display the image in itemPictureImageView
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                itemPictureImageView.setImageBitmap(bitmap);

                // Call uploadImageToFirebaseStorage to upload the selected image
                uploadImageToFirebaseStorage(selectedImageUri);

                // Save the image URL in itemPictureUrl
                assert selectedImageUri != null;
                itemPictureUrl = selectedImageUri.toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateInput() {
        // Validate the item name
        String itemName = itemNameEditText.getText().toString();
        if (itemName.isEmpty()) {
            itemNameEditText.setError("Please enter an item name");
            return false;
        }

        // Validate the item description
        String itemDescription = itemDescriptionEditText.getText().toString();
        if (itemDescription.isEmpty()) {
            itemDescriptionEditText.setError("Please enter an item description");
            return false;
        }

        // Validate the item category
        String itemCategory = itemCategorySpinner.getSelectedItem().toString();
        if (itemCategory.isEmpty()) {
            itemCategorySpinner.requestFocus();
            return false;
        }

        // Validate the item price
        String itemPrice = itemPriceEditText.getText().toString();
        if (itemPrice.isEmpty()) {
            itemPriceEditText.setError("Please enter an item price");
            return false;
        }

        // Validate the item type
        if (itemTypeRadioGroup.getCheckedRadioButtonId() == -1) {
            itemTypeRadioGroup.requestFocus();
            return false;
        }

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

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        // Get a reference to the Firebase Storage location
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("item_images");

        // Create a unique filename for the image (e.g., using a timestamp)
        String filename = "item_image_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        // Upload the image
        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    // Image uploaded successfully, get the download URL
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        // Now, you have the imageUrl; you can do something with it or save it to Firestore
                        itemPictureUrl = imageUrl;
                    });
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the upload
                    Log.e("AddItemActivity", "Error uploading image: " + e.getMessage());
                });
    }

    private void uploadItemToFirestore(String itemName, String itemDescription, String itemCategory, int itemPrice, String itemPictureUrl, boolean itemForTrade, boolean itemForSale, boolean itemForAuction, String userId) {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("ITEMS")
                .add(new ItemModel(itemName, itemDescription, itemCategory, itemPrice, itemPictureUrl, itemForTrade, itemForSale, itemForAuction, userId))
                .addOnSuccessListener(documentReference -> {
                    // Item added successfully
                    SearchDataManager.getInstance().saveSearch(itemCategory);
                    PostedItemsManager.getInstance().savePostedItem(itemName);
                    PostedItemsManager.getInstance().savePostedItem(itemDescription);
                    Log.d("AddItemActivity", "Item added successfully");
                    Log.d("Item category", itemCategory);
                    // Get the document from CATEGORIES with the field 'name' equal to 'itemCategory' and update the field 'numberOfItems' with 1
                    firestoreDB.collection("CATEGORIES")
                            .whereEqualTo("name", itemCategory)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                if (queryDocumentSnapshots.size() == 1) {
                                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                                    String documentId = documentSnapshot.getId();
                                    Log.d("AddItemActivity", "Category ID: " + documentId);
                                    int numberOfItems = documentSnapshot.getLong("numberOfItems").intValue();
                                    Log.d("AddItemActivity", "Number of items: " + numberOfItems);
                                    firestoreDB.collection("CATEGORIES").document(documentId).update("numberOfItems", numberOfItems + 1);
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle any errors that occur during the Firestore query
                                Log.e("AddItemActivity", "Error updating category: " + e.getMessage());
                            });
                    // Go back to the previous activity
                    Intent intent = new Intent(AddItemActivity.this, HomePageActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Log.e("AddItemActivity", "Error adding item: " + e.getMessage());
                });
    }
}
