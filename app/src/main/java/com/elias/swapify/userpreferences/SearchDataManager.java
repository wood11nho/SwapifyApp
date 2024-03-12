package com.elias.swapify.userpreferences;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SearchDataManager {
    private static SearchDataManager instance;
    private final FirebaseFirestore firestoreDB;
    private final String userId;

    public SearchDataManager(FirebaseFirestore firestoreDB, String userId) {
        this.firestoreDB = firestoreDB;
        this.userId = userId;
    }

    public static synchronized SearchDataManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(SearchDataManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public static synchronized void initializeInstance(FirebaseFirestore firestoreDB, String userId) {
        if (instance == null) {
            instance = new SearchDataManager(firestoreDB, userId);
        }
    }

    public void saveSearch(String searchTerm){
        DocumentReference documentReference = firestoreDB.collection("USER_PREFERENCES").document(userId);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    List<String> searchHistory = Objects.requireNonNull(document.toObject(CustomerPreferencesModel.class)).getSearchHistory();

                    if (searchHistory == null){
                        searchHistory = new ArrayList<>();
                    }

                    searchHistory.add(searchTerm);

                    documentReference.update("searchHistory", searchHistory);
                }
            }
        });
    }
}
