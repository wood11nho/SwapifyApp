package com.example.swapify;

import androidx.annotation.NonNull;

public class CategoryModel {
    private String name;
    private String reference;
    private String categoryImage;

    public CategoryModel() {
        // Default constructor required for Firestore deserialization
    }

    public CategoryModel(String name, String reference, String categoryImage) {
        this.name = name;
        this.reference = reference;
        this.categoryImage = categoryImage;
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

    @NonNull
    @Override
    public String toString() {
        return "CategoryModel{" +
                "name='" + name + '\'' +
                ", reference='" + reference + '\'' +
                ", categoryImage='" + categoryImage + '\'' +
                '}';
    }
}
