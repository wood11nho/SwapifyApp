package com.elias.swapify.users;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.elias.swapify.R;
import com.elias.swapify.firebase.FirebaseUtil;
import com.elias.swapify.firebase.FirestoreUtil;
import com.elias.swapify.items.ItemModel;
import com.elias.swapify.items.SomeDetailedItemAdapter;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {
    ImageButton btnBack;
    RecyclerView wishlistRecyclerView;
    SomeDetailedItemAdapter itemAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        btnBack = findViewById(R.id.btnBack_wishlist);
        wishlistRecyclerView = findViewById(R.id.wishlistRecyclerView);
        wishlistRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new SwipeToDeleteCallback());
        itemTouchHelper.attachToRecyclerView(wishlistRecyclerView);

        itemAdapter = new SomeDetailedItemAdapter(this, new ArrayList<>());
        wishlistRecyclerView.setAdapter(itemAdapter);

        btnBack.setOnClickListener(v -> finish());

        fetchWishlist();
    }

    class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {
        private final Drawable icon;
        private final ColorDrawable background;
        // We don't want to support drag & drop, so we pass 0 for drag directions
        SwipeToDeleteCallback() {
            super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
            icon = ContextCompat.getDrawable(WishlistActivity.this, R.drawable.ic_delete);
            background = new ColorDrawable(Color.RED);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            deleteItemFromWishlist(position);
        }

        @Override
        public void onChildDraw(Canvas c, RecyclerView recyclerView,
                                RecyclerView.ViewHolder viewHolder, float dX, float dY,
                                int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            View itemView = viewHolder.itemView;
            int backgroundCornerOffset = 20; // Adjust this to your liking

            int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
            int iconBottom = iconTop + icon.getIntrinsicHeight();

            if (dX > 0) { // Swiping to the right
                int iconLeft = itemView.getLeft() + iconMargin;
                int iconRight = iconLeft + icon.getIntrinsicWidth();
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getLeft(), itemView.getTop(),
                        itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
            } else if (dX < 0) { // Swiping to the left
                int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
                int iconRight = itemView.getRight() - iconMargin;
                icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

                background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                        itemView.getTop(), itemView.getRight(), itemView.getBottom());
            } else { // view is unSwiped
                background.setBounds(0, 0, 0, 0);
            }

            background.draw(c);
            icon.draw(c);
        }

    }

    private void fetchWishlist() {
        FirestoreUtil.fetchUserWishlistItems(FirebaseUtil.getCurrentUserId(), new FirestoreUtil.OnWishlistItemsFetchedListener() {
            @Override
            public void onWishlistItemsFetched(List<ItemModel> wishlistItems) {
                itemAdapter.updateItems(wishlistItems);
            }

            @Override
            public void onError(String error) {
                // Handle the error, e.g., show a toast or log it
            }
        });
    }

    private void deleteItemFromWishlist(int position) {
        // Get the item to delete by its position
        ItemModel itemToRemove = itemAdapter.getItemAtPosition(position);

        // Call FirestoreUtil to remove the item from the wishlist in Firestore
        FirestoreUtil.removeItemFromWishlist(FirebaseUtil.getCurrentUserId(), itemToRemove.getItemId(), new FirestoreUtil.WishlistUpdateCallback() {
            @Override
            public void onWishlistUpdated() {
                // Remove the item from the adapter and notify it
                itemAdapter.removeItemAt(position);
            }

            @Override
            public void onWishlistUpdateFailed(Exception e) {
                // Handle failure: show a message, log the error, etc.
            }
        });
    }
}
