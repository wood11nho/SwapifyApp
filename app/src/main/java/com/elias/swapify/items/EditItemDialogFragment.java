package com.elias.swapify.items;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.elias.swapify.charity.CharityModel;
import com.elias.swapify.charity.CharityPagerAdapter;
import com.elias.swapify.firebase.FirebaseMLModel;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;

public class EditItemDialogFragment extends DialogFragment {
    private static final int PICK_FROM_GALLERY = 1;

    private ItemModel item;
    private String itemPictureUrl;
    private EditText editItemName;
    private EditText editItemPrice;
    private EditText editItemDescription;
    private Spinner locationSpinner;
    private Spinner categorySpinner;
    private RadioGroup radioGroupItemType;
    private RadioButton radioButtonTrade;
    private RadioButton radioButtonSale;
    private RadioButton radioButtonCharity;
    private ImageView itemPicture;
    private Button btnSave;
    private ImageButton btnBack;
    private Button changePictureButton;
    private TextView textViewPriceExplanationAndItemType;
    private ViewPager charitiesViewPager;
    private CharityPagerAdapter charityPagerAdapter;
    private ArrayList<CharityModel> charities;
    private FirebaseMLModel firebaseMLModel;

    private CountDownLatch dataLoadLatch = new CountDownLatch(3); // For categories, locations, and charities
    private OnItemUpdatedListener onItemUpdatedListener;

