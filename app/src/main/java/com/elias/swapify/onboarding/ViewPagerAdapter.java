package com.elias.swapify.onboarding;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.elias.swapify.R;

public class ViewPagerAdapter extends PagerAdapter {

    Context context;
    int[] images = {
            R.mipmap.welcome,
            R.mipmap.discover,
            R.mipmap.community,
            R.mipmap.explore
    };

    int[] headings = {
            R.string.onboarding_screen_1_title,
            R.string.onboarding_screen_2_title,
            R.string.onboarding_screen_3_title,
            R.string.onboarding_screen_4_title
    };

    int[] descriptions = {
            R.string.onboarding_screen_1_description,
            R.string.onboarding_screen_2_description,
            R.string.onboarding_screen_3_description,
            R.string.onboarding_screen_4_description
    };

    public ViewPagerAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return headings.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.slider_layout, container, false);

        ImageView slideTitleImage = view.findViewById(R.id.imageview_title);
        TextView slideHeading = view.findViewById(R.id.textview_heading);
        TextView slideDescription = view.findViewById(R.id.textview_description);

        slideTitleImage.setImageResource(images[position]);
        slideHeading.setText(headings[position]);
        slideDescription.setText(descriptions[position]);

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
