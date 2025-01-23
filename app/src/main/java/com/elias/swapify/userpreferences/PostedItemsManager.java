package com.elias.swapify.userpreferences;

import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;

public class PostedItemsManager {
    private static PostedItemsManager instance;

    public PostedItemsManager(){
    }

    public static synchronized PostedItemsManager getInstance() {
        if (instance == null) {
            instance = new PostedItemsManager();
        }
        return instance;
    }

    public void savePostedItem(String detail) {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) return; // Early return if user ID is null

        // Use FirestoreUtil for database operations
        FirestoreUtil.saveUserPostedItem(userId, detail, new FirestoreUtil.SaveUserPostedItemCallback() {
            @Override
            public void onSuccess() {
                // Handle successful save
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
            }
        });
    }
}
