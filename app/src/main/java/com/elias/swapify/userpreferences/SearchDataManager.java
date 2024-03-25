package com.elias.swapify.userpreferences;

import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;

public class SearchDataManager {
    private static SearchDataManager instance;

    private SearchDataManager() {}

    public static synchronized SearchDataManager getInstance() {
        if (instance == null) {
            instance = new SearchDataManager();
        }
        return instance;
    }

    public void saveSearch(String searchTerm){
        String userId = FirebaseUtil.getCurrentUserId();
        if (userId == null) return; // Early return if user ID is null

        // Assuming FirestoreUtil or another utility class handles user preferences updates
        FirestoreUtil.updateUserSearchHistory(userId, searchTerm, new FirestoreUtil.UpdateUserSearchHistoryCallback() {
            @Override
            public void onSuccess() {
                // Handle success
            }

            @Override
            public void onFailure(Exception e) {
                // Handle failure
            }
        });
    }
}
