package com.elias.swapify.users;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.elias.swapify.principalactivities.EntryActivity;
import com.elias.swapify.R;
import com.elias.swapify.userpreferences.CustomerPreferencesModel;
import com.elias.swapify.wishlists.WishlistModel;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {
    MaterialButton btnRegister;
    EditText edtName, edtUsername, edtEmail, edtPassword, edtConfirmPassword;
    TextView tvPasswordLength, tvPasswordSpecialChar, tvPasswordNumber, tvPasswordLetter, tvPasswordMatch;
    FirebaseFirestore firestoreDB;
    FirebaseAuth firebaseAuth;
    ImageButton btnBack;
    private ExecutorService executorService;
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z]+(?:\\s+[A-Za-z]+)*$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()])[A-Za-z\\d!@#$%^&*()]{8,}$");
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
        tvPasswordLength = findViewById(R.id.tvPasswordLength);
        tvPasswordSpecialChar = findViewById(R.id.tvPasswordSpecialChar);
        tvPasswordNumber = findViewById(R.id.tvPasswordNumber);
        tvPasswordLetter = findViewById(R.id.tvPasswordLetter);
        tvPasswordMatch = findViewById(R.id.tvPasswordMatch);

        firestoreDB = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        executorService = Executors.newSingleThreadExecutor();

        btnRegister.setOnClickListener(v -> {
            String name = edtName.getText().toString();
            String username = edtUsername.getText().toString();
            String email = edtEmail.getText().toString();
            String password = edtPassword.getText().toString();
            if (validateInput(name, username, email, password)) {
                // if the name for elias was written in all caps, or all lowercase, i want to change it to first letter uppercase for each word
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

        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                updatePasswordIndicators(charSequence.toString());

                String password = edtPassword.getText().toString();
                String confirmPassword = edtConfirmPassword.getText().toString();
                if (!password.equals(confirmPassword)) {
                    tvPasswordMatch.setTextColor(getResources().getColor(R.color.red_500));
                } else {
                    tvPasswordMatch.setTextColor(getResources().getColor(R.color.green_700));
                }
            }
            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2){

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2){
                String password = edtPassword.getText().toString();
                String confirmPassword = edtConfirmPassword.getText().toString();
                if (!password.equals(confirmPassword)) {
                    tvPasswordMatch.setTextColor(getResources().getColor(R.color.red_500));
                } else {
                    tvPasswordMatch.setTextColor(getResources().getColor(R.color.green_700));
                }
            }

            @Override
            public void afterTextChanged(Editable editable){

            }
        });
    }

    private void updatePasswordIndicators(String password) {
        if (password.length() < 8) {
            tvPasswordLength.setTextColor(getResources().getColor(R.color.red_500));
        } else {
            tvPasswordLength.setTextColor(getResources().getColor(R.color.green_700));
        }
        if (!password.matches(".*[!@#$%^&*()].*")) {
            tvPasswordSpecialChar.setTextColor(getResources().getColor(R.color.red_500));
        } else {
            tvPasswordSpecialChar.setTextColor(getResources().getColor(R.color.green_700));
        }
        if (!password.matches(".*\\d.*")) {
            tvPasswordNumber.setTextColor(getResources().getColor(R.color.red_500));
        } else {
            tvPasswordNumber.setTextColor(getResources().getColor(R.color.green_700));
        }
        if (!password.matches(".*[A-Za-z].*")) {
            tvPasswordLetter.setTextColor(getResources().getColor(R.color.red_500));
        } else {
            tvPasswordLetter.setTextColor(getResources().getColor(R.color.green_700));
        }
    }

    public boolean validateInput(String name, String username, String email, String password) {
        if (name.isEmpty() || username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Toast.makeText(RegisterActivity.this, "Please fill in all fields", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(RegisterActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
            return false;
        }

        if (!name.matches(NAME_PATTERN.pattern())) {
            Toast.makeText(RegisterActivity.this, "Please enter a valid full name (only alphabetical characters are allowed)", Toast.LENGTH_LONG).show();
            return false;
        }

        String confirmPassword = edtConfirmPassword.getText().toString();
        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegisterActivity.this, "Passwords do not match", Toast.LENGTH_LONG).show();
            return false;
        }

        // Verify regex for password: "^(?=.*[A-Za-z])(?=.*\d)(?=.*[!@#$%^&*()])[A-Za-z\d!@#$%^&*()]{8,}$"
        // Password must be at least 8 characters long and contain at least one letter, one number and one special character (!@#$%^&*())
        if (!password.matches(PASSWORD_PATTERN.pattern())) {
            Toast.makeText(RegisterActivity.this, "Password must be at least 8 characters long and contain at least one letter, one number and one special character (!@#$%^&*())",
                    Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public void registerUserWithEmailAndPassword(String name, String username, String email, String password) {
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
                        // Here we should toast what exactly went wrong, not a general message, so we create an IF-ELSE statement
                        if (task.getException() != null) {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            // If email already exists, verify if the mail is not verified, if so, send verification e-mail and go to VerifyEmailActivity
                            if (task.getException().getMessage().equals("The email address is already in use by another account.")) {
                                // Authenticate the user
                                firebaseAuth.signInWithEmailAndPassword(email, password)
                                        .addOnCompleteListener(this, task1 -> {
                                            if (task1.isSuccessful()) {
                                                checkAndSendVerificationEmail();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Error during user registration", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(RegisterActivity.this, "Error during user registration", Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    public void saveUserDataToFirestore(String name, String username, String email, String userId) {
        // Create a new customer
        CustomerModel customer = new CustomerModel();
        customer.setName(name);
        customer.setUsername(username);
        customer.setEmail(email);

        // Add the customer to the "USERS" collection in Firestore
        // Add the customer preferences to the "USER_PREFERENCES" collection in Firestore
        // Create a document for the user in WISHLISTS collection
        CollectionReference usersCollection = firestoreDB.collection("USERS");
        CollectionReference customerPreferencesCollection = firestoreDB.collection("USER_PREFERENCES");
        CollectionReference wishlistsCollection = firestoreDB.collection("WISHLISTS");

        usersCollection.document(userId)
                .set(customer)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(RegisterActivity.this, "User registration successful", Toast.LENGTH_LONG).show();

                    CustomerPreferencesModel customerPreferences = new CustomerPreferencesModel();
                    customerPreferencesCollection.document(userId)
                            .set(customerPreferences)
                            .addOnSuccessListener(documentReference1 -> Log.d("RegisterActivity", "User preferences added to Firestore"))
                            .addOnFailureListener(e -> Log.d("RegisterActivity", "Error adding user preferences to Firestore"));

                    WishlistModel wishlist = new WishlistModel(userId);
                    // Create a document for the user in WISHLISTS collection, but the document ID should be auto generated
                    wishlistsCollection.add(wishlist)
                            .addOnSuccessListener(documentReference2 -> Log.d("RegisterActivity", "Wishlist added to Firestore"))
                            .addOnFailureListener(e -> Log.d("RegisterActivity", "Error adding wishlist to Firestore"));

                    checkAndSendVerificationEmail();
                })
                .addOnFailureListener(e -> Toast.makeText(RegisterActivity.this, "Error adding user to Firestore", Toast.LENGTH_LONG).show());
    }

    public void checkAndSendVerificationEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            user.reload();
            if (!user.isEmailVerified()) {
                user.sendEmailVerification().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Try again later", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "Email already verified", Toast.LENGTH_SHORT).show();
                goToLoginPage();
            }
            user.reload();
        }

        Intent intent = new Intent(RegisterActivity.this, VerifyEmailActivity.class);
        startActivity(intent);
        finish();
    }

    public void goToLoginPage() {
        Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdownNow();
    }

    public void setFirebaseAuth(FirebaseAuth mockAuth) {
        this.firebaseAuth = mockAuth;
    }

    public void setFirestoreDB(FirebaseFirestore mockFirestore) {
        this.firestoreDB = mockFirestore;
    }

    public EditText getEdtConfirmPassword() {
        return edtConfirmPassword;
    }

    public void setEdtConfirmPassword(EditText mockEdtConfirmPassword) {
        this.edtConfirmPassword = mockEdtConfirmPassword;
    }
}
