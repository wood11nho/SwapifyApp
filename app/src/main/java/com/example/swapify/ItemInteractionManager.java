package com.example.swapify;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ItemInteractionManager {
    private static ItemInteractionManager instance;
    private final FirebaseFirestore firestoreDB;
    private final String userId;

    public ItemInteractionManager(FirebaseFirestore firestoreDB, String userId) {
        this.firestoreDB = firestoreDB;
        this.userId = userId;
    }

    public static synchronized ItemInteractionManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException(ItemInteractionManager.class.getSimpleName() +
                    " is not initialized, call initializeInstance(..) method first.");
        }
        return instance;
    }

    public static synchronized void initializeInstance(FirebaseFirestore firestoreDB, String userId) {
        if (instance == null) {
            instance = new ItemInteractionManager(firestoreDB, userId);
        }
    }

    public void saveItemInteraction(String detail){
        DocumentReference documentReference = firestoreDB.collection("USER_PREFERENCES").document(userId);

        documentReference.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()){
                    List<String> itemInteractions = Objects.requireNonNull(document.toObject(CustomerPreferencesModel.class)).getItemInteractions();

                    if (itemInteractions == null){
                        itemInteractions = new ArrayList<>();
                    }

                    itemInteractions.add(detail);

                    documentReference.update("itemInteractions", itemInteractions);
                }
            }
        });
    }
}
