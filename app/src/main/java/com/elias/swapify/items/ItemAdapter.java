package com.elias.swapify.items;

import android.content.Intent;
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
import com.elias.swapify.userpreferences.ItemInteractionManager;
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
            Glide.with(holder.itemView.getContext()) // Context can be obtained from the ViewHolder's itemView.
                    .load(itemImage)
                    .fitCenter() // Scales the image to fit the ImageView while maintaining its aspect ratio.
                    .centerCrop() // This will crop the center of the scaled image to fill the ImageView fully.
                    .placeholder(R.drawable.ic_question_mark)
                    .error(R.drawable.ic_question_mark)
                    .into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_question_mark);
        }


        // Set the item's name and price
        holder.itemName.setText(item.getItemName());
        String itemPrice = item.getItemPrice() + " RON";
        holder.itemPrice.setText(itemPrice);

        // Set an OnClickListener for going to the item's full details page
        holder.itemView.setOnClickListener(view -> {
            // Open the item's details page
            SearchDataManager.getInstance().saveSearch(item.getItemCategory());
            ItemInteractionManager.getInstance().saveItemInteraction(item);

            Intent intent = new Intent(view.getContext(), FullDetailItemActivity.class);
            intent.putExtra("itemCategory", item.getItemCategory());
            intent.putExtra("itemName", item.getItemName());
            intent.putExtra("itemDescription", item.getItemDescription());
            intent.putExtra("itemPrice", item.getItemPrice());
            intent.putExtra("itemImage", item.getItemImage());
            intent.putExtra("itemIsForTrade", item.getItemIsForTrade());
            intent.putExtra("itemIsForSale", item.getItemIsForSale());
            intent.putExtra("itemIsForCharity", item.getItemIsForCharity());
            intent.putExtra("itemUserId", item.getItemUserId());
            intent.putExtra("itemId", item.getItemId());
            intent.putExtra("itemLocation", item.getItemLocation());
            intent.putExtra("itemCharityId", item.getItemCharityId());
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
