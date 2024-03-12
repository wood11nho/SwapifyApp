package com.example.swapify.principalactivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.swapify.R;
import com.example.swapify.users.LoginActivity;
import com.example.swapify.users.RegisterActivity;
import com.example.swapify.users.VerifyEmailActivity;
import com.google.firebase.auth.FirebaseAuth;

public class EntryActivity extends AppCompatActivity {
    Button registerButton;
    Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entry);
        registerButton = findViewById(R.id.sign_up_button);
        loginButton = findViewById(R.id.log_in_button);

        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            // User is logged in, redirect to HomePageActivity
            if (!FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()) {
                // just log out but remain on the entry page
                FirebaseAuth.getInstance().signOut();
            }
            else {
                Intent intent = new Intent(EntryActivity.this, HomePageActivity.class);
                startActivity(intent);
                finish();
            }
        } else {
            // User is not logged in, show the entry UI and set the button listeners
            registerButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EntryActivity.this, RegisterActivity.class);
                    startActivity(intent);
                    finish();
                }
            });

            loginButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(EntryActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }
}