    private final ActivityResultLauncher<String[]> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                Boolean readImagesGranted = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    readImagesGranted = result.getOrDefault(Manifest.permission.READ_MEDIA_IMAGES, false);
                }
                if (readImagesGranted) {
                    openGallery();
                } else {
                    Toast.makeText(getActivity(), "Permission denied to read your media", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<Intent> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    handleImageSelection(selectedImageUri);
                }
            });

    public interface OnItemUpdatedListener {
        void onItemUpdated();
    }

    public static EditItemDialogFragment newInstance(ItemModel item) {
        EditItemDialogFragment fragment = new EditItemDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable("item", item);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnItemUpdatedListener) {
            onItemUpdatedListener = (OnItemUpdatedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnItemUpdatedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onItemUpdatedListener = null;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_item, container, false);

        Toolbar toolbar = view.findViewById(R.id.back_toolbar_edit_item);
        btnBack = toolbar.findViewById(R.id.btnBack_edit_item);
        btnBack.setOnClickListener(v -> dismiss());

        editItemName = view.findViewById(R.id.editText_itemName);
        editItemPrice = view.findViewById(R.id.editText_itemPrice);
        editItemDescription = view.findViewById(R.id.editText_itemDescription);
        locationSpinner = view.findViewById(R.id.location_spinner);
        categorySpinner = view.findViewById(R.id.spinner_itemCategory);
        radioGroupItemType = view.findViewById(R.id.radioGroup_itemType);
        radioButtonTrade = view.findViewById(R.id.radioButton_trade);
        radioButtonSale = view.findViewById(R.id.radioButton_sale);
        radioButtonCharity = view.findViewById(R.id.radioButton_charity);
        itemPicture = view.findViewById(R.id.item_picture);
        btnSave = view.findViewById(R.id.save_button);
        changePictureButton = view.findViewById(R.id.edit_picture_button);
        textViewPriceExplanationAndItemType = view.findViewById(R.id.textView_priceExplanationAndItemType);
        charitiesViewPager = view.findViewById(R.id.charities_view_pager_slider);

        charitiesViewPager.setVisibility(View.GONE);

        // Initialize the FirebaseMLModel object
        firebaseMLModel = new FirebaseMLModel();
        firebaseMLModel.downloadAndInitializeModel(getContext(), "Detect-Category-Model");

        if (getArguments() != null) {
            item = (ItemModel) getArguments().getSerializable("item");
        }

        changePictureButton.setOnClickListener(v -> openGalleryWithPermissionCheck());

        btnSave.setOnClickListener(v -> saveItemChanges());

        radioGroupItemType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton_trade) {
                editItemPrice.setEnabled(false);
                editItemPrice.setText("0");
                textViewPriceExplanationAndItemType.setText("Item selected for trade, therefore the price is not mandatory and is set to 0.");
                charitiesViewPager.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioButton_sale) {
                editItemPrice.setEnabled(true);
//                editItemPrice.setText("");
                textViewPriceExplanationAndItemType.setText("Item selected for sale, therefore the price is mandatory.");
                charitiesViewPager.setVisibility(View.GONE);
            } else if (checkedId == R.id.radioButton_charity) {
                editItemPrice.setEnabled(true);
//                editItemPrice.setText("");
                textViewPriceExplanationAndItemType.setText("Item selected for charity, therefore the price is mandatory and you must select a charity.");
                charitiesViewPager.setVisibility(View.VISIBLE);
            }
        });

        fetchLocationsFromJSON();
        fetchCategoriesFromFirestore();
        fetchCharitiesFromFirestore();

        // Wait for all data to be fetched before populating the fields
        new Thread(() -> {
            try {
                dataLoadLatch.await();
                getActivity().runOnUiThread(this::populateFields);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();

        return view;
    }

    private void populateFields() {
        if (item != null) {
            editItemName.setText(item.getItemName());
            editItemPrice.setText(String.valueOf(item.getItemPrice()));
            editItemDescription.setText(item.getItemDescription());

            // Set the image
            if (!TextUtils.isEmpty(item.getItemImage())) {
                Glide.with(this)
                        .load(item.getItemImage())
                        .fitCenter()
                        .centerCrop()
                        .placeholder(R.drawable.ic_question_mark)
                        .error(R.drawable.ic_question_mark)
                        .into(itemPicture);
            }

            // Set the location and category spinners
            if (locationSpinner.getAdapter() != null) {
                locationSpinner.setSelection(((ArrayAdapter<String>) locationSpinner.getAdapter()).getPosition(item.getItemLocation()));
            }
            if (categorySpinner.getAdapter() != null) {
                categorySpinner.setSelection(((ArrayAdapter<String>) categorySpinner.getAdapter()).getPosition(item.getItemCategory()));
            }

            // Set the item type radio buttons
            if (item.getItemIsForTrade()) {
                radioButtonTrade.setChecked(true);
            } else if (item.getItemIsForSale()) {
                radioButtonSale.setChecked(true);
            } else if (item.getItemIsForCharity()) {
                radioButtonCharity.setChecked(true);
                charitiesViewPager.setVisibility(View.VISIBLE);
                for (int i = 0; i < charities.size(); i++) {
                    if (charities.get(i).getCharityId().equals(item.getItemCharityId())) {
                        charitiesViewPager.setCurrentItem(i);
                        break;
                    }
                }
            }
        }
    }

    private void saveItemChanges() {
        String name = editItemName.getText().toString();
        String price = editItemPrice.getText().toString();
        String description = editItemDescription.getText().toString();
        String location = locationSpinner.getSelectedItem().toString();
        String category = categorySpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(price) || TextUtils.isEmpty(description) || TextUtils.isEmpty(location) || TextUtils.isEmpty(category)) {
            Toast.makeText(getActivity(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        item.setItemName(name);
        item.setItemPrice(Integer.parseInt(price));
        item.setItemDescription(description);
        item.setItemLocation(location);
        item.setItemCategory(category);

        int checkedRadioButtonId = radioGroupItemType.getCheckedRadioButtonId();
        if (checkedRadioButtonId == R.id.radioButton_trade) {
            item.setItemIsForTrade(true);
            item.setItemIsForSale(false);
            item.setItemIsForCharity(false);
        } else if (checkedRadioButtonId == R.id.radioButton_sale) {
            item.setItemIsForTrade(false);
            item.setItemIsForSale(true);
            item.setItemIsForCharity(false);
        } else if (checkedRadioButtonId == R.id.radioButton_charity) {
            item.setItemIsForTrade(false);
            item.setItemIsForSale(false);
            item.setItemIsForCharity(true);
            item.setItemCharityId(charities.get(charitiesViewPager.getCurrentItem()).getCharityId());
        }

        FirebaseFirestore.getInstance().collection("ITEMS").document(item.getItemId())
                .set(item)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Item updated successfully", Toast.LENGTH_SHORT).show();
                    dismiss();
                    // Make sure to reload the adapter from the parent fragment or activity
                    if (onItemUpdatedListener != null) {
                        onItemUpdatedListener.onItemUpdated();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getActivity(), "Error updating item", Toast.LENGTH_SHORT).show());
    }

    private void openGalleryWithPermissionCheck() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14 or higher
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO,
                        Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
                });
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_MEDIA_IMAGES,
                        Manifest.permission.READ_MEDIA_VIDEO
                });
            }
        } else {
            // Android 12L or lower
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                openGallery();
            } else {
                requestPermissionLauncher.launch(new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE
                });
            }
        }
    }

    private void showPermissionRationale() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Permission Needed")
                .setMessage("This app needs to access your external storage to select a picture.")
                .setPositiveButton("OK", (dialog, which) -> requestPermissionLauncher.launch(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}))
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        pickImageLauncher.launch(intent);
    }

    private void handleImageSelection(Uri selectedImageUri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
            // Resize the bitmap before setting it to ImageView
            Bitmap resizedBitmap = getResizedBitmap(bitmap, 1200); // Resize to a max width of 1200px while maintaining aspect ratio
            itemPicture.setImageBitmap(resizedBitmap);

            // Classify the resized image using the Firebase ML model
            classifyImage(resizedBitmap);

            // Upload the resized image to Firebase Storage
            uploadImageToFirebaseStorage(selectedImageUri);

            // Save the image URL in itemPictureUrl
            assert selectedImageUri != null;
            itemPictureUrl = selectedImageUri.toString();
        } catch (IOException e) {
            Toast.makeText(getActivity(), "Error loading image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (OutOfMemoryError e) {
            Toast.makeText(getActivity(), "Image is too large and cannot be loaded.", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap getResizedBitmap(Bitmap image, int maxWidth) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxWidth;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxWidth;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }

    private void classifyImage(Bitmap image) {
        // Ensure the model is ready for prediction
        if (firebaseMLModel.getInterpreter() != null) {
            String predictedCategory = firebaseMLModel.predictImageCategory(image, getContext());
            Log.d("EditItemDialogFragment", "Predicted Category: " + predictedCategory);
            // Create a modern and attractive screen notification or popup which tells you the predicted category
            showSnackbar(predictedCategory);
        } else {
            Log.d("EditItemDialogFragment", "Interpreter is not initialized");
        }
    }

    private void showSnackbar(String predictedCategory) {
        // Find the root view of the fragment to anchor the Snackbar
        View rootView = getView().findViewById(R.id.fragment_edit_item_root); // Ensure you have a root view with this id in your fragment layout

        // Create the Snackbar
        Snackbar snackbar = Snackbar.make(rootView, "It looks like you want to add a " + predictedCategory + " item. Is that correct?", Snackbar.LENGTH_LONG);

        // Add "YES" action to the Snackbar
        snackbar.setAction("YES", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Find the spinner item that matches the predicted category and set the spinner's selection
                for (int i = 0; i < categorySpinner.getAdapter().getCount(); i++) {
                    if (categorySpinner.getAdapter().getItem(i).toString().equalsIgnoreCase(predictedCategory)) {
                        categorySpinner.setSelection(i);
                        break; // Exit the loop once the matching category is found and set
                    }
                }
                snackbar.dismiss(); // Dismiss the Snackbar once the action is performed
            }
        }).setActionTextColor(getResources().getColor(android.R.color.holo_blue_light)); // Set the action text color

        // Optionally, change the background or text color of the Snackbar
        snackbar.setBackgroundTint(getResources().getColor(R.color.white)); // Example background color
        snackbar.setTextColor(getResources().getColor(R.color.black)); // Example text color

        // Show the Snackbar
        snackbar.show();
    }

    private void fetchCharitiesFromFirestore() {
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        firestoreDB.collection("CHARITIES")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    charities = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String charityId = documentSnapshot.getId();
                        String charityName = documentSnapshot.getString("charityName");
                        String charityDescription = documentSnapshot.getString("charityDescription");
                        String charityImage = documentSnapshot.getString("charityImage");
                        charities.add(new CharityModel(charityId, charityName, charityDescription, charityImage));
                    }
                    charityPagerAdapter = new CharityPagerAdapter(getContext(), charities);
                    charitiesViewPager.setAdapter(charityPagerAdapter);
                    charitiesViewPager.addOnPageChangeListener(viewListener);

                    dataLoadLatch.countDown(); // Count down after fetching charities
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Log.e("EditItemDialogFragment", "Error fetching charities: " + e.getMessage());
                    dataLoadLatch.countDown(); // Count down even if there's an error
                });
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageSelected(int position) {
            // Ensure views are not null before setting text
            if (getView() != null) {
                TextView charityName = getView().findViewById(R.id.charityName);
                TextView charityDescription = getView().findViewById(R.id.charityDescription);
                if (charityName != null && charityDescription != null) {
                    charityName.setText(charities.get(position).getCharityName());
                    charityDescription.setText(charities.get(position).getCharityDescription());
                    charityPagerAdapter.setSelectedPosition(position);
                    Log.d("EditItemDialogFragment", "Selected charity: " + charities.get(position).getCharityName());
                }
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

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
                    // Populate the spinner with the fetched categories
                    populateCategoriesInSpinner(categories);

                    dataLoadLatch.countDown(); // Count down after fetching categories
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Log.e("EditItemDialogFragment", "Error fetching categories: " + e.getMessage());
                    dataLoadLatch.countDown(); // Count down even if there's an error
                });
    }

    private void populateCategoriesInSpinner(ArrayList<String> categories) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, categories);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);
    }

    private void fetchLocationsFromJSON() {
        ArrayList<String> countyNames = new ArrayList<>();
        try {
            String json = loadJSONFromAsset();
            JSONArray jsonArray = new JSONArray(json);

            for (int i = 0; i < jsonArray.length(); i++) {
                countyNames.add(jsonArray.getJSONObject(i).getString("nume")); // "nume" is the key for county names in your JSON
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        populateSpinner(countyNames);

        dataLoadLatch.countDown(); // Count down after fetching locations
    }

    private String loadJSONFromAsset() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("counties_and_cities/counties.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void populateSpinner(ArrayList<String> countyNames) {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, countyNames);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        locationSpinner.setAdapter(adapter);
    }

    private void uploadImageToFirebaseStorage(Uri imageUri) {
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("item_images");

        String filename = "item_image_" + System.currentTimeMillis() + ".jpg";
        StorageReference imageRef = storageRef.child(filename);

        imageRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageUrl = uri.toString();
                        itemPictureUrl = imageUrl;
                        item.setItemImage(imageUrl);
                        Toast.makeText(getActivity(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e("EditItemDialogFragment", "Error uploading image: " + e.getMessage());
                    Toast.makeText(getActivity(), "Error uploading image", Toast.LENGTH_SHORT).show();
                });
    }
}
