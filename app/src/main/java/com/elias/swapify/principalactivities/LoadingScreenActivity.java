package com.elias.swapify.principalactivities;

import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

import com.elias.swapify.R;

public class LoadingScreenActivity extends AppCompatActivity {

    private static final long DELAY_MILLIS = 1500; // 1.5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_screen);

        new Handler().postDelayed(this::finish, DELAY_MILLIS);
    }

}
