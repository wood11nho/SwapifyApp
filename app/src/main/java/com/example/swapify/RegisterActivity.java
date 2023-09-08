package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {
    MaterialButton btnRegister;
    EditText edtName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    private FirebaseFirestore firestoreDB;
    private FirebaseAuth firebaseAuth;
    ImageButton btnBack;

    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);


        btnRegister = findViewById(R.id.btnRegister);
        edtName = findViewById(R.id.edtName);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnBack = findViewById(R.id.btnBack);

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        executorService = Executors.newSingleThreadExecutor();

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String username = edtUsername.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if (validateInput(name, username, email, password)) {
                // if the name for example was written in all caps, or all lowercase, i want to change it to first letter uppercase for each word
                String[] nameArray = name.split(" ");
                StringBuilder nameFormatted = new StringBuilder();
                for (String word : nameArray) {
                    nameFormatted.append(word.substring(0, 1).toUpperCase()).append(word.substring(1).toLowerCase()).append(" ");
                }
                nameFormatted = new StringBuilder(nameFormatted.toString().trim());
                name = nameFormatted.toString();
                registerUserWithEmailAndPassword(name, username, email, password);
            }
        });

        btnBack.setOnClickListener(v -> {
            // navigate to the entry activity
            Intent intent = new Intent(RegisterActivity.this, EntryActivity.class);
            startActivity(intent);
            finish(); // finish the current activity to remove it from the stack
        });
    }

    private boolean validateInput(String name, String username, String email, String password) {
        // Validate input fields
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify if the email is valid
        if (!isValidEmail(email)) {
            Toast.makeText(RegisterActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify if the email already exists
        if (emailAlreadyExists(email)) {
            Toast.makeText(RegisterActivity.this, "Email already exists", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify regex for name: "^[A-Za-z]+(?:\s+[A-Za-z]+)*$"
        String namePattern = "^[A-Za-z]+(?:\\s+[A-Za-z]+)*$";
        if (!name.matches(namePattern)) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid full name (only alphabetical characters are allowed)", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify if password and confirm password match
        String confirmPassword = edtConfirmPassword.getText().toString();
        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify regex for password: "^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()])[A-Za-z\d!@#$%^&*()]{8,}$"
        String passwordPattern = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$";
        if (!password.matches(passwordPattern)) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters long and contain at least one letter, one number and one special character (!@#$%^&*())", Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean emailAlreadyExists(String email) {
        // Create a reference to the "USERS" collection
        CollectionReference usersCollection = firestoreDB.collection("USERS");

        // Create a query against the collection
        Task<QuerySnapshot> query = usersCollection.whereEqualTo("email", email).get();

        // Get the result of the query
        while (!query.isComplete()) {
            // Wait for the query to complete
        }

        // Check if the query returned any results
        return query.getResult().size() > 0;
    }

    private void registerUserWithEmailAndPassword(String name, String username, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // User registration successful, save additional user data in Firestore
                        FirebaseUser user = firebaseAuth.getCurrentUser();
                        if (user != null) {
                            saveUserDataToFirestore(name, username, email, user.getUid());
                        } else {
                            // Unexpected error: User is null
                            Toast.makeText(RegisterActivity.this, "Error during user registration", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        // User registration failed
                        Toast.makeText(RegisterActivity.this, "Error during user registration", Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserDataToFirestore(String name, String username, String email, String userId) {
        // Create a new customer
        CustomerModel customer = new CustomerModel();
        customer.setName(name);
        customer.setUsername(username);
        customer.setEmail(email);

        // Add the customer to the "USERS" collection in Firestore
        CollectionReference usersCollection = firestoreDB.collection("USERS");
        usersCollection.document(userId)
                .set(customer)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegisterActivity.this, "User registration successful", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error adding user to Firestore", Toast.LENGTH_LONG).show());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }
}
