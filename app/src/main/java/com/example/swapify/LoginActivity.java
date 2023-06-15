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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginActivity extends AppCompatActivity {

    MaterialButton btnLogin;
    EditText edtEmail, edtPassword;
    ImageButton btnBack;
    private DBObject db;
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

        db = new DBObject(this);
        Log.d("Database version: ", String.valueOf(db.getReadableDatabase().getVersion()));

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
            authenticationTaskFuture = executorService.submit(() -> {
                boolean success = db.authenticate(email, password);
                if (success) {
                    saveUserDetailsToSharedPreferences(email);
                }
                updateUI(success);
            });
        }
    }

    private void saveUserDetailsToSharedPreferences(String email) {
        SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt("id", db.getId(email));
        editor.putString("email", email);
        editor.putString("username", db.getUsername(email));
        editor.putString("name", db.getName(email));
        editor.putString("phone_number", db.getPhone(email));
        Log.d(db.getPhone(email), "merge: ");
        editor.putString("county", db.getCounty(email));
        editor.putString("city", db.getCity(email));
        editor.putString("bio", db.getBio(email));
        editor.putString("profile_picture", null);
        editor.apply();
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
