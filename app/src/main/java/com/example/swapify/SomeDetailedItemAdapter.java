package com.example.swapify;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class SomeDetailedItemAdapter extends RecyclerView.Adapter<SomeDetailedItemAdapter.SomeDetailedItemViewHolder> {
    private final ArrayList<ItemModel> items;
    private final Context context;

    public SomeDetailedItemAdapter(Context context, ArrayList<ItemModel> items) {
        this.context = context;
        this.items = new ArrayList<>(items);
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
        String itemImage = item.getItemImage();

        // If the item doesn't have an image the String will look like "" or maybe null
        if (itemImage != null && !itemImage.equals("")) {
            Picasso.get().load(itemImage).into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_question_mark);
        }

        holder.itemName.setText(item.getItemName());
        holder.itemPrice.setText(String.valueOf(item.getItemPrice()));
        holder.itemDescription.setText(item.getItemDescription());

        // Set an OnClickListener for going to the item's details page
        holder.itemView.setOnClickListener(view -> {
            // Open the item's details page
            SearchDataManager.getInstance().saveSearch(item.getItemCategory());
            ItemInteractionManager.getInstance().saveItemInteraction(item.getItemName());
            ItemInteractionManager.getInstance().saveItemInteraction(item.getItemDescription());

            Intent intent = new Intent(context, FullDetailItemActivity.class);
            intent.putExtra("itemCategory", item.getItemCategory());
            intent.putExtra("itemName", item.getItemName());
            intent.putExtra("itemDescription", item.getItemDescription());
            intent.putExtra("itemPrice", item.getItemPrice());
            intent.putExtra("itemImage", item.getItemImage());
            intent.putExtra("itemIsForTrade", item.getItemIsForTrade());
            intent.putExtra("itemIsForSale", item.getItemIsForSale());
            intent.putExtra("itemIsForAuction", item.getItemIsForAuction());
            intent.putExtra("itemUserId", item.getItemUserId());
            context.startActivity(intent);
        });

        // Set an OnClickListener for going to chat with the user
        holder.btnSendMsg.setOnClickListener(view -> {
            // Open a chat with the user associated with this item
            openChatWithUser(item.getItemUserId());
        });
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
        ImageButton btnSendMsg;

        public SomeDetailedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.someDetailedItemImage);
            itemName = itemView.findViewById(R.id.someDetailedItemName);
            itemPrice = itemView.findViewById(R.id.someDetailedItemPrice);
            itemDescription = itemView.findViewById(R.id.someDetailedItemDescription);
            btnSendMsg = itemView.findViewById(R.id.sendMessageButton);
        }
    }

    private void openChatWithUser(String userId) {
        // Create an Intent to open the chat activity
        Intent chatIntent = new Intent(context, ChatActivity.class);

        // Pass the user's unique identifier to the chat activity
        chatIntent.putExtra("userId", userId);

        // Start the chat activity
        context.startActivity(chatIntent);
    }

    public void filterList(ArrayList<ItemModel> filteredList) {
        items.clear();
        items.addAll(filteredList);
        notifyDataSetChanged();
    }

}
