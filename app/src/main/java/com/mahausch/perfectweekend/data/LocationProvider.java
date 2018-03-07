package com.mahausch.perfectweekend.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;

public class LocationProvider extends ContentProvider {

    private LocationDbHelper mLocationDbHelper;

    public static final String LOG_TAG = LocationProvider.class.getSimpleName();

    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS, LOCATION);
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS + "/#", LOCATION_ID);
    }

    @Override
    public boolean onCreate() {
        mLocationDbHelper = new LocationDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase db = mLocationDbHelper.getReadableDatabase();

        Cursor cursor;

        int match = sUriMatcher.match(uri);
        String table;

        switch (match) {
            case LOCATION_ID:
                selection = LocationEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case LOCATION:
                table = LocationEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        cursor = db.query(table, projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

}
