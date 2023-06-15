package com.example.swapify;

import static com.example.swapify.CustomerModel.encryptPassword;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collection;

public class DBObject extends SQLiteOpenHelper {
    // constants for users table
    public static final String USERS_TABLE = "USERS";
    public static final String COLUMN_ID = "ID";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_USERNAME = "USERNAME";
    public static final String COLUMN_EMAIL = "EMAIL";
    public static final String COLUMN_PASSWORD = "PASSWORD";
    public static final String COLUMN_PROFILE_PICTURE = "PROFILE_PICTURE";
    public static final String COLUMN_PHONE_NUMBER = "PHONE_NUMBER";
    public static final String COLUMN_BIO = "BIO";
    public static final String COLUMN_COUNTY = "COUNTY";
    public static final String COLUMN_CITY = "CITY";

    // constants for items table
    public static final String ITEMS_TABLE = "ITEMS";
    public static final String COLUMN_ITEM_ID = "ID";
    public static final String COLUMN_ITEM_NAME = "ITEM_NAME";
    public static final String COLUMN_ITEM_DESCRIPTION = "ITEM_DESCRIPTION";
    public static final String COLUMN_ITEM_CATEGORY = "ITEM_CATEGORY";
    public static final String COLUMN_ITEM_IS_FOR_TRADE = "ITEM_IS_FOR_TRADE";
    public static final String COLUMN_ITEM_IS_FOR_SALE = "ITEM_IS_FOR_SALE";
    public static final String COLUMN_ITEM_IS_FOR_AUCTION = "ITEM_IS_FOR_AUCTION";
    public static final String COLUMN_ITEM_USER_ID = "ITEM_USER_ID";
    public static final String COLUMN_ITEM_IMAGE = "ITEM_IMAGE";
    public static final String COLUMN_ITEM_PRICE = "ITEM_PRICE";

    public DBObject(@Nullable Context context) {
        super(context, "swapify.db", null, 4);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + USERS_TABLE + "(ID INTEGER PRIMARY KEY, " + COLUMN_NAME + " TEXT, " + COLUMN_USERNAME + " TEXT, " + COLUMN_EMAIL + " TEXT, " + COLUMN_PASSWORD + " TEXT)";

        db.execSQL(createTableStatement);

        String createItemsTableStatement = "CREATE TABLE " + ITEMS_TABLE + "(ID INTEGER PRIMARY KEY, " + COLUMN_ITEM_NAME + " TEXT, " + COLUMN_ITEM_DESCRIPTION + " TEXT, " + COLUMN_ITEM_CATEGORY + " TEXT, " + COLUMN_ITEM_IS_FOR_TRADE + " INTEGER, " + COLUMN_ITEM_IS_FOR_SALE + " INTEGER, " + COLUMN_ITEM_IS_FOR_AUCTION + " INTEGER, " + COLUMN_ITEM_USER_ID + " INTEGER, " + COLUMN_ITEM_IMAGE + " TEXT, " + COLUMN_ITEM_PRICE + " INTEGER)";

        db.execSQL(createItemsTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

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

    public int getId(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + USERS_TABLE + " WHERE " + COLUMN_EMAIL + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{email});

        if (cursor != null && cursor.moveToFirst()) {
            int idColumnIndex = cursor.getColumnIndex(COLUMN_ID);
            if (idColumnIndex >= 0) {
                int id = cursor.getInt(idColumnIndex);
                cursor.close();
                db.close();
                return id;
            }
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return -1;
    }

    public void addItem(ItemModel itemModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ITEM_NAME, itemModel.getItemName());
        cv.put(COLUMN_ITEM_DESCRIPTION, itemModel.getItemDescription());
        cv.put(COLUMN_ITEM_PRICE, itemModel.getItemPrice());
        cv.put(COLUMN_ITEM_CATEGORY, itemModel.getItemCategory());
        cv.put(COLUMN_ITEM_IMAGE, itemModel.getItemImage());
        cv.put(COLUMN_ITEM_USER_ID, itemModel.getItemUserId());
        cv.put(COLUMN_ITEM_IS_FOR_TRADE, itemModel.getItemIsForTrade());
        cv.put(COLUMN_ITEM_IS_FOR_SALE, itemModel.getItemIsForSale());
        cv.put(COLUMN_ITEM_IS_FOR_AUCTION, itemModel.getItemIsForAuction());


        db.beginTransaction();
        try{
            long insert = db.insert(ITEMS_TABLE, null, cv);
            if (insert != -1) {
                db.setTransactionSuccessful();
            }
        }finally {
            db.endTransaction();
        }
        db.close();
    }

    public ArrayList<ItemModel> getAllItems() {
        ArrayList<ItemModel> itemModelArrayList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + ITEMS_TABLE;
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int idColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_ID);
                int nameColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_NAME);
                int descriptionColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_DESCRIPTION);
                int priceColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_PRICE);
                int categoryColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_CATEGORY);
                int imageColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_IMAGE);
                int userIdColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_USER_ID);
                int isForTradeColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_IS_FOR_TRADE);
                int isForSaleColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_IS_FOR_SALE);
                int isForAuctionColumnIndex = cursor.getColumnIndex(COLUMN_ITEM_IS_FOR_AUCTION);

                String name = cursor.getString(nameColumnIndex);
                String description = cursor.getString(descriptionColumnIndex);
                int price = cursor.getInt(priceColumnIndex);
                String category = cursor.getString(categoryColumnIndex);
                String image = cursor.getString(imageColumnIndex);
                int userId = cursor.getInt(userIdColumnIndex);
                int isForTrade = cursor.getInt(isForTradeColumnIndex);
                int isForSale = cursor.getInt(isForSaleColumnIndex);
                int isForAuction = cursor.getInt(isForAuctionColumnIndex);

                ItemModel itemModel = new ItemModel(name, description, category, price, isForTrade, isForSale, isForAuction, userId);
                itemModelArrayList.add(itemModel);
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }
        db.close();
        return itemModelArrayList;
    }
}
