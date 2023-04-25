package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import android.content.Intent;

public class RegisterActivity extends AppCompatActivity {

    MaterialButton btnRegister;
    EditText edtName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    ListView lstCustomers;
    private DBObject db;
    ImageButton btnBack;

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

        db = new DBObject(this);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                if (validateInput()) {
                    CreateCustomerTask task = new CreateCustomerTask();
                    task.execute();
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

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private class CreateCustomerTask extends AsyncTask<Void, Void, CustomerModel> {

        @Override
        protected CustomerModel doInBackground(Void... voids) {
            String name = edtName.getText().toString();
            String username = edtUsername.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();

            // Create a new customer
            return new CustomerModel(name, username, email, password);
        }

        @Override
        protected void onPostExecute(CustomerModel customer) {
            // Add the customer to the database
            boolean success = db.addOne(customer);
            // Show the customer in a toast message
            if (success) {
                Toast.makeText(RegisterActivity.this, "Customer added", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Error adding customer", Toast.LENGTH_LONG).show();
            }
        }

    }
}
