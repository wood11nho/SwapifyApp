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
    private String passwordHash;
    private String profilePicture;
    private String phoneNumber;
    private String bio;
    private String city;

    public CustomerModel(String name, String username, String email, String passwordHash) {
        this.id = idCounter;
        this.name = name;
        this.username = username;
        this.email = email;
        this.passwordHash = encryptPassword(passwordHash);
        this.profilePicture = "";
        this.phoneNumber = "";
        this.bio = "";
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

    public String getPasswordHash() {
        return passwordHash;
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
                ", passwordHash='" + passwordHash + '\'' +
                ", profilePicture='" + profilePicture + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", bio='" + bio + '\'' +
                ", city='" + city + '\'' +
                '}';
    }
}
