package com.elias.swapify.chats;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.elias.swapify.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDB;
    private CollectionReference chatMessagesCollection;
    private ListView listViewChatMessages;
    private EditText editTextMessageInput;
    private ImageButton buttonSendMessage;
    private ImageButton backButton;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messages;
    private TextView otherPersonNameTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        chatMessagesCollection = firestoreDB.collection("MESSAGES");

        listViewChatMessages = findViewById(R.id.listViewChatMessages);
        editTextMessageInput = findViewById(R.id.editTextMessageInput);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        backButton = findViewById(R.id.btnBack_chat);
        otherPersonNameTextView = findViewById(R.id.otherPersonNameTextView);

        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, R.layout.message_layout_left, messages);
        listViewChatMessages.setAdapter(messageAdapter);
        // Move the list view to the bottom
        listViewChatMessages.setSelection(messageAdapter.getCount() - 1);

        buttonSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Just finish this activity so it goes back to the previous one
                finish();
            }
        });

        otherPersonNameTextView.setText(getIntent().getStringExtra("otherPersonName"));

        loadMessages();
    }

    private void sendMessage() {
        String messageContent = editTextMessageInput.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            // Create a new message
            MessageModel message = new MessageModel(currentUser.getUid(), getIntent().getStringExtra("receiverId"), messageContent, new Timestamp(new Date()).toDate());

            // Add the message to Firestore
            chatMessagesCollection.add(message).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if (task.isSuccessful()) {
                        // Clear the message input
                        editTextMessageInput.setText("");
                        Toast.makeText(ChatActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                        // Reload the messages
                        loadMessages();
                        // Try to add the chat between the two users to the 'CHATS' collection
                        // This will fail if the chat already exists
                        addChatToChatsCollection();
                        // Move the list view to the bottom
                        listViewChatMessages.setSelection(messageAdapter.getCount() - 1);
                    } else {
                        Toast.makeText(ChatActivity.this, "Error sending message!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void addChatToChatsCollection() {
        String user1 = currentUser.getUid();
        String user2 = getIntent().getStringExtra("receiverId");

        // Create a new chat
        ChatModel chat = new ChatModel(user1, user2);

        // Add the chat to Firestore but verify if a chat between user1 and user2 exists, or maybe user2 and user1
        firestoreDB.collection("CHATS")
                .whereEqualTo("user1", user1)
                .whereEqualTo("user2", user2)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().isEmpty()) {
                        firestoreDB.collection("CHATS")
                                .whereEqualTo("user1", user2)
                                .whereEqualTo("user2", user1)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful() && task2.getResult() != null && task2.getResult().isEmpty()) {
                                        firestoreDB.collection("CHATS")
                                                .add(chat)
                                                .addOnCompleteListener(task3 -> {
                                                    if (task3.isSuccessful()) {
                                                        Toast.makeText(ChatActivity.this, "Chat added to CHATS collection!", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Toast.makeText(ChatActivity.this, "Error adding chat to CHATS collection!", Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                });
                    }
                });
    }

    private void loadMessages() {
        String senderId = currentUser.getUid();
        String receiverId = getIntent().getStringExtra("receiverId");
        // Create an array of user IDs for the two directions of the chat
        List<String> chatUserIds = new ArrayList<>();
        chatUserIds.add(senderId);
        chatUserIds.add(receiverId);

        // Build a query to get messages sent between the two users in both directions
        chatMessagesCollection
                .whereIn("senderId", chatUserIds)
                .whereIn("receiverId", chatUserIds)
                .orderBy("datetime")  // You can adjust the ordering as needed
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            messages.clear();
                            messages.addAll(task.getResult().toObjects(MessageModel.class));
                            messageAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }


}