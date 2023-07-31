package com.example.swapify;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CustomerModel {
    private String name;
    private String username;
    private String email;
    private String profilepicture;
    private String phonenumber;
    private String bio;
    private String county;
    private String city;

    public CustomerModel() {
        this.name = "";
        this.username = "";
        this.email = "";
        this.profilepicture = "";
        this.phonenumber = "";
        this.bio = "";
        this.county = "";
        this.city = "";
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getProfilepicture() {
        return profilepicture;
    }

    public String getPhonenumber() {
        return phonenumber;
    }

    public String getBio() {
        return bio;
    }

    public String getCounty() {
        return county;
    }

    public String getCity() {
        return city;
    }

    static String encryptPassword(String password) {
        try{
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return "CustomerModel{" +
                "name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", profilepicture='" + profilepicture + '\'' +
                ", phonenumber='" + phonenumber + '\'' +
                ", bio='" + bio + '\'' +
                ", county='" + county + '\'' +
                ", city='" + city + '\'' +
                '}';
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setProfilepicture(String profilepicture) {
        this.profilepicture = profilepicture;
    }

    public void setPhonenumber(String phonenumber) {
        this.phonenumber = phonenumber;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
