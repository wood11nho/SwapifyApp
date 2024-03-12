package com.elias.swapify.userpreferences;

import java.util.ArrayList;
import java.util.List;

public class CustomerPreferencesModel {
    private List<String> itemInteractions;
    private List<String> postedItems;
    private List<String> searchHistory;

    public CustomerPreferencesModel() {
        // Empty constructor needed for Firestore serialization
        itemInteractions = new ArrayList<>();
        postedItems = new ArrayList<>();
        searchHistory = new ArrayList<>();
    }

    public CustomerPreferencesModel(List<String> itemInteractions, List<String> postedItems, List<String> searchHistory) {
        this.itemInteractions = itemInteractions;
        this.postedItems = postedItems;
        this.searchHistory = searchHistory;
    }

    public List<String> getItemInteractions() {
        return itemInteractions;
    }

    public void setItemInteractions(List<String> itemInteractions) {
        this.itemInteractions = itemInteractions;
    }

    public List<String> getPostedItems() {
        return postedItems;
    }

    public void setPostedItems(List<String> postedItems) {
        this.postedItems = postedItems;
    }

    public List<String> getSearchHistory() {
        return searchHistory;
    }

    public void setSearchHistory(List<String> searchHistory) {
        this.searchHistory = searchHistory;
    }

    @Override
    public String toString() {
        return "CustomerPreferencesModel{" +
                "itemInteractions=" + itemInteractions +
                ", postedItems=" + postedItems +
                ", searchHistory=" + searchHistory +
                '}';
    }
}
