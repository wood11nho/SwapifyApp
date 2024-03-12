package com.elias.swapify.userpreferences;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostedItemsManager {
    private static PostedItemsManager instance;
    private final FirebaseFirestore firestoreDB;
    private final String userId;

    public PostedItemsManager(FirebaseFirestore firestoreDB, String userId) {
        this.firestoreDB = firestoreDB;
        this.userId = userId;
    }

    public static synchronized PostedItemsManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(PostedItemsManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public static synchronized void initializeInstance(FirebaseFirestore firestoreDB, String userId) {
        if (instance == null) {
            instance = new PostedItemsManager(firestoreDB, userId);
        }
    }

    public void savePostedItem(String detail){
        DocumentReference documentReference = firestoreDB.collection("USER_PREFERENCES").document(userId);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    List<String> postedItems = Objects.requireNonNull(document.toObject(CustomerPreferencesModel.class)).getPostedItems();

                    if (postedItems == null){
                        postedItems = new ArrayList<>();
                    }

                    postedItems.add(detail);

                    documentReference.update("postedItems", postedItems);
                }
            }
        });
    }
}
