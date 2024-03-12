package com.example.swapify.categories;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.swapify.R;
import com.example.swapify.userpreferences.SearchDataManager;
import com.example.swapify.items.SeeAllItemsActivity;

import java.util.ArrayList;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {

    private ArrayList<String> categories;

    public CategoryAdapter(ArrayList<String> categories) {
        this.categories = categories;
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_layout, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        String category = categories.get(position);

        // Set the category name
        holder.categoryName.setText(category);

        // Set an OnClickListener for going to the category's page
        holder.itemView.setOnClickListener(
                view -> {
                    // Open the SeeAllItemsActivity with extra "category" set to the category name
                    SearchDataManager.getInstance().saveSearch(category);

                    Intent intent = new Intent(view.getContext(), SeeAllItemsActivity.class);
                    intent.putExtra("category", category);
                    view.getContext().startActivity(intent);
                }
        );
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
        }
    }

}
