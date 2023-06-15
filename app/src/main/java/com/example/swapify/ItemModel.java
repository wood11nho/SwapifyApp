package com.example.swapify;

public class ItemModel {
    private static int idCounter = 1;
    private int id;
    private String item_name;
    private String item_description;
    private String item_category;
    private int item_price;
    private String item_image;
    private int item_is_for_trade;
    private int item_is_for_sale;
    private int item_is_for_auction;
    private int item_user_id;

    public ItemModel(String item_name, String item_description, String item_category, int item_price, int item_is_for_trade, int item_is_for_sale, int item_is_for_auction, int item_user_id) {
        this.id = idCounter;
        this.item_name = item_name;
        this.item_description = item_description;
        this.item_category = item_category;
        this.item_price = item_price;
        this.item_is_for_trade = item_is_for_trade;
        this.item_is_for_sale = item_is_for_sale;
        this.item_is_for_auction = item_is_for_auction;
        this.item_user_id = item_user_id;
        idCounter++;
    }

    public String getItemName() {
        return item_name;
    }

    public String getItemDescription() {
        return item_description;
    }

    public String getItemCategory() {
        return item_category;
    }

    public int getItemPrice() {
        return item_price;
    }

    public String getItemImage() {
        return item_image;
    }

    public int getItemIsForTrade() {
        return item_is_for_trade;
    }

    public int getItemIsForSale() {
        return item_is_for_sale;
    }

    public int getItemIsForAuction() {
        return item_is_for_auction;
    }

    public int getItemUserId() {
        return item_user_id;
    }

    public void setItemName(String item_name) {
        this.item_name = item_name;
    }

    public void setItemDescription(String item_description) {
        this.item_description = item_description;
    }

    public void setItemCategory(String item_category) {
        this.item_category = item_category;
    }

    public void setItemPrice(int item_price) {
        this.item_price = item_price;
    }

    public void setItemImage(String item_image) {
        this.item_image = item_image;
    }

    public void setItemIsForTrade(int item_is_for_trade) {
        this.item_is_for_trade = item_is_for_trade;
    }

    public void setItemIsForSale(int item_is_for_sale) {
        this.item_is_for_sale = item_is_for_sale;
    }

    public void setItemIsForAuction(int item_is_for_auction) {
        this.item_is_for_auction = item_is_for_auction;
    }

    public void setItemUserId(int item_user_id) {
        this.item_user_id = item_user_id;
    }
}
