package com.elias.swapify.firebase;

import android.util.Log;

import com.elias.swapify.categories.CategoryModel;
import com.elias.swapify.charity.CharityModel;
import com.elias.swapify.items.ItemModel;
import com.elias.swapify.wishlists.WishlistItem;
import com.elias.swapify.wishlists.WishlistModel;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreUtil {
    private static final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public static void fetchCharityData(String charityId, OnCharityDataFetchedListener listener) {
        firestore.collection("CHARITIES").document(charityId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        CharityModel charity = documentSnapshot.toObject(CharityModel.class);
                        if (charity != null){
                            listener.onCharityDataFetched(charity);
                        }
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.toString()));
    }

    public static void fetchUserData(String userId, OnUserDataFetchedListener listener) {
        firestore.collection("USERS").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");
                        listener.onUserDataFetched(username);
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.toString()));
    }

    public static void fetchUsersName(String userId, OnUserDataFetchedListener listener) {
        firestore.collection("USERS").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("name");
                        listener.onUserDataFetched(name);
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.toString()));
    }


    public static void fetchItemsFromFirestore(OnItemsFetchedListener listener) {
        firestore.collection("ITEMS").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<ItemModel> items = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        item.setItemId(documentSnapshot.getId());
                        if (item != null && !item.getItemUserId().equals(FirebaseUtil.getCurrentUserId())) {
                            items.add(item);
                        }
                    }
                    listener.onItemsFetched(items);
                })
                .addOnFailureListener(e -> listener.onError(e.toString()));
    }

    public static void fetchCategoriesFromFirestore(OnCategoriesFetchedListener listener) {
        firestore.collection("CATEGORIES")
                .orderBy("numberOfItems", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> categories = new ArrayList<>();
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        CategoryModel category = documentSnapshot.toObject(CategoryModel.class);
                        if (category != null) {
                            categories.add(category.getName());
                        }
                    }
                    listener.onCategoriesFetched(categories);
                })
                .addOnFailureListener(e -> listener.onError(e.toString()));
    }

    public static void updateUserSearchHistory(String userId, String searchTerm, UpdateUserSearchHistoryCallback callback) {
        DocumentReference documentReference = firestore.collection("USER_PREFERENCES").document(userId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    List<String> searchHistory = (List<String>) document.get("searchHistory");
                    if (searchHistory == null) searchHistory = new ArrayList<>();
                    searchHistory.add(searchTerm);
                    documentReference.update("searchHistory", searchHistory)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                } else {
                    // Handle document does not exist
                    Map<String, Object> newSearchHistory = new HashMap<>();
                    List<String> initialSearchHistory = new ArrayList<>();
                    initialSearchHistory.add(searchTerm);
                    newSearchHistory.put("searchHistory", initialSearchHistory);
                    documentReference.set(newSearchHistory)
                            .addOnSuccessListener(aVoid -> callback.onSuccess())
                            .addOnFailureListener(callback::onFailure);
                }
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    public static void saveUserPostedItem(String userId, String detail, SaveUserPostedItemCallback callback) {
        DocumentReference documentReference = firestore.collection("USER_PREFERENCES").document(userId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                List<String> postedItems;
                if (document.exists()) {
                    postedItems = (List<String>) document.get("postedItems");
                    if (postedItems == null) postedItems = new ArrayList<>();
                } else {
                    postedItems = new ArrayList<>();
                }
                postedItems.add(detail);
                documentReference.set(Collections.singletonMap("postedItems", postedItems))
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onFailure);
            } else {
                // Task was not successful or result was null
                callback.onFailure(task.getException());
            }
        });
    }

    public static void saveUserItemInteraction(String userId, ItemModel item, SaveUserItemInteractionCallback callback) {
        DocumentReference documentReference = firestore.collection("USER_PREFERENCES").document(userId);
        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                DocumentSnapshot document = task.getResult();
                List<String> itemInteractions;
                if (document.exists()) {
                    itemInteractions = (List<String>) document.get("itemInteractions");
                    if (itemInteractions == null) itemInteractions = new ArrayList<>();
                } else {
                    itemInteractions = new ArrayList<>();
                }

                // Assuming you want to save more detailed information about the interaction, adjust as needed
                itemInteractions.add(item.getItemName()); // Example of saving item name
                // Potentially save more details about the item here

                documentReference.update("itemInteractions", itemInteractions)
                        .addOnSuccessListener(aVoid -> callback.onSuccess())
                        .addOnFailureListener(callback::onFailure);
            } else {
                // Task was not successful or result was null
                callback.onFailure(task.getException());
            }
        });
    }

    public static void addItemToWishlist(String userId, String itemId, Date addedOn, WishlistUpdateCallback callback) {
        // Query the WISHLISTS collection to find the user's wishlist document
        firestore.collection("WISHLISTS").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // Assuming the user has only one wishlist document, get the first (and should be only) document
                        DocumentReference wishlistRef = queryDocumentSnapshots.getDocuments().get(0).getReference();

                        WishlistItem newItem = new WishlistItem(addedOn, itemId);

                        // Atomically add the new item to the "items" array field in the wishlist document
                        wishlistRef.update("items", FieldValue.arrayUnion(newItem))
                                .addOnSuccessListener(aVoid -> callback.onWishlistUpdated())
                                .addOnFailureListener(e -> callback.onWishlistUpdateFailed(e));
                    } else {
                        // Handle the case where the user doesn't have a wishlist document
                        // You may want to create a new document here
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any failures here
                    callback.onWishlistUpdateFailed(e);
                });
    }

    public static void removeItemFromWishlist(String userId, String itemId, WishlistUpdateCallback callback) {
        // Query the WISHLISTS collection to find the user's wishlist document
        firestore.collection("WISHLISTS").whereEqualTo("userId", userId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        List<Map<String, Object>> items = (List<Map<String, Object>>) documentSnapshot.get("items");

                        if (items != null) {
                            // Create a new list for the updated items
                            List<Map<String, Object>> updatedItems = new ArrayList<>();

                            // Add all items except the one to remove to the updated list
                            for (Map<String, Object> itemMap : items) {
                                if (!itemId.equals(itemMap.get("itemId"))) {
                                    updatedItems.add(itemMap);
                                }
                            }

                            // Now update the document with the new list of items
                            documentSnapshot.getReference().update("items", updatedItems)
                                    .addOnSuccessListener(aVoid -> callback.onWishlistUpdated())
                                    .addOnFailureListener(e -> callback.onWishlistUpdateFailed(e));
                        } else {
                            // No items in wishlist, nothing to remove
                            callback.onWishlistUpdateFailed(new Exception("No items in wishlist."));
                        }
                    } else {
                        // No document found for the user's wishlist
                        callback.onWishlistUpdateFailed(new Exception("Wishlist document not found."));
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any failures here
                    callback.onWishlistUpdateFailed(e);
                });
    }


    public static void isItemInWishlist(String currentUserId, String itemId, FirestoreUtil.WishlistCheckCallback callback) {
        firestore.collection("WISHLISTS").whereEqualTo("userId", currentUserId).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                        List<HashMap<String, Object>> items = (List<HashMap<String, Object>>) documentSnapshot.get("items");
                        if (items != null) {
                            for (HashMap<String, Object> itemMap : items) {
                                String wishlistItemId = (String) itemMap.get("itemId");
                                if (itemId.equals(wishlistItemId)) {
                                    callback.onItemInWishlist(true);
                                    return;
                                }
                            }
                        }
                    }
                    callback.onItemInWishlist(false);
                })
                .addOnFailureListener(e -> callback.onItemInWishlist(false));
    }

    public static void fetchUserWishlistItems(String userId, final OnWishlistItemsFetchedListener listener) {
        firestore.collection("WISHLISTS")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        // For simplicity, assume each user has only one wishlist document
                        DocumentSnapshot wishlistDocument = queryDocumentSnapshots.getDocuments().get(0);
                        WishlistModel wishlist = wishlistDocument.toObject(WishlistModel.class);

                        if (wishlist != null){
                            List<String> itemIds = new ArrayList<>();
                            for (WishlistItem item : wishlist.getItems()) {
                                itemIds.add(item.getItemId());
                                Log.d("WishlistActivity", "Item ID: " + item.getItemId());
                            }
                            fetchItemsByIds(itemIds, listener);
                        } else{
                            listener.onWishlistItemsFetched(new ArrayList<>());
                        }
                    } else {
                        listener.onWishlistItemsFetched(new ArrayList<>()); // Empty list if no wishlist
                    }
                })
                .addOnFailureListener(e -> listener.onError(e.getMessage()));
    }

    private static void fetchItemsByIds(List<String> itemIds, final OnWishlistItemsFetchedListener listener) {
        final ArrayList<ItemModel> wishlistItems = new ArrayList<>();

        final int[] counter = {itemIds.size()};

        for (String itemId : itemIds) {
            firestore.collection("ITEMS").document(itemId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        ItemModel item = documentSnapshot.toObject(ItemModel.class);
                        if (item != null) {
                            item.setItemId(documentSnapshot.getId());
                            wishlistItems.add(item);
                        }
                        counter[0]--;
                        if (counter[0] == 0) {
                            listener.onWishlistItemsFetched(wishlistItems);
                        }
                    })
                    .addOnFailureListener(e -> {
                        counter[0]--;
                        if (counter[0] == 0) {
                            listener.onWishlistItemsFetched(wishlistItems);
                        }
                    });
        }

        if (itemIds.isEmpty()) {
            listener.onWishlistItemsFetched(wishlistItems);
        }
    }

    // Define interfaces for callbacks

    public interface OnCharityDataFetchedListener {
        void onCharityDataFetched(CharityModel charity);
        void onError(String error);
    }

    public interface OnUserDataFetchedListener {
        void onUserDataFetched(String field);
        void onError(String error);
    }

    public interface OnItemsFetchedListener {
        void onItemsFetched(ArrayList<ItemModel> items);
        void onError(String error);
    }

    public interface OnCategoriesFetchedListener {
        void onCategoriesFetched(ArrayList<String> categories);
        void onError(String error);
    }

    public interface UpdateUserSearchHistoryCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface SaveUserPostedItemCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface SaveUserItemInteractionCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface WishlistUpdateCallback {
        void onWishlistUpdated();
        void onWishlistUpdateFailed(Exception e);
    }

    public interface WishlistCheckCallback {
        void onItemInWishlist(boolean isInWishlist);
    }

    public interface OnWishlistItemsFetchedListener {
        void onWishlistItemsFetched(List<ItemModel> wishlistItems);
        void onError(String error);
    }
}
