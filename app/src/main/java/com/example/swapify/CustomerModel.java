package com.example.swapify;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class CustomerModel {
    private static int idCounter = 1;
    private int id;
    private String name;
    private String username;
    private String email;
    private String profilePicture;
    private String phoneNumber;
    private String bio;
    private String county;
    private String city;

    public CustomerModel(String name, String username, String email) {
        this.id = idCounter;
        this.name = name;
        this.username = username;
        this.email = email;
        this.profilePicture = "";
        this.phoneNumber = "";
        this.bio = "";
        this.county = "";
        this.city = "";
        idCounter++;
    }

    public CustomerModel() {
        this.id = idCounter;
        this.name = "";
        this.username = "";
        this.email = "";
        this.profilePicture = "";
        this.phoneNumber = "";
        this.bio = "";
        this.county = "";
        this.city = "";
        idCounter++;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public String getPhoneNumber() {
        return phoneNumber;
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
                "id=" + id +
                ", name='" + name + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
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

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
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

    public void setId(int id) {
        this.id = id;
    }

    public static void setIdCounter(int idCounter) {
        CustomerModel.idCounter = idCounter;
    }

    public int getId() {
        return id;
    }
}
