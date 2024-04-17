package com.elias.swapify.charity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.elias.swapify.firebase.FirestoreUtil;

public class FullDetailCharityActivity extends AppCompatActivity {
    private ImageButton backBtn;
    private ImageView charityImage;
    private TextView charityName, charityDescription;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_detail_charity);

        backBtn = findViewById(R.id.back_button_full_detail_charity);
        charityImage = findViewById(R.id.charityImage);
        charityName = findViewById(R.id.charityName);
        charityDescription = findViewById(R.id.charityDescription);

        backBtn.setOnClickListener(v -> finish());

        String charityId = getIntent().getStringExtra("charityId");

        FirestoreUtil.fetchCharityData(charityId, new FirestoreUtil.OnCharityDataFetchedListener() {
            @Override
            public void onCharityDataFetched(CharityModel charity) {
                Glide.with(FullDetailCharityActivity.this)
                        .load(charity.getCharityImage())
                        .fitCenter()
                        .centerCrop() // Ensures the image is centered and cropped, matching the ImageView's scaleType
                        .placeholder(R.drawable.ic_question_mark) // Display a placeholder before the image loads
                        .error(R.drawable.ic_question_mark) // Display an error image if the URL fails to load
                        .into(charityImage);
                charityName.setText(charity.getCharityName());
                charityDescription.setText(charity.getCharityDescription());
            }

            @Override
            public void onError(String error) {
                Toast.makeText(FullDetailCharityActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}