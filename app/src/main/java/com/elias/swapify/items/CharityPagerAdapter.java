package com.elias.swapify.items;

import android.content.Context;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.viewpager.widget.PagerAdapter;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;

import java.util.ArrayList;

public class CharityPagerAdapter extends PagerAdapter{
    private Context context;

    private ArrayList<CharityModel> charities;
    private int selectedPosition = -1;

    public CharityPagerAdapter(Context context, ArrayList<CharityModel> charities) {
        this.context = context;
        this.charities = charities;
    }

    public void setSelectedPosition(int selectedPosition) {
        this.selectedPosition = selectedPosition;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return charities.size();
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == (LinearLayout) object;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.item_charity_layout, container, false);

        ImageView charityImage = view.findViewById(R.id.charityImage);
        TextView charityName = view.findViewById(R.id.charityName);
        TextView charityDescription = view.findViewById(R.id.charityDescription);

        CharityModel charity = charities.get(position);
        Glide.with(context).load(charity.getCharityImage()).into(charityImage);
        charityName.setText(charity.getCharityName());
        charityDescription.setText(charity.getCharityDescription());

        view.setOnClickListener(v -> {
            setSelectedPosition(position);
            Log.d("CharityPagerAdapter", "Selected position: " + selectedPosition);
            // Make the maxLines of the selected charity description
            if (charityDescription.getMaxLines() == 3) {
                charityDescription.setMaxLines(Integer.MAX_VALUE);
            } else {
                charityDescription.setMaxLines(3);
            }
        });

        container.addView(view);
        return view;
    }

    @Override
    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        container.removeView((LinearLayout) object);
    }
}
