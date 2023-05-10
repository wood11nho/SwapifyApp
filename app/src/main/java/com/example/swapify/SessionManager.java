package com.example.swapify;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_NAME = "name";
    private static final String KEY_BIO = "bio";
    private static final String KEY_PROFILE_PIC = "profile_pic";
    private static final String KEY_COUNTY = "county";
    private static final String KEY_CITY = "city";
    private static final String KEY_PHONE_NUMBER = "phone_number";

    private final SharedPreferences sharedPreferences;
    private final SharedPreferences.Editor editor;

    public SessionManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_EMAIL, "");
    }

    public boolean isLoggedIn() {
        return !getUserEmail().isEmpty();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, "");
    }

    public String getName() {
        return sharedPreferences.getString(KEY_NAME, "");
    }

    public String getBio() {
        return sharedPreferences.getString(KEY_BIO, "");
    }

    public String getProfilePic() {
        return sharedPreferences.getString(KEY_PROFILE_PIC, "");
    }

    public String getCounty() {
        return sharedPreferences.getString(KEY_COUNTY, "");
    }
    public String getCity() {
        return sharedPreferences.getString(KEY_CITY, "");
    }

    public String getPhoneNumber() {
        return sharedPreferences.getString(KEY_PHONE_NUMBER, "");
    }

    public void clearSession() {
        editor.clear();
        editor.apply();
    }
}
