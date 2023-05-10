package com.example.swapify;

import static com.example.swapify.CustomerModel.encryptPassword;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBObject extends SQLiteOpenHelper {

    public static final String USERS_TABLE = "USERS";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_USERNAME = "USERNAME";
    public static final String COLUMN_EMAIL = "EMAIL";
    public static final String COLUMN_PASSWORD = "PASSWORD";
    public static final String COLUMN_PROFILE_PICTURE = "PROFILE_PICTURE";
    public static final String COLUMN_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String COLUMN_BIO = "BIO";
    public static final String COLUMN_COUNTY = "COUNTY";
    public static final String COLUMN_CITY = "CITY";

    public DBObject(@Nullable Context context) {
        super(context, "swapify.db", null, 3);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + USERS_TABLE + "(ID INTEGER PRIMARY KEY, " + COLUMN_NAME + " TEXT, " + COLUMN_USERNAME + " TEXT, " + COLUMN_EMAIL + " TEXT, " + COLUMN_PASSWORD + " TEXT)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if(oldVersion < 3){

            String alterTableStatement = "ALTER TABLE " + USERS_TABLE + " ADD COLUMN " + COLUMN_COUNTY + " TEXT";
            db.execSQL(alterTableStatement);
        }
    }

    public void deleteAll() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(USERS_TABLE, null, null);
        db.close();
    }

    public boolean addOne(CustomerModel customerModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, customerModel.getName());
        cv.put(COLUMN_USERNAME, customerModel.getUsername());
        cv.put(COLUMN_EMAIL, customerModel.getEmail());
        cv.put(COLUMN_PASSWORD, customerModel.getPasswordHash());
        cv.put(COLUMN_PROFILE_PICTURE, customerModel.getProfilePicture());
        cv.put(COLUMN_PHONE_NUMBER, customerModel.getPhoneNumber());
        cv.put(COLUMN_BIO, customerModel.getBio());
        cv.put(COLUMN_COUNTY, customerModel.getCounty());
        cv.put(COLUMN_CITY, customerModel.getCity());

        db.beginTransaction();
        try{
            long insert = db.insert(USERS_TABLE, null, cv);
            if (insert != -1) {
                db.setTransactionSuccessful();
                return true;
            }
        }finally {
            db.endTransaction();
        }
        return false;
    }

    public boolean authenticate(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int hashedPasswordColumnIndex = cursor.getColumnIndex(COLUMN_PASSWORD);
            if (hashedPasswordColumnIndex >= 0) {
                String hashedPassword = cursor.getString(hashedPasswordColumnIndex);
                String hashedInputPassword = encryptPassword(password);
                if (hashedPassword.equals(hashedInputPassword)) {
                    cursor.close();
                    db.close();
                    return true;
                }
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return false;
    }


    public String getUsername(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int usernameColumnIndex = cursor.getColumnIndex(COLUMN_USERNAME);
            if (usernameColumnIndex >= 0) {
                String username = cursor.getString(usernameColumnIndex);
                cursor.close();
                db.close();
                return username;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return "";
    }

    public boolean updateProfilePicture(String email, String profilePicture) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_PROFILE_PICTURE, profilePicture);

        db.beginTransaction();
        try{
            long update = db.update(USERS_TABLE, cv, COLUMN_EMAIL + " = ?", new String[]{email});
            if (update != -1) {
                db.setTransactionSuccessful();
                return true;
            }
        }finally {
            db.endTransaction();
        }
        return false;
    }

    public String getName(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(COLUMN_NAME);
            if (nameColumnIndex >= 0) {
                String name = cursor.getString(nameColumnIndex);
                cursor.close();
                db.close();
                return name;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return "";
    }

    public String getPhone(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int phoneColumnIndex = cursor.getColumnIndex(COLUMN_PHONE_NUMBER);
            if (phoneColumnIndex >= 0) {
                String phone = cursor.getString(phoneColumnIndex);
                cursor.close();
                db.close();
                return phone;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return "";
    }

    public String getCounty(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int countyColumnIndex = cursor.getColumnIndex(COLUMN_COUNTY);
            if (countyColumnIndex >= 0) {
                String county = cursor.getString(countyColumnIndex);
                cursor.close();
                db.close();
                return county;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return "";
    }

    public String getCity(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int countryColumnIndex = cursor.getColumnIndex(COLUMN_CITY);
            if (countryColumnIndex >= 0) {
                String city = cursor.getString(countryColumnIndex);
                cursor.close();
                db.close();
                return city;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return "";
    }

    public String getBio(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int bioColumnIndex = cursor.getColumnIndex(COLUMN_BIO);
            if (bioColumnIndex >= 0) {
                String bio = cursor.getString(bioColumnIndex);
                cursor.close();
                db.close();
                return bio;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return "";
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            db.close();
            return true;
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return false;
    }
}
