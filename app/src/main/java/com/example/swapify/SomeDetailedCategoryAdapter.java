package com.example.swapify;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SomeDetailedCategoryAdapter extends RecyclerView.Adapter<SomeDetailedCategoryAdapter.SomeDetailedCategoryViewHolder>{
    private final ArrayList<CategoryModel> categories;
    private final Context context;

    public SomeDetailedCategoryAdapter(Context context, ArrayList<CategoryModel> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public SomeDetailedCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.some_details_category_layout, parent, false);
        return new SomeDetailedCategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SomeDetailedCategoryViewHolder holder, int position) {
        CategoryModel category = categories.get(position);

        // Set the category name
        holder.categoryName.setText(category.getName());

        // Set the category image
        String categoryImage = category.getCategoryImage();
        // If the category doesn't have an image the String will look like "" or maybe null
        if (categoryImage != null && !categoryImage.equals("")) {
            Picasso.get()
                    .load(categoryImage)
                    .resize(300, 0)
                    .centerCrop()
                    .placeholder(R.drawable.ic_question_mark)
                    .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                    .into(holder.categoryImage);
        } else {
            holder.categoryImage.setImageResource(R.drawable.ic_question_mark);
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class SomeDetailedCategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryImage;

        public SomeDetailedCategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.someDetailedCategoryName);
            categoryImage = itemView.findViewById(R.id.someDetailedCategoryImage);
        }
    }

}
