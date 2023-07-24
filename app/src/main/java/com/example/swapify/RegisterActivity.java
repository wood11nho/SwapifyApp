package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterActivity extends AppCompatActivity {

    MaterialButton btnRegister;
    EditText edtName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    ListView lstCustomers;
    private FirebaseFirestore firestoreDB;
    ImageButton btnBack;

    private ExecutorService executorService;
    private Future<?> createCustomerTaskFuture;

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
        lstCustomers = findViewById(R.id.lvCustomerList);
        btnBack = findViewById(R.id.btnBack);

        firestoreDB = FirebaseFirestore.getInstance();

        executorService = Executors.newSingleThreadExecutor();

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (validateInput()) {
                    if(createCustomerTaskFuture == null || createCustomerTaskFuture.isDone()){
                        createCustomerTaskFuture = executorService.submit(new CreateCustomerTask());
                    }
                }
            }
        });

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // navigate to the entry activity
                Intent intent = new Intent(RegisterActivity.this, EntryActivity.class);
                startActivity(intent);
                finish(); // finish the current activity to remove it from the stack
            }
        });
    }

    private boolean validateInput() {
        String name = edtName.getText().toString();
        String username = edtUsername.getText().toString();
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // Validate input fields
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify if the password and confirm password are the same
        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
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
        if (query.getResult().size() > 0) {
            return true;
        }

        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    private class CreateCustomerTask implements Runnable{

            @Override
            public void run() {
                String name = edtName.getText().toString();
                String username = edtUsername.getText().toString();
                String email = edtEmail.getText().toString();
                String password = edtPassword.getText().toString();

                // Create a new customer
                CustomerModel customer = new CustomerModel(name, username, email, password);

                // Add the customer to the database
                CollectionReference usersCollection = firestoreDB.collection("USERS");
                usersCollection.add(customer).
                        addOnSuccessListener(documentReference -> {
                            Toast.makeText(RegisterActivity.this, "User added successfully", Toast.LENGTH_LONG).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }).
                        addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error adding user", Toast.LENGTH_LONG).show());
            }
    }
}
