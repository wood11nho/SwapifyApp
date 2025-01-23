package com.elias.swapify.userpreferences;

import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.elias.swapify.items.ItemModel;

public class ItemInteractionManager {
    private static ItemInteractionManager instance;

    public ItemInteractionManager() {
    }

    public static synchronized ItemInteractionManager getInstance() {
        if (instance == null) {
            instance = new ItemInteractionManager();
        }
        return instance;
    }

    public void saveItemInteraction(ItemModel item) {
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) return; // Early return if user ID is null

        // Use FirestoreUtil for database operations
        FirestoreUtil.saveUserItemInteraction(userId, item, new FirestoreUtil.SaveUserItemInteractionCallback() {
            @Override
            public void onSuccess() {
                // Handle successful interaction save
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
            }
        });
    }
}
