package com.example.swapify;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    private final ArrayList<ItemModel> items;

    public ItemAdapter(ArrayList<ItemModel> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_layout, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        ItemModel item = items.get(position);

        // Set the item's image
        String itemImage = item.getItemImage();
        // If the item doesn't have an image the String will look like "" or maybe null
        if (itemImage != null && !itemImage.equals("")) {
            Picasso.get().load(itemImage).into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_question_mark);
        }

        // Set the item's name and price
        holder.itemName.setText(item.getItemName());
        holder.itemPrice.setText(String.valueOf(item.getItemPrice()));

        // Set an OnClickListener for going to the item's full details page
        holder.itemView.setOnClickListener(view -> {
            // Open the item's details page
            Intent intent = new Intent(view.getContext(), FullDetailItemActivity.class);
            intent.putExtra("itemCategory", item.getItemCategory());
            intent.putExtra("itemName", item.getItemName());
            intent.putExtra("itemDescription", item.getItemDescription());
            intent.putExtra("itemPrice", item.getItemPrice());
            intent.putExtra("itemImage", item.getItemImage());
            intent.putExtra("itemIsForTrade", item.getItemIsForTrade());
            intent.putExtra("itemIsForSale", item.getItemIsForSale());
            intent.putExtra("itemIsForAuction", item.getItemIsForAuction());
            intent.putExtra("itemUserId", item.getItemUserId());
            view.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.itemImage);
            itemName = itemView.findViewById(R.id.itemName);
            itemPrice = itemView.findViewById(R.id.itemPrice);
        }
    }
}
