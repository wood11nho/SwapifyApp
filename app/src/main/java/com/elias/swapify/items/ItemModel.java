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
    private boolean itemIsForCharity;
    private String itemUserId;
    private String itemLocation;
    private String itemCharityId;

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

    public ItemModel(String itemName, String itemDescription, String itemCategory, int itemPrice, String itemImage, boolean itemIsForTrade, boolean itemIsForSale, boolean itemIsForCharity, String itemUserId, String itemLocation, String itemCharityId) {
        this.itemName = itemName;
        this.itemDescription = itemDescription;
        this.itemCategory = itemCategory;
        this.itemPrice = itemPrice;
        this.itemImage = itemImage;
        this.itemIsForTrade = itemIsForTrade;
        this.itemIsForSale = itemIsForSale;
        this.itemIsForCharity = itemIsForCharity;
        this.itemUserId = itemUserId;
        this.itemId = "";
        this.itemLocation = itemLocation;
        this.itemCharityId = itemCharityId;
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

    public boolean getItemIsForCharity() {
        return itemIsForCharity;
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

    public void setItemIsForCharity(boolean itemIsForCharity) {
        this.itemIsForCharity = itemIsForCharity;
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

    public String getItemCharityId() {
        return itemCharityId;
    }

    public void setItemCharityId(String itemCharityId) {
        this.itemCharityId = itemCharityId;
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
                ", itemIsForCharity=" + itemIsForCharity +
                ", itemUserId='" + itemUserId + '\'' +
                ", itemLocation='" + itemLocation + '\'' +
                '}';
    }
}