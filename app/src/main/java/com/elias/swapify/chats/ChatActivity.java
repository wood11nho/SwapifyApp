package com.elias.swapify.chats;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.elias.swapify.cloudmessaging.AccessTokenTask;
import com.elias.swapify.cloudmessaging.AuthUtil;
import com.elias.swapify.cloudmessaging.FCMApi;
import com.elias.swapify.cloudmessaging.FCMRequest;
import com.elias.swapify.cloudmessaging.FCMResponse;
import com.elias.swapify.cloudmessaging.RetrofitClientFCM;
import com.elias.swapify.secrets.SecretsManager;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseFirestore firestoreDB;
    private RecyclerView recyclerViewChatMessages;
    private EditText editTextMessageInput;
    private ImageButton buttonSendMessage, backButton;
    private MessageAdapter messageAdapter;
    private List<MessageModel> messages;
    private TextView otherPersonNameTextView;
    private ImageView otherPersonAvatarImageView;
    private FCMApi fcmApi;
    private String SERVER_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        getWindow().setBackgroundDrawableResource(R.drawable.background);
        SecretsManager secretsManager = new SecretsManager(this);
        SERVER_KEY = "Bearer " + secretsManager.getSecret("com.elias.swapify.ACCESS_TOKEN");

        firebaseAuth = FirebaseAuth.getInstance();
        firestoreDB = FirebaseFirestore.getInstance();
        currentUser = firebaseAuth.getCurrentUser();

        initViews();
        setupRecyclerView();
        setupListeners();

        otherPersonNameTextView.setText(getIntent().getStringExtra("otherPersonName"));
        loadAvatar();
        loadMessages();

        fcmApi = RetrofitClientFCM.getRetrofitInstance().create(FCMApi.class);

        final View activityRootView = findViewById(R.id.chatActivityLayout);
        activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = activityRootView.getRootView().getHeight() - activityRootView.getHeight();
            // If more than 100 pixels, its probably a keyboard...
            if (heightDiff > dpToPx(100)) { // 100dp threshold is considered as keyboard.
                if (messageAdapter.getItemCount() > 0) {
                    recyclerViewChatMessages.smoothScrollToPosition(messageAdapter.getItemCount() - 1);
                }
            }
        });
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void initViews() {
        recyclerViewChatMessages = findViewById(R.id.recyclerViewChatMessages);
        editTextMessageInput = findViewById(R.id.editTextMessageInput);
        buttonSendMessage = findViewById(R.id.buttonSendMessage);
        backButton = findViewById(R.id.btnBack_chat);
        otherPersonNameTextView = findViewById(R.id.otherPersonNameTextView);
        otherPersonAvatarImageView = findViewById(R.id.otherPersonAvatarImageView);
    }

    private void setupRecyclerView() {
        messages = new ArrayList<>();
        messageAdapter = new MessageAdapter(this, messages);
        recyclerViewChatMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChatMessages.setAdapter(messageAdapter);
    }

    private void setupListeners() {
        buttonSendMessage.setOnClickListener(view -> sendMessage());

        backButton.setOnClickListener(v -> finish());

        editTextMessageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                recyclerViewChatMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessage() {
        String messageContent = editTextMessageInput.getText().toString().trim();
        if (!messageContent.isEmpty()) {
            MessageModel message = new MessageModel(currentUser.getUid(), getIntent().getStringExtra("receiverId"), messageContent, new Timestamp(new Date()).toDate());
            firestoreDB.collection("MESSAGES").add(message)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            editTextMessageInput.setText("");
                            Toast.makeText(ChatActivity.this, "Message sent!", Toast.LENGTH_SHORT).show();
                            loadMessages(); // Consider optimizing this to not reload all messages
                            sendNotificationToReceiver(messageContent);
                        } else {
                            Toast.makeText(ChatActivity.this, "Error sending message!", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void sendNotificationToReceiver(String messageContent) {
        String receiverId = getIntent().getStringExtra("receiverId");

        // Fetch receiver's FCM token from Firestore
        firestoreDB.collection("USERS").document(receiverId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String fcmToken = task.getResult().getString("fcmToken");
                        String profilePicture = task.getResult().getString("profilepicture");
                        if (fcmToken != null) {
                            FCMRequest.Message.Notification notification = new FCMRequest.Message.Notification(
                                    "New Message from " + getIntent().getStringExtra("otherPersonName"),
                                    messageContent,
                                    profilePicture
                            );

                            FCMRequest.Message.Data data = new FCMRequest.Message.Data(
                                    "value1",
                                    "value2"
                            );

                            FCMRequest.Message message = new FCMRequest.Message(
                                    fcmToken,
                                    notification,
                                    data
                            );

                            FCMRequest request = new FCMRequest(message);

                            new AccessTokenTask(this, accessToken -> {
                                if (accessToken != null) {
                                    Call<FCMResponse> call = fcmApi.sendNotification(request, "Bearer " + accessToken);
                                    call.enqueue(new Callback<FCMResponse>() {
                                        @Override
                                        public void onResponse(Call<FCMResponse> call, Response<FCMResponse> response) {
                                            if (response.isSuccessful()) {
                                                Log.d("ChatActivity", "Notification sent successfully");
                                            } else {
                                                Log.e("ChatActivity", "Notification sending failed: " + response.errorBody());
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<FCMResponse> call, Throwable t) {
                                            Log.e("ChatActivity", "Notification sending failed", t);
                                        }
                                    });
                                } else {
                                    Log.e("ChatActivity", "Failed to get access token");
                                }
                            }).execute();
                        } else {
                            Log.e("ChatActivity", "FCM token is null for user: " + receiverId);
                        }
                    } else {
                        Log.e("ChatActivity", "Failed to fetch user data for user: " + receiverId);
                    }
                });
    }



    private void loadMessages() {
        String senderId = currentUser.getUid();
        String receiverId = getIntent().getStringExtra("receiverId");

        firestoreDB.collection("MESSAGES")
                .whereIn("senderId", Arrays.asList(senderId, receiverId))
                .whereIn("receiverId", Arrays.asList(senderId, receiverId))
                .orderBy("datetime")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.w("ChatActivity", "Listen failed.", e);
                        return;
                    }

                    messages.clear();
                    assert queryDocumentSnapshots != null;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        messages.add(doc.toObject(MessageModel.class));
                    }
                    messageAdapter.notifyDataSetChanged();
                    recyclerViewChatMessages.scrollToPosition(messageAdapter.getItemCount() - 1);
                });
    }

    private void loadAvatar() {
        String receiverId = getIntent().getStringExtra("receiverId");
        FirebaseFirestore.getInstance().collection("USERS").document(receiverId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        String avatarUrl = task.getResult().getString("profilepicture");
                        if (avatarUrl != null && !avatarUrl.isEmpty()) {
                            Glide.with(ChatActivity.this)
                                    .load(avatarUrl)
                                    .placeholder(R.drawable.ic_profile_fulfill)
                                    .error(R.drawable.ic_profile_fulfill)
                                    .into(otherPersonAvatarImageView);
                        } else {
                            otherPersonAvatarImageView.setImageResource(R.drawable.ic_profile_fulfill);
                        }
                    } else {
                        otherPersonAvatarImageView.setImageResource(R.drawable.ic_profile_fulfill);
                    }
                });
    }
}