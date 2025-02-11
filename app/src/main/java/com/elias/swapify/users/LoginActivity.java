package com.elias.swapify.users;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.elias.swapify.principalactivities.EntryActivity;
import com.elias.swapify.principalactivities.HomePageActivity;
import com.elias.swapify.R;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {
    MaterialButton btnLogin, btnForgotPassword;
    EditText edtEmail, edtPassword;
    ImageButton btnBack;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

    private ExecutorService executorService;
    private Future<?> authenticationTaskFuture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        executorService = Executors.newSingleThreadExecutor();

        btnLogin.setOnClickListener(v -> {
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if (validateInput(email, password)) {
                startAuthentication(email, password);
            }
        });

        btnBack.setOnClickListener(v -> {
            // navigate to the entry activity
            Intent intent = new Intent(LoginActivity.this, EntryActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });

        btnForgotPassword.setOnClickListener(v -> {
            // navigate to the forgot password activity
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });
    }

    private boolean validateInput(String email, String password) {
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return false;
        }
        if (!isValidEmail(email)) {
            Toast.makeText(LoginActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void startAuthentication(String email, String password) {
        if (authenticationTaskFuture == null || authenticationTaskFuture.isDone()) {
            progressDialog = ProgressDialog.show(LoginActivity.this, "", "Logging in...", true);

            FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, task -> {
                        progressDialog.dismiss();

                        if (task.isSuccessful()) {
                            // Authentication successful, update UI with the signed-in user's information
                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            assert user != null;
                            if (user.isEmailVerified()) {
                                saveUserDetailsToSharedPreferences(user.getUid(), email);
                                updateUI(true);
                            }
                            else {
                                Toast.makeText(LoginActivity.this, "Please verify your email", Toast.LENGTH_LONG).show();
                                updateUIforUnverifiedEmail();
                            }
                        } else {
                            // Authentication failed
                            Toast.makeText(LoginActivity.this, "Incorrect email or password", Toast.LENGTH_LONG).show();
                            updateUI(false);
                        }
                    });
        }
    }

    private void saveUserDetailsToSharedPreferences(String userId, String email) {
        // Here, we don't need to fetch additional user details from Firestore.
        // We can directly access the user's information from the FirebaseUser object.
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user != null) {
            String name = user.getDisplayName();
            String phoneNumber = user.getPhoneNumber();
            // You can access other user details using user.getEmail(), user.getPhotoUrl(), etc.

            // Save user details to SharedPreferences
            SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("email", email);
            editor.putString("name", name);
            editor.putString("phonenumber", phoneNumber);
            editor.putString("profilepicture", null);
            editor.apply();
        }
    }

    private void updateUI(boolean success) {
        progressBar.setVisibility(View.GONE);
        if (success) {
            // Navigate to the home page
            Intent homePageIntent = new Intent(LoginActivity.this, HomePageActivity.class);
            startActivity(homePageIntent);
            finish(); // finish the current activity to remove it from the stack
        }  // Stay on the login page
    }

    private void updateUIforUnverifiedEmail() {
        // Navigate to the verify email page
        Intent verifyEmailIntent = new Intent(LoginActivity.this, VerifyEmailActivity.class);
        startActivity(verifyEmailIntent);
        finish(); // finish the current activity to remove it from the stack
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}

