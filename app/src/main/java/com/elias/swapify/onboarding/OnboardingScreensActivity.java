package com.elias.swapify.onboarding;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.elias.swapify.R;
import com.elias.swapify.principalactivities.EntryActivity;

public class OnboardingScreensActivity extends AppCompatActivity{
    ViewPager SlideViewPager;
    LinearLayout DotsLayout;
    Button backBtn, nextBtn, skipBtn;
    ImageView nextImage, backImage;

    TextView[] dots;
    ViewPagerAdapter viewPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding_screens);

        SlideViewPager = findViewById(R.id.viewpager_slider);
        DotsLayout = findViewById(R.id.ll_dots);

        backBtn = findViewById(R.id.btn_back);
        nextBtn = findViewById(R.id.btn_next);
        skipBtn = findViewById(R.id.btn_skip);
        nextImage = findViewById(R.id.imageview_next);
        backImage = findViewById(R.id.imageview_back);

        backBtn.setOnClickListener(view -> {
            SlideViewPager.setCurrentItem(getItem(-1), true);
        });

        nextBtn.setOnClickListener(view -> {
            SlideViewPager.setCurrentItem(getItem(+1), true);
        });

        skipBtn.setOnClickListener(view -> {
            Intent intent = new Intent(OnboardingScreensActivity.this, EntryActivity.class);
            startActivity(intent);
            finish();
        });

        viewPagerAdapter = new ViewPagerAdapter(this);
        SlideViewPager.setAdapter(viewPagerAdapter);

        setUpDots(0);
        backBtn.setVisibility(View.INVISIBLE);
        backImage.setVisibility(View.INVISIBLE);
        SlideViewPager.addOnPageChangeListener(viewListener);

    }

    public void setUpDots(int position) {
        dots = new TextView[viewPagerAdapter.getCount()];
        DotsLayout.removeAllViews();

        for (int i = 0; i < dots.length; i++) {
            dots[i] = new TextView(this);
            dots[i].setText(Html.fromHtml("&#8226;"));
            dots[i].setTextSize(35);
            // Set color from ?attr/colorPrimary from theme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dots[i].setTextColor(getResources().getColor(R.color.translucent_grey, getApplicationContext().getTheme()));
            }
            DotsLayout.addView(dots[i]);
        }

        if (dots.length > 0) {
            // Set color from ?attr/colorPrimary from theme
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dots[position].setTextColor(getResources().getColor(R.color.white, getApplicationContext().getTheme()));
            }
        }
    }

    ViewPager.OnPageChangeListener viewListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            setUpDots(position);
            if (position > 0){
                backBtn.setVisibility(View.VISIBLE);
                backImage.setVisibility(View.VISIBLE);
            } else {
                backBtn.setVisibility(View.INVISIBLE);
                backImage.setVisibility(View.INVISIBLE);
            }

            if (position == dots.length - 1){
                nextBtn.setVisibility(View.INVISIBLE);
                nextImage.setVisibility(View.INVISIBLE);
            } else {
                nextBtn.setVisibility(View.VISIBLE);
                nextImage.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    private int getItem(int i) {
        return SlideViewPager.getCurrentItem() + i;
    }
}
