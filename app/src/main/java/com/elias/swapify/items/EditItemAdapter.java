package com.elias.swapify.items;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.elias.swapify.R;
import com.elias.swapify.firebase.FirestoreUtil;
import com.google.firebase.firestore.DocumentSnapshot;

import org.checkerframework.checker.units.qual.A;

import java.util.ArrayList;
import java.util.List;

public class EditItemAdapter extends RecyclerView.Adapter<EditItemAdapter.EditItemViewHolder> {
    private ArrayList<ItemModel> itemsPostedByUser;
    private final Context context;

    public EditItemAdapter(Context context, ArrayList<ItemModel> itemsPostedByUser) {
        this.itemsPostedByUser = new ArrayList<>(itemsPostedByUser);
        this.context = context;
    }

    @NonNull
    @Override
    public EditItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.edit_item_layout, parent, false);
        return new EditItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EditItemViewHolder holder, int position) {
        ItemModel item = itemsPostedByUser.get(position);

        // Set the item image, name, and price
        String itemImage = item.getItemImage();

        if (itemImage != null && !itemImage.equals("")){
            Glide.with(holder.itemView.getContext())
                    .load(itemImage)
                    .fitCenter()
                    .centerCrop()
                    .placeholder(R.drawable.ic_question_mark)
                    .error(R.drawable.ic_question_mark)
                    .into(holder.itemImage);
        } else {
            holder.itemImage.setImageResource(R.drawable.ic_question_mark);
        }

        holder.itemName.setText(item.getItemName());
        String itemPrice = item.getItemPrice() + " RON";
        holder.itemPrice.setText(itemPrice);

        holder.btnEdit.setOnClickListener(view -> {
            EditItemDialogFragment editItemDialog = EditItemDialogFragment.newInstance(item);
            editItemDialog.show(((FragmentActivity) context).getSupportFragmentManager(), "EditItemDialogFragment");
        });

        holder.btnDelete.setOnClickListener(view -> {
            // Show confirmation dialog before deleting
            new AlertDialog.Builder(context)
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Delete the item from Firestore and the RecyclerView
                        FirestoreUtil.deleteItem(item);
                        removeItemAt(position);
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    public ItemModel getItemAtPosition(int position) {
        return itemsPostedByUser.get(position);
    }

    public void removeItemAt(int position) {
        if (position >= 0 && position < itemsPostedByUser.size()) {
            itemsPostedByUser.remove(position);
            notifyItemRemoved(position);
        }
    }

    @Override
    public int getItemCount() {
        return itemsPostedByUser.size();
    }

    public void updateItems(List<DocumentSnapshot> userItems) {
        itemsPostedByUser.clear();
        for (DocumentSnapshot document : userItems) {
            ItemModel item = document.toObject(ItemModel.class);
            if (item != null) {
                item.setItemId(document.getId());
                itemsPostedByUser.add(item);
            }
        }
        notifyDataSetChanged();
    }

    static class EditItemViewHolder extends RecyclerView.ViewHolder {
        ImageView itemImage;
        TextView itemName;
        TextView itemPrice;
        ImageButton btnEdit;
        ImageButton btnDelete;

        public EditItemViewHolder(@NonNull View itemView) {
            super(itemView);
            itemImage = itemView.findViewById(R.id.editItemImage);
            itemName = itemView.findViewById(R.id.editItemName);
            itemPrice = itemView.findViewById(R.id.editItemPrice);
            btnEdit = itemView.findViewById(R.id.editItemButton);
            btnDelete = itemView.findViewById(R.id.deleteItemButton);
        }
    }


}
