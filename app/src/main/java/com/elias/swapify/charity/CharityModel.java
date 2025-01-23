package com.elias.swapify.charity;

public class CharityModel {
    private String charityId;
    private String charityName;
    private String charityDescription;
    private String charityImage;

    public CharityModel(String charityId, String charityName, String charityDescription, String charityImage) {
        this.charityId = charityId;
        this.charityName = charityName;
        this.charityDescription = charityDescription;
        this.charityImage = charityImage;
    }

    public CharityModel() {
    }

    public String getCharityId() {
        return charityId;
    }

    public void setCharityId(String charityId) {
        this.charityId = charityId;
    }

    public String getCharityName() {
        return charityName;
    }

    public void setCharityName(String charityName) {
        this.charityName = charityName;
    }

    public String getCharityDescription() {
        return charityDescription;
    }

    public void setCharityDescription(String charityDescription) {
        this.charityDescription = charityDescription;
    }

    public String getCharityImage() {
        return charityImage;
    }

    public void setCharityImage(String charityImage) {
        this.charityImage = charityImage;
    }
}
