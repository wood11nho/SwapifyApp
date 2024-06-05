package com.elias.swapify.principalactivities;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.elias.swapify.R;
import com.elias.swapify.chatbot.ChatbotActivity;
import com.elias.swapify.chats.ChatActivity;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.elias.swapify.onboarding.OnboardingScreensActivity;
import com.elias.swapify.users.CustomerModel;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.perf.FirebasePerformance;

public class EntryActivity extends AppCompatActivity {
    Button registerButton;
    Button loginButton;
    FloatingActionButton fabSupport, fabInfo;
    private static final String SHARED_PREFS_KEY = "chat_prefs";
    private static final String MESSAGES_KEY = "messages";
    public static final String TAG = "GenerateToken";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        // Retrieve the current registration token
        retrieveCurrentRegistrationToken();

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
                if (getIntent().getExtras() != null) {
                    // from notification
                    String userId = getIntent().getStringExtra("userId");
                    if (userId != null && !userId.isEmpty()) {
                        navigateToChat(userId);
                    } else {
                        navigateToHome();
                    }
                } else {
                    navigateToHome();
                }
            }
        } else {
            if (getIntent().getExtras() != null && getIntent().getStringExtra("userId") != null) {
                // user is not logged in but notification is clicked
                navigateToLogin();
            }
        }
    }

    private void navigateToLogin() {
        Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void navigateToChat(String userId) {
        FirestoreUtil.usersCollection().document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        CustomerModel user = task.getResult().toObject(CustomerModel.class);
                        Intent intent = new Intent(this, HomePageActivity.class);
                        intent.putExtra("receiverId", userId);
                        if (user != null) {
                            intent.putExtra("otherPersonName", user.getName());
                        }
                        startActivity(intent);
                        finish();
                    }
                });
    }

    private void navigateToHome() {
        Intent intent = new Intent(EntryActivity.this, HomePageActivity.class);
        startActivity(intent);
        finish();
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

    private void retrieveCurrentRegistrationToken() {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            Log.w("EntryActivity", "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new FCM registration token
                        String token = task.getResult();
                        if (token != null) {
                            FirestoreUtil.updateUserDetails("fcmToken", token, new FirestoreUtil.OnUpdateUserDetailsListener() {
                                        @Override
                                        public void onUpdateSuccess() {
                                            Log.d("EntryActivity", "Token updated successfully");
                                        }

                                        @Override
                                        public void onUpdateFailure(String error) {
                                            navigateToLogin();
                                        }
                                    });

                                    // Log and toast
                                    Log.d("EntryActivity", token);
//                            Toast.makeText(EntryActivity.this, token, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}