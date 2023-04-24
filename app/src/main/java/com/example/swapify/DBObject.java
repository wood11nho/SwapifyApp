package com.example.swapify;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DBObject extends SQLiteOpenHelper {

    public static final String USERS_TABLE = "USERS";
    public static final String COLUMN_NAME = "NAME";
    public static final String COLUMN_USERNAME = "USERNAME";
    public static final String COLUMN_EMAIL = "EMAIL";
    public static final String COLUMN_PASSWORD = "PASSWORD";

    public DBObject(@Nullable Context context) {
        super(context, "swapify.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableStatement = "CREATE TABLE " + USERS_TABLE + "(ID INTEGER PRIMARY KEY, " + COLUMN_NAME + " TEXT, " + COLUMN_USERNAME + " TEXT, " + COLUMN_EMAIL + " TEXT, " + COLUMN_PASSWORD + " TEXT)";

        db.execSQL(createTableStatement);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public boolean addOne(CustomerModel customerModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_NAME, customerModel.getName());
        cv.put(COLUMN_USERNAME, customerModel.getUsername());
        cv.put(COLUMN_EMAIL, customerModel.getEmail());
        cv.put(COLUMN_PASSWORD, customerModel.getPasswordHash());

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
}
