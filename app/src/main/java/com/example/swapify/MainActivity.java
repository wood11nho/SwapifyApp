package com.example.swapify;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.example.swapify.DBObject;


public class MainActivity extends AppCompatActivity {

    MaterialButton btnRegister;
    EditText edtName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    ListView lstCustomers;
    private DBObject db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        btnRegister = findViewById(R.id.btnRegister);
        edtName = findViewById(R.id.edtName);
        edtUsername = findViewById(R.id.edtUsername);
        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        lstCustomers = findViewById(R.id.lvCustomerList);

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
    }

    private boolean validateInput() {
        String name = edtName.getText().toString();
        String username = edtUsername.getText().toString();
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();
        String confirmPassword = edtConfirmPassword.getText().toString();

        // Validate input fields
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify if the password and confirm password are the same
        if (!password.equals(confirmPassword)) {
            Toast.makeText(MainActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify if the email is valid
        if (!isValidEmail(email)) {
            Toast.makeText(MainActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
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
                Toast.makeText(MainActivity.this, "Customer added", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(MainActivity.this, "Error adding customer", Toast.LENGTH_LONG).show();
            }
        }
    }
}
