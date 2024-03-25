package com.elias.swapify.users;

import java.util.Date;

public class WishlistItem {
    private Date addedOn;
    private String itemId;

    public WishlistItem() {
        // Default constructor required for calls to DataSnapshot.getValue(WishlistItem.class)
    }

    public WishlistItem(Date addedOn, String itemId) {
        this.addedOn = addedOn;
        this.itemId = itemId;
    }

    public Date getAddedOn() {
        return addedOn;
    }

    public String getItemId() {
        return itemId;
    }

    public void setAddedOn(Date addedOn) {
        this.addedOn = addedOn;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

}
