package com.example.swapify;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class SomeDetailedItemAdapter extends RecyclerView.Adapter<SomeDetailedItemAdapter.SomeDetailedItemViewHolder> {
    private final ArrayList<ItemModel> items;

    public SomeDetailedItemAdapter(ArrayList<ItemModel> items) {
        this.items = items;
    }


    @NonNull
    @Override
    public SomeDetailedItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.some_details_item_layout, parent, false);
        return new SomeDetailedItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SomeDetailedItemViewHolder holder, int position) {
        ItemModel item = items.get(position);

        // Set the item image, name, and price
        holder.itemImage.setImageResource(R.drawable.ic_question_mark);
        holder.itemName.setText(item.getItemName());
        holder.itemPrice.setText(String.valueOf(item.getItemPrice()));
        holder.itemDescription.setText(item.getItemDescription());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SomeDetailedItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;
        TextView itemDescription;

        public SomeDetailedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.someDetailedItemImage);
            itemName = itemView.findViewById(R.id.someDetailedItemName);
            itemPrice = itemView.findViewById(R.id.someDetailedItemPrice);
            itemDescription = itemView.findViewById(R.id.someDetailedItemDescription);
        }
    }
}
