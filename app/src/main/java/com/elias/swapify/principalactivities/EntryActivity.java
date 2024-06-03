package com.elias.swapify.principalactivities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.elias.swapify.R;
import com.elias.swapify.chatbot.ChatbotActivity;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.onboarding.OnboardingScreensActivity;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.RegisterActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.perf.FirebasePerformance;

public class EntryActivity extends AppCompatActivity {
    Button registerButton;
    Button loginButton;
    FloatingActionButton fabSupport, fabInfo;
    private static final String SHARED_PREFS_KEY = "chat_prefs";
    private static final String MESSAGES_KEY = "messages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Clear the shared preferences
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS_KEY, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(MESSAGES_KEY);
        editor.apply();

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                DebugAppCheckProviderFactory.getInstance());
        FirebasePerformance.getInstance().setPerformanceCollectionEnabled(true);

        registerButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.log_in_button);
        fabSupport = findViewById(R.id.fabSupport);
        fabInfo = findViewById(R.id.fabInfo);

        checkUserStatus();

        setUpButtonListeners();
    }

    private void checkUserStatus() {
        if (FirebaseUtil.isUserLoggedIn()) {
            if (!FirebaseUtil.isEmailVerified()) {
                FirebaseUtil.signOut(); // Remain on the entry page
            } else {
                Log.d("EntryActivity", "User is logged in");
                Intent intent = new Intent(EntryActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        }
    }

    private void setUpButtonListeners() {
        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTo(RegisterActivity.class);
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTo(LoginActivity.class);
            }
        });

        fabSupport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                Intent intent = new Intent(EntryActivity.this, ChatbotActivity.class);
                startActivity(intent);
            }
        });

        fabInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navigateTo(OnboardingScreensActivity.class);
            }
        });
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(EntryActivity.this, cls);
        startActivity(intent);
        finish();
    }
}