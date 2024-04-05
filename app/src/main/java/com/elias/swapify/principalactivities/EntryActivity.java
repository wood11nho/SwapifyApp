package com.elias.swapify.principalactivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.elias.swapify.R;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.users.LoginActivity;
import com.elias.swapify.users.RegisterActivity;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory;

public class EntryActivity extends AppCompatActivity {
    Button registerButton;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);

        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(
                PlayIntegrityAppCheckProviderFactory.getInstance());

        registerButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.log_in_button);

        if (FirebaseUtil.isUserLoggedIn()) {
            if (!FirebaseUtil.isEmailVerified()) {
                FirebaseUtil.signOut(); // Remain on the entry page
            } else {
                Intent intent = new Intent(EntryActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            setUpButtonListeners();
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
    }

    private void navigateTo(Class<?> cls) {
        Intent intent = new Intent(EntryActivity.this, cls);
        startActivity(intent);
        finish();
    }
}