package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    MaterialButton btnLogin;
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
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        executorService = Executors.newSingleThreadExecutor();

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if (validateInput(email, password)) {
                    startAuthentication(email, password);
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to the entry activity
                Intent intent = new Intent(LoginActivity.this, EntryActivity.class);
                startActivity(intent);
                finish(); // finish the current activity to remove it from the stack
            }
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
                            if (user != null) {
                                saveUserDetailsToSharedPreferences(user.getUid(), email);
                                updateUI(true);
                            } else {
                                // Unexpected error: User is null
                                updateUI(false);
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
        FirebaseFirestore firestoreDB = FirebaseFirestore.getInstance();
        DocumentReference userDocRef = firestoreDB.collection("USERS").document(userId);

        userDocRef.get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Fetch user details from Firestore
                        String name = documentSnapshot.getString("NAME");
                        String username = documentSnapshot.getString("USERNAME");
                        String phone = documentSnapshot.getString("PHONE_NUMBER");
                        String county = documentSnapshot.getString("COUNTY");
                        String city = documentSnapshot.getString("CITY");
                        String bio = documentSnapshot.getString("BIO");

                        // Save user details to SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("email", email);
                        editor.putString("name", name);
                        editor.putString("username", username);
                        editor.putString("phone_number", phone);
                        editor.putString("county", county);
                        editor.putString("city", city);
                        editor.putString("bio", bio);
                        editor.putString("profile_picture", null);
                        editor.apply();
                    } else {
                        // Unexpected error: User document doesn't exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle any errors that occur during the Firestore query
                    Toast.makeText(LoginActivity.this, "Error fetching user details", Toast.LENGTH_LONG).show();
                });
    }

    private void updateUI(boolean success) {
        progressBar.setVisibility(View.GONE);
        if (success) {
            // navigate to the home activity
            Intent intent = new Intent(LoginActivity.this, HomePageActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        } else {
            Toast.makeText(LoginActivity.this, "Incorrect email or password", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
}
