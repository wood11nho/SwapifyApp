package com.example.swapify.categories;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapify.R;
import com.example.swapify.userpreferences.SearchDataManager;
import com.example.swapify.items.SeeAllItemsActivity;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SomeDetailedCategoryAdapter extends RecyclerView.Adapter<SomeDetailedCategoryAdapter.SomeDetailedCategoryViewHolder>{
    private final ArrayList<CategoryModel> categories;
    private final Context context;

    public SomeDetailedCategoryAdapter(Context context, ArrayList<CategoryModel> categories) {
        this.context = context;
        this.categories = new ArrayList<>(categories);
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

        // Set an OnClickListener for going to the category's page
        holder.itemView.setOnClickListener(
                view -> {
                    // Open the SeeAllItemsActivity with extra "category" set to the category name
                    SearchDataManager.getInstance().saveSearch(category.getName());

                    Intent intent = new Intent(view.getContext(), SeeAllItemsActivity.class);
                    intent.putExtra("category", category.getName());
                    view.getContext().startActivity(intent);
                }
        );
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
    public void filterList(ArrayList<CategoryModel> filteredList) {
        categories.clear();
        categories.addAll(filteredList);
        notifyDataSetChanged();
    }
}
