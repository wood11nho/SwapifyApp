package com.elias.swapify.chats;

import static com.elias.swapify.firebase.FirebaseUtil.getCurrentUserId;

import android.content.Intent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.elias.swapify.principalactivities.HomePageActivity;
import com.elias.swapify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AllChatsActivity extends AppCompatActivity implements SearchFragment.OnSearchInputListener {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firestoreDB;
    private FirebaseUser currentUser;
    private CollectionReference chatMessagesCollection;
    private ListView listViewChatMessages;
    private ImageButton backButton;
    private List<ChatModel> chats;
    private ChatAdapter chatAdapter;
    private ImageButton searchButton;
    @Override
    public void onSearchInput(String searchText) {
        filterChats(searchText);
    }

    private void filterChats(String searchText) {
        String currentUserId = getCurrentUserId(); // You need to implement this method
        List<ChatModel> filteredChats = new ArrayList<>();
        for (ChatModel chat : chats) {
            // Check if current user is user1 or user2 and get the other user's username
            String otherUsername = chat.getUser1().equals(currentUserId) ? chat.getUser2Username() : chat.getUser1Username();

            // Now filter using otherUsername
            if (otherUsername.toLowerCase().contains(searchText.toLowerCase())) {
                filteredChats.add(chat);
            }
        }
        chatAdapter = new ChatAdapter(this, R.layout.chat_layout, filteredChats);
        listViewChatMessages.setAdapter(chatAdapter);
    }

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
        searchButton = findViewById(R.id.btnSearchAllChats);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AllChatsActivity.this, HomePageActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FrameLayout searchFrameLayout = findViewById(R.id.searchFrameLayout);
                SearchFragment searchFragment = (SearchFragment) getSupportFragmentManager().findFragmentById(R.id.searchFrameLayout);

                if (searchFragment != null && searchFragment.isVisible()) {
                    // Hide the search fragment with animation
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.fade_in, R.anim.slide_out)
                            .remove(searchFragment)
                            .commit();
                    searchFrameLayout.setVisibility(View.GONE);
                } else {
                    // Show the search fragment with animation
                    searchFragment = new SearchFragment();
                    getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(R.anim.slide_in, R.anim.fade_out)
                            .replace(R.id.searchFrameLayout, searchFragment)
                            .commit();
                    ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) listViewChatMessages.getLayoutParams();
                    params.topToBottom = R.id.searchFrameLayout;
                    listViewChatMessages.setLayoutParams(params);
                    searchFrameLayout.setVisibility(View.VISIBLE);
                }
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
                            assert chat != null;
                            chat.setUser1UsernameFromFirestore();
                            chat.setUser2UsernameFromFirestore();
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
