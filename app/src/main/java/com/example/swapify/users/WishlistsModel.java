package com.example.swapify.users;

import java.util.Date;
import java.util.List;

public class WishlistsModel {
    private String userId;
    private List<WishlistItem> items;

    public WishlistsModel(){
        // Default constructor required for calls to DataSnapshot.getValue(WishlistsModel.class)
    }

    public WishlistsModel(String userId, List<WishlistItem> items){
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
}

class WishlistItem{
    private Date addedOn;
    private String itemId;

    public WishlistItem(){
        // Default constructor required for calls to DataSnapshot.getValue(WishlistItem.class)
    }

    public WishlistItem(Date addedOn, String itemId){
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
