package com.elias.swapify.items;

import androidx.annotation.NonNull;

public class ItemModel {
    private String itemId;
    private String itemName;
    private String itemDescription;
    private String itemCategory;
    private int itemPrice;
    private String itemImage;
    private boolean itemIsForTrade;
    private boolean itemIsForSale;
    private boolean itemIsForAuction;
    private String itemUserId;
    private String itemLocation;

    public ItemModel() {
        // Default constructor required for Firestore deserialization
    }

    // Add the existing constructor that you have in the ItemModel class
    public ItemModel(String name, String image, int price) {
        this.itemName = name;
        this.itemPrice = price;
        this.itemImage = image;
        this.itemId = "";
    }

    public ItemModel(String itemName, String itemDescription, String itemCategory, int itemPrice, String itemImage, boolean itemIsForTrade, boolean itemIsForSale, boolean itemIsForAuction, String itemUserId, String itemLocation) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemCategory = itemCategory;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
        this.itemIsForTrade = itemIsForTrade;
        this.itemIsForSale = itemIsForSale;
        this.itemIsForAuction = itemIsForAuction;
        this.itemUserId = itemUserId;
        this.itemId = "";
        this.itemLocation = itemLocation;
    }

    public String getItemName() {
        return itemName;
    }

    public String getItemDescription() {
        return itemDescription;
    }

    public String getItemCategory() {
        return itemCategory;
    }

    public int getItemPrice() {
        return itemPrice;
    }

    public String getItemImage() {
        return itemImage;
    }

    public boolean getItemIsForTrade() {
        return itemIsForTrade;
    }

    public boolean getItemIsForSale() {
        return itemIsForSale;
    }

    public boolean getItemIsForAuction() {
        return itemIsForAuction;
    }

    public String getItemUserId() {
        return itemUserId;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public void setItemDescription(String itemDescription) {
        this.itemDescription = itemDescription;
    }

    public void setItemCategory(String itemCategory) {
        this.itemCategory = itemCategory;
    }

    public void setItemPrice(int itemPrice) {
        this.itemPrice = itemPrice;
    }

    public void setItemImage(String itemImage) {
        this.itemImage = itemImage;
    }

    public void setItemIsForTrade(boolean itemIsForTrade) {
        this.itemIsForTrade = itemIsForTrade;
    }

    public void setItemIsForSale(boolean itemIsForSale) {
        this.itemIsForSale = itemIsForSale;
    }

    public void setItemIsForAuction(boolean itemIsForAuction) {
        this.itemIsForAuction = itemIsForAuction;
    }

    public void setItemUserId(String itemUserId) {
        this.itemUserId = itemUserId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getItemLocation() {
        return itemLocation;
    }

    public void setItemLocation(String itemLocation) {
        this.itemLocation = itemLocation;
    }

    @NonNull
    @Override
    public String toString() {
        return "ItemModel{" +
                "itemName='" + itemName + '\'' +
                ", itemDescription='" + itemDescription + '\'' +
                ", itemCategory='" + itemCategory + '\'' +
                ", itemPrice=" + itemPrice +
                ", itemImage='" + itemImage + '\'' +
                ", itemIsForTrade=" + itemIsForTrade +
                ", itemIsForSale=" + itemIsForSale +
                ", itemIsForAuction=" + itemIsForAuction +
                ", itemUserId='" + itemUserId + '\'' +
                ", itemLocation='" + itemLocation + '\'' +
                '}';
    }
}