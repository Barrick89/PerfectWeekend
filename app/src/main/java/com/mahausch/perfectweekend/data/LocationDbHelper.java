package com.mahausch.perfectweekend.data;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;

public class LocationDbHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "location.db";

    public LocationDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_LOCATION_TABLE = "CREATE TABLE IF NOT EXISTS " + LocationEntry.TABLE_NAME + " ("
                + LocationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + LocationEntry.COLUMN_LOCATION_NAME + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_IMAGE + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_DESCRIPTION + " TEXT,"
                + LocationEntry.COLUMN_LOCATION_POSITION + " TEXT NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_LONGITUDE + " REAL NOT NULL, "
                + LocationEntry.COLUMN_LOCATION_LATITUDE + " REAL NOT NULL);";

        db.execSQL(SQL_CREATE_LOCATION_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
    }
}
