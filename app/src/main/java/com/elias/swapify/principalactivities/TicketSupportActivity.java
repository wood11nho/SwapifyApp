package com.elias.swapify.principalactivities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import androidx.appcompat.app.AppCompatActivity;
import com.elias.swapify.R;
import com.google.firebase.firestore.FirebaseFirestore;

public class TicketSupportActivity extends AppCompatActivity {

    private EditText subjectEditText, descriptionEditText, userEmailEditText, userNameEditText;
    private Spinner issueCategorySpinner, urgencySpinner;
    private Button sendButton;
    private ImageButton backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket_support);

        subjectEditText = findViewById(R.id.subjectEditText);
        descriptionEditText = findViewById(R.id.descriptionEditText);
        userEmailEditText = findViewById(R.id.userEmailEditText);
        userNameEditText = findViewById(R.id.userNameEditText);
        issueCategorySpinner = findViewById(R.id.issueCategorySpinner);
        urgencySpinner = findViewById(R.id.urgencySpinner);
        sendButton = findViewById(R.id.sendButton);
        backButton = findViewById(R.id.btnBack_toolbar_support);

        setupSpinners();

        sendButton.setOnClickListener(view -> {
            String subject = subjectEditText.getText().toString();
            String description = descriptionEditText.getText().toString();
            String userEmail = userEmailEditText.getText().toString();
            String issueCategory = issueCategorySpinner.getSelectedItem().toString();
            String urgency = urgencySpinner.getSelectedItem().toString();
            String name = userNameEditText.getText().toString();
            fetchSupportEmailAndSend(userEmail, subject, description, issueCategory, urgency, name);
        });

        backButton.setOnClickListener(view -> {
            finish();
        });
    }

    private void setupSpinners() {
        ArrayAdapter<CharSequence> categoryAdapter = ArrayAdapter.createFromResource(this,
                R.array.issue_categories, android.R.layout.simple_spinner_item);
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        issueCategorySpinner.setAdapter(categoryAdapter);

        ArrayAdapter<CharSequence> urgencyAdapter = ArrayAdapter.createFromResource(this,
                R.array.urgency_levels, android.R.layout.simple_spinner_item);
        urgencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        urgencySpinner.setAdapter(urgencyAdapter);
    }

    private String buildDescription(String userEmail, String issueCategory, String urgency, String description, String name) {
        // Use StringBuilder for efficient string manipulation
        StringBuilder emailBody = new StringBuilder();

        // Email header
        emailBody.append("Dear Swapify Support Team,\n\n");

        // User's email
        emailBody.append("User Email: ").append(userEmail).append("\n\n");

        // Issue Category
        emailBody.append("Issue Category: ").append(issueCategory).append("\n\n");

        // Urgency
        emailBody.append("Urgency Level: ").append(urgency).append("\n\n");

        // Description
        emailBody.append("Issue Description:\n");
        emailBody.append(description.isEmpty() ? "No description provided." : description).append("\n\n");

        // Add a polite closing
        emailBody.append("I am looking forward to your prompt response. Thank you for your attention to this matter.\n\n");

        // Signature
        emailBody.append("Best regards,\n");
        emailBody.append(name.isEmpty() ? "Swapify User" : name).append("\n");
        emailBody.append("Member of the Swapify Community\n\n");

        // Footer
        emailBody.append("--------------------------------------------------\n");
        emailBody.append("This email was sent from the Swapify mobile application.");

        // Convert StringBuilder to String and return
        return emailBody.toString();
    }


    private void fetchSupportEmailAndSend(String userEmail, String subject, String description, String issueCategory, String urgency, String name) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CONFIG")
                .document("support")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String supportEmail = documentSnapshot.getString("supportEmail");
                        if (supportEmail != null) {
                            sendEmail(userEmail, subject, description, issueCategory, urgency, supportEmail, name);
                        } else {
                            // Handle the case where supportEmail is not found
                        }
                    } else {
                        // Handle the case where the document does not exist
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle the error
                });
    }

    private void sendEmail(String userEmail, String subject, String description, String issueCategory, String urgency, String supportEmail, String name) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{supportEmail});
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, buildDescription(userEmail, issueCategory, urgency, description, name));

        try {
            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
        } catch (android.content.ActivityNotFoundException ex) {
            // Handle the error
        }
    }
}
