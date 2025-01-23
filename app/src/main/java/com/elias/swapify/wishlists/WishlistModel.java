package com.elias.swapify.wishlists;

import java.util.ArrayList;
import java.util.List;

public class WishlistModel {
    private String userId;
    private List<WishlistItem> items;

    public WishlistModel(){
        // Default constructor required for calls to DataSnapshot.getValue(WishlistsModel.class)
    }

    public WishlistModel(String userId){
        this.userId = userId;
        // Also create an empty list of items
        this.items = new ArrayList<WishlistItem>();
    }

    public WishlistModel(String userId, List<WishlistItem> items){
        this.userId = userId;
        this.items = items;
    }

    public String getUserId() {
        return userId;
    }

    public List<WishlistItem> getItems() {
        return items;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setItems(List<WishlistItem> items) {
        this.items = items;
    }

    public List<String> getItemIds() {
        List<String> itemIds = new ArrayList<>();
        for (WishlistItem item : items) {
            itemIds.add(item.getItemId());
        }
        return itemIds;
    }
}

