package com.elias.swapify.items;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.chats.ChatActivity;
import com.elias.swapify.R;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.elias.swapify.userpreferences.SearchDataManager;
import com.elias.swapify.userpreferences.ItemInteractionManager;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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
        String itemPrice = item.getItemPrice() + " RON";
        holder.itemPrice.setText(itemPrice);

        // If the item is already in wishlist, change the icon to @drawable/ic_remove_from_wishlist
        FirestoreUtil.isItemInWishlist(
                FirebaseUtil.getCurrentUserId(),
                item.getItemId(),
                new FirestoreUtil.WishlistCheckCallback() {
                    @Override
                    public void onItemInWishlist(boolean isInWishlist) {
                        if (isInWishlist) {
                            holder.btnAddToWishlist.setImageResource(R.drawable.ic_remove_from_wishlist);
                        } else {
                            holder.btnAddToWishlist.setImageResource(R.drawable.ic_add_to_wishlist);
                        }
                    }
                }
        );

        // Set an OnClickListener for going to the item's details page
        holder.itemView.setOnClickListener(view -> {
            // Open the item's details page
            SearchDataManager.getInstance().saveSearch(item.getItemCategory());
            ItemInteractionManager.getInstance().saveItemInteraction(item);

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
            intent.putExtra("itemId", item.getItemId());
            context.startActivity(intent);
        });

        // Set an OnClickListener for going to chat with the user
        holder.btnSendMsg.setOnClickListener(view -> {
            // Open a chat with the user associated with this item
            openChatWithUser(item.getItemUserId());
        });

        holder.btnAddToWishlist.setOnClickListener(view -> {
            // Add the item to the user's wishlist or remove it if it's already there
            FirestoreUtil.isItemInWishlist(
                    FirebaseUtil.getCurrentUserId(),
                    item.getItemId(),
                    new FirestoreUtil.WishlistCheckCallback() {
                        @Override
                        public void onItemInWishlist(boolean isInWishlist) {
                            if (isInWishlist) {
                                removeFromWishlist(item.getItemId());
                                holder.btnAddToWishlist.setImageResource(R.drawable.ic_add_to_wishlist);
                            } else {
                                addToWishlist(item.getItemId());
                                holder.btnAddToWishlist.setImageResource(R.drawable.ic_remove_from_wishlist);
                            }
                        }
                    }
            );
        });
    }

    public ItemModel getItemAtPosition(int position) {
        return items.get(position);
    }

    // Method to remove an item at a certain position
    public void removeItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
        }
    }

    // Method to add an item to the adapter (for updating UI after adding to wishlist)
    public void addItem(ItemModel item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class SomeDetailedItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;
        ImageButton btnSendMsg;
        ImageButton btnAddToWishlist;

        public SomeDetailedItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.someDetailedItemImage);
            itemName = itemView.findViewById(R.id.someDetailedItemName);
            itemPrice = itemView.findViewById(R.id.someDetailedItemPrice);
            btnSendMsg = itemView.findViewById(R.id.sendMessageButton);
            btnAddToWishlist = itemView.findViewById(R.id.addToWishlistButton);
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

    private void addToWishlist(String itemId) {
        // Add the item to the user's wishlist
        FirestoreUtil.addItemToWishlist(
                FirebaseUtil.getCurrentUserId(),
                itemId,
                new Date(), // Current date as the addedOn date
                new FirestoreUtil.WishlistUpdateCallback() {
                    @Override
                    public void onWishlistUpdated() {
                        // Handle the successful update, maybe show a message to the user
                        Toast.makeText(context, "Item added to wishlist!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onWishlistUpdateFailed(Exception e) {
                        // Handle the error, maybe show an error message to the user
                        Toast.makeText(context, "Failed to add item to wishlist.", Toast.LENGTH_SHORT).show();
                        Log.e("Wishlist", "Error adding item to wishlist", e);
                    }
                }
        );
    }

    private void removeFromWishlist(String itemId) {
        // Remove the item from the user's wishlist
        FirestoreUtil.removeItemFromWishlist(
                FirebaseUtil.getCurrentUserId(),
                itemId,
                new FirestoreUtil.WishlistUpdateCallback() {
                    @Override
                    public void onWishlistUpdated() {
                        // Handle the successful update, maybe show a message to the user
                        Toast.makeText(context, "Item removed from wishlist!", Toast.LENGTH_SHORT).show();

                        // Update the UI to reflect the change
                        notifyDataSetChanged();
                    }

                    @Override
                    public void onWishlistUpdateFailed(Exception e) {
                        // Handle the error, maybe show an error message to the user
                        Toast.makeText(context, "Failed to remove item from wishlist.", Toast.LENGTH_SHORT).show();
                        Log.e("Wishlist", "Error removing item from wishlist", e);
                    }
                }
        );
    }

    public void filterList(ArrayList<ItemModel> filteredList) {
        items.clear();
        items.addAll(filteredList);
        notifyDataSetChanged();
    }

    public void updateItems(List<ItemModel> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
    }

}
