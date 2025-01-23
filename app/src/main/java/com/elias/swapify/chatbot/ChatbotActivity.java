package com.elias.swapify.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.R;
import com.elias.swapify.keyboard.KeyboardUtils;
import com.elias.swapify.principalactivities.TicketSupportActivity;
import com.elias.swapify.secrets.SecretsManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import android.content.SharedPreferences;

public class ChatbotActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private EditText editTextMessage;
    private ImageButton buttonSend, buttonBack;
    private ChatbotMessageAdapter messageAdapter;
    private List<Message> messageList;
    private ProgressBar progressBar;
    private FloatingActionButton fabTicket;

    private OpenAIApi openAIApi;
    private String apiKey;

    private SharedPreferences sharedPreferences;
    private static final String SHARED_PREFS_KEY = "chat_prefs";
    private static final String MESSAGES_KEY = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatbot);

        SecretsManager secretsManager = new SecretsManager(this);
        apiKey = "Bearer " + secretsManager.getSecret("com.elias.swapify.OPENAI_API_KEY");
        Log.d("ChatActivity", "API key: " + apiKey);

        recyclerView = findViewById(R.id.recyclerView);
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        buttonBack = findViewById(R.id.buttonBack);
        progressBar = findViewById(R.id.progressBar);
        fabTicket = findViewById(R.id.fab_create_ticket);

        messageList = new ArrayList<>();
        messageAdapter = new ChatbotMessageAdapter(messageList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        openAIApi = retrofit.create(OpenAIApi.class);

        sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);

        loadMessagesFromPreferences();

        KeyboardUtils.addKeyboardVisibilityListener(this, isVisible -> {
            if (isVisible) {
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });

        buttonSend.setOnClickListener(view -> {
            String messageContent = editTextMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessageToAPI(messageContent);
                editTextMessage.setText("");
                addMessageToChat(messageContent, true);
            }
        });

        buttonBack.setOnClickListener(view -> {
            saveMessagesToPreferences();
            finish();
        });

        fabTicket.setOnClickListener(view -> {
            saveMessagesToPreferences();
            Intent intent = new Intent(ChatbotActivity.this, TicketSupportActivity.class);
            startActivity(intent);
        });
    }

    private void loadMessagesFromPreferences() {
        String json = sharedPreferences.getString(MESSAGES_KEY, null);
        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<Message>>() {}.getType();
            List<Message> savedMessages = gson.fromJson(json, type);
            messageList.addAll(savedMessages);
            messageAdapter.notifyDataSetChanged();
        }
    }

    private void saveMessagesToPreferences() {
        Gson gson = new Gson();
        String json = gson.toJson(messageList);
        sharedPreferences.edit().putString(MESSAGES_KEY, json).apply();
    }

    private void sendMessageToAPI(String messageContent) {
        progressBar.setVisibility(View.VISIBLE); // Show the loading indicator

        List<ChatRequest.Message> messages = new ArrayList<>();
        messages.add(new ChatRequest.Message("user", messageContent));

        ChatRequest request = new ChatRequest("gpt-3.5-turbo", messages);

        Call<ChatResponse> call = openAIApi.getChatResponse(request, apiKey);
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                progressBar.setVisibility(View.GONE); // Hide the loading indicator

                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getChoices().get(0).getMessage().getContent();
                    addMessageToChat(reply, false);
                } else {
                    Log.e("ChatActivity", "Response not successful");
                    Log.e("ChatActivity", response.toString());
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                progressBar.setVisibility(View.GONE); // Hide the loading indicator
                Log.e("ChatActivity", "API call failed", t);

                // Handle the error
                Toast.makeText(ChatbotActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addMessageToChat(String content, boolean isSentByUser) {
        Message message = new Message(content, isSentByUser);
        messageList.add(message);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);
    }
}