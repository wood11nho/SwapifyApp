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

public class LoginActivity extends AppCompatActivity {

    MaterialButton btnLogin;
    EditText edtEmail, edtPassword;
    ImageButton btnBack;
    private DBObject db;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;

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

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();
                if (validateInput(email, password)) {
                    showProgressDialog();
                    authenticateUser(email, password);
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

    private void showProgressDialog() {
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.setMessage("Authenticating...");
        progressDialog.show();
    }

    private void authenticateUser(final String email, final String password) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                // Authenticate the user
                boolean success = db.authenticate(email, password);
                if (success) {
                    // Save the user details to SharedPreferences
                    SharedPreferences sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
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

                final boolean finalSuccess = success;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        dismissProgressDialog();
                        updateUI(finalSuccess);
                    }
                });
            }
        });
        thread.start();
    }

    private void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
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
}
