package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

public class AddItemActivity extends AppCompatActivity {

    private ImageView profilePictureImageView;
    private Button changePictureButton;
    private EditText itemNameEditText;
    private EditText itemDescriptionEditText;
    private Spinner itemCategorySpinner;
    private EditText itemPriceEditText;
    private RadioGroup itemTypeRadioGroup;
    private Button saveButton;

    private DBObject db;

    private SharedPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // Initialize views
        profilePictureImageView = findViewById(R.id.profile_picture);
        changePictureButton = findViewById(R.id.change_picture_button);
        itemNameEditText = findViewById(R.id.editText_itemName);
        itemDescriptionEditText = findViewById(R.id.editText_itemDescription);
        itemCategorySpinner = findViewById(R.id.spinner_itemCategory);
        itemPriceEditText = findViewById(R.id.editText_itemPrice);
        itemTypeRadioGroup = findViewById(R.id.radioGroup_itemType);
        saveButton = findViewById(R.id.save_button);

        userPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);

        db = new DBObject(this);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInput()) {
                    // Start a new thread to add the data to the database
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // Get the input values
                            String itemName = itemNameEditText.getText().toString();
                            String itemDescription = itemDescriptionEditText.getText().toString();
                            String itemCategory;
                            if(itemCategorySpinner.getSelectedItem() == null) {
                                itemCategory = "";
                            } else {
                                itemCategory = itemCategorySpinner.getSelectedItem().toString();
                            }
                            int itemPrice = Integer.parseInt(itemPriceEditText.getText().toString());
                            int selectedItemTypeId = itemTypeRadioGroup.getCheckedRadioButtonId();
                            Log.d("Selected item type id: ", String.valueOf(selectedItemTypeId));
                            int itemForTrade = 0;
                            int itemForSale = 0;
                            int itemForAuction = 0;

                            // Determine the selected item type
                            if (selectedItemTypeId == R.id.radioButton_trade) {
                                itemForTrade = 1;
                            } else if (selectedItemTypeId == R.id.radioButton_sale) {
                                itemForSale = 1;
                            } else if (selectedItemTypeId == R.id.radioButton_auction) {
                                itemForAuction = 1;
                            }

                            // Add the data to the database

                            Log.d("AddItemActivity", "run: " + itemName + " " + itemDescription + " " + itemCategory + " " + itemPrice + " " + itemForTrade + " " + itemForSale + " " + itemForAuction + " " + userPreferences.getInt("id", 0));
                            ItemModel item = new ItemModel(itemName, itemDescription, itemCategory, itemPrice, itemForTrade, itemForSale, itemForAuction, userPreferences.getInt("id", 0));
                            db.addItem(item);

                            // Go back to the previous activity
                            Intent intent = new Intent(AddItemActivity.this, HomePageActivity.class);
                            startActivity(intent);

                            finish();
                        }
                    });
                    thread.start();
                }
            }
        });
    }

    private boolean validateInput() {
        // TODO: Implement input validation logic here
        return true;
    }
}
