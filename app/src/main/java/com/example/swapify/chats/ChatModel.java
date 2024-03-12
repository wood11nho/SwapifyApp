package com.example.swapify.chats;

import com.google.firebase.firestore.FirebaseFirestore;

public class ChatModel {
    private String user1;
    private String user2;
    private String user1Username;
    private String user2Username;

    public ChatModel() {
        // Empty constructor needed for Firestore serialization
    }

    public ChatModel(String user1, String user2) {
        this.user1 = user1;
        this.user2 = user2;
        this.user1Username = "";
        this.user2Username = "";
        setUser1UsernameFromFirestore();
        setUser2UsernameFromFirestore();
    }

    public String getUser1() {
        return user1;
    }

    public void setUser1(String user1) {
        this.user1 = user1;
    }

    public String getUser2() {
        return user2;
    }

    public void setUser2(String user2) {
        this.user2 = user2;
    }

    @Override
    public String toString() {
        return "ChatModel{" +
                "user1='" + user1 + '\'' +
                ", user2='" + user2 + '\'' +
                '}';
    }

    public void setUser1UsernameFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERS").document(user1).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user1Username = task.getResult().getString("username");
            }
        });
    }

    public void setUser2UsernameFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("USERS").document(user2).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                user2Username = task.getResult().getString("username");
            }
        });
    }

    public String getUser1Username() {
        return user1Username;
    }

    public String getUser2Username() {
        return user2Username;
    }
}
