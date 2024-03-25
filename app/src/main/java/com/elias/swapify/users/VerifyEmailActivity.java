package com.elias.swapify.users;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.elias.swapify.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class VerifyEmailActivity extends AppCompatActivity {
    EditText emailEditText;
    Button reverifyEmailButton;
    Button continueButton;
    ProgressBar progressBar;
    FrameLayout overlay;

    private static final long RESEND_EMAIL_TIMEOUT = 60000; // 1 minute
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        emailEditText = findViewById(R.id.email_field);
        reverifyEmailButton = findViewById(R.id.resend_verification_email_button);
        continueButton = findViewById(R.id.continue_button);
        progressBar = findViewById(R.id.progress_bar);
        overlay = findViewById(R.id.overlay);

        emailEditText.setText(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getEmail());
        blockReverifyEmailButtonForFirstTime();

        reverifyEmailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Send the user a verification email
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                user.reload();
                if (!user.isEmailVerified()){
                    user.sendEmailVerification().addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(VerifyEmailActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                            startTimer();
                        } else {
                            Toast.makeText(VerifyEmailActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(VerifyEmailActivity.this, "Email already verified", Toast.LENGTH_SHORT).show();
                }
                user.reload();
            }
        });
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user != null) {
                    progressBar.setVisibility(View.VISIBLE);
                    overlay.setVisibility(View.VISIBLE);
                    continueButton.setEnabled(false);
                    new Handler().postDelayed(() -> {
                        user.reload().addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            overlay.setVisibility(View.GONE);
                            continueButton.setEnabled(true);
                            if (user.isEmailVerified()) {
                                FirebaseAuth.getInstance().signOut();
                                Intent intent = new Intent(VerifyEmailActivity.this, LoginActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                Toast.makeText(VerifyEmailActivity.this, "Please verify your email", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }, 5000); // 5-second delay
                }
            }
        });
    }

    private void startTimer(){
        reverifyEmailButton.setEnabled(false);
        new CountDownTimer(RESEND_EMAIL_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                reverifyEmailButton.setText("Resend Verification Email (" + millisUntilFinished / 1000 + "s)");
            }

            public void onFinish() {
                reverifyEmailButton.setText("Resend Verification Email");
                reverifyEmailButton.setEnabled(true);
            }
        }.start();
    }

    private void blockReverifyEmailButtonForFirstTime(){
        reverifyEmailButton.setEnabled(false);
        new CountDownTimer(RESEND_EMAIL_TIMEOUT, 1000) {
            public void onTick(long millisUntilFinished) {
                reverifyEmailButton.setText("Resend Verification Email (" + millisUntilFinished / 1000 + "s)");
            }

            public void onFinish() {
                reverifyEmailButton.setText("Resend Verification Email");
                reverifyEmailButton.setEnabled(true);
            }
        }.start();
    }
}
