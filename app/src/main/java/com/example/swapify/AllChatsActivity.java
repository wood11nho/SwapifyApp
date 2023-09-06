package com.example.swapify;

import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AllChatsActivity extends AppCompatActivity{
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private FirebaseUser currentUser;
    private CollectionReference chatMessagesCollection;
    private ListView listViewChatMessages;
    private ImageButton backButton;
    private List<ChatModel> chats;
    private ChatAdapter chatAdapter;

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_chats);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        chatMessagesCollection = firestoreDB.collection("CHATS");

        listViewChatMessages = findViewById(R.id.listViewAllChats);
        backButton = findViewById(R.id.btnBack_all_chats);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllChatsActivity.this, HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        chats = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, R.layout.chat_layout, chats);
        listViewChatMessages.setAdapter(chatAdapter);

        loadChats();
    }

    private void loadChats() {
        chatMessagesCollection
                .whereEqualTo("user1", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documents) {
                            ChatModel chat = document.toObject(ChatModel.class);
                            chats.add(chat);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }
                });

        chatMessagesCollection
                .whereEqualTo("user2", currentUser.getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<DocumentSnapshot> documents = task.getResult().getDocuments();
                        for (DocumentSnapshot document : documents) {
                            ChatModel chat = document.toObject(ChatModel.class);
                            chats.add(chat);
                        }
                        chatAdapter.notifyDataSetChanged();
                    }
                });
    }
}
