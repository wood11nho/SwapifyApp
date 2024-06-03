package com.elias.swapify.categories;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.elias.swapify.items.SeeAllItemsActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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

        holder.categoryName.setText(category);

        // Fetch picture from Firestore Database, CATEGORIES collection
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("CATEGORIES")
                .whereEqualTo("name", category)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String imageUrl = document.getString("categoryImage");
                            if (imageUrl != null) {
                                // Load the image using Glide
                                Glide.with(holder.itemView.getContext())
                                        .load(imageUrl)
                                        .placeholder(R.drawable.ic_category) // Placeholder image
                                        .into(holder.categoryIcon);
                            } else {
                                holder.categoryIcon.setImageResource(R.drawable.ic_category); // Default image
                            }
                            break; // Exit loop after finding the document
                        }
                    } else {
                        Log.d("CategoryAdapter", "No such document or task failed: ", task.getException());
                        holder.categoryIcon.setImageResource(R.drawable.ic_category); // Default image
                    }
                });

        holder.itemView.setOnClickListener(view -> {
            SearchDataManager.getInstance().saveSearch(category);

            Intent intent = new Intent(view.getContext(), SeeAllItemsActivity.class);
            intent.putExtra("category", category);
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        TextView categoryName;
        ImageView categoryIcon;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryName = itemView.findViewById(R.id.categoryName);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
        }
    }
}
