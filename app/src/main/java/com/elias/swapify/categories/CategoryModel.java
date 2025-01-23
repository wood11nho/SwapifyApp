package com.elias.swapify.categories;

import androidx.annotation.NonNull;

public class CategoryModel {
    private String name;
    private String reference;
    private String categoryImage;
    private int numberOfItems;

    public CategoryModel() {
        // Default constructor required for Firestore deserialization
    }

    public CategoryModel(String name, String reference, String categoryImage, int numberOfItems) {
        this.name = name;
        this.reference = reference;
        this.categoryImage = categoryImage;
        this.numberOfItems = numberOfItems;
    }

    public String getName() {
        return name;
    }

    public String getReference() {
        return reference;
    }

    public String getCategoryImage() {
        return categoryImage;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public void setCategoryImage(String categoryImage) {
        this.categoryImage = categoryImage;
    }

    public int getNumberOfItems() {
        return numberOfItems;
    }

    public void incrementNumberOfItems() {
        this.numberOfItems++;
    }

    public void setNumberOfItems(int numberOfItems) {
        this.numberOfItems = numberOfItems;
    }

    @NonNull
    @Override
    public String toString() {
        return "CategoryModel{" +
                "name='" + name + '\'' +
                ", reference='" + reference + '\'' +
                ", categoryImage='" + categoryImage + '\'' +
                ", numberOfItems=" + numberOfItems +
                '}';
    }
}
