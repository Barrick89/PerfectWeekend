package com.mahausch.perfectweekend.data;


import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATION:
                return insertLocation(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertLocation(Uri uri, ContentValues values) {

        String name = values.getAsString(LocationEntry.COLUMN_LOCATION_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Location requires a name");
        }

        String image = values.getAsString(LocationEntry.COLUMN_LOCATION_IMAGE);
        if (image == null) {
            throw new IllegalArgumentException("Location requires valid image");
        }

        String description = values.getAsString(LocationEntry.COLUMN_LOCATION_DESCRIPTION);
        if (description == null) {
            description = "";
        }

        String position = values.getAsString(LocationEntry.COLUMN_LOCATION_POSITION);
        if (position == null) {
            throw new IllegalArgumentException("Location requires valid position");
        }

        Double longitude = values.getAsDouble(LocationEntry.COLUMN_LOCATION_LONGITUDE);
        if (longitude == null) {
            throw new IllegalArgumentException("Location requires valid longitude");
        }

        Double latitude = values.getAsDouble(LocationEntry.COLUMN_LOCATION_LATITUDE);
        if (latitude == null) {
            throw new IllegalArgumentException("Location requires valid latitude");
        }

        SQLiteDatabase db = mLocationDbHelper.getWritableDatabase();

        long id = db.insert(LocationEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mLocationDbHelper.getWritableDatabase();

        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        String table;

        switch (match) {
            case LOCATION_ID:
                selection = LocationEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
            case LOCATION:
                table = LocationEntry.TABLE_NAME;
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        rowsDeleted = db.delete(table, selection, selectionArgs);

        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case LOCATION:
                return updateLocation(uri, contentValues, selection, selectionArgs);
            case LOCATION_ID:
                selection = LocationEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateLocation(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateLocation(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        if (values.size() == 0) {
            return 0;
        }

        if (values.containsKey(LocationEntry.COLUMN_LOCATION_NAME)) {
            String name = values.getAsString(LocationEntry.COLUMN_LOCATION_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Location requires a name");
            }
        }

        if (values.containsKey(LocationEntry.COLUMN_LOCATION_IMAGE)) {
            String image = values.getAsString(LocationEntry.COLUMN_LOCATION_IMAGE);
            if (image == null) {
                throw new IllegalArgumentException("Location requires an image");
            }
        }

        if (values.containsKey(LocationEntry.COLUMN_LOCATION_DESCRIPTION)) {
            String description = values.getAsString(LocationEntry.COLUMN_LOCATION_DESCRIPTION);
            if (description == null) {
                description = "";
            }
        }

        if (values.containsKey(LocationEntry.COLUMN_LOCATION_POSITION)) {
            String position = values.getAsString(LocationEntry.COLUMN_LOCATION_POSITION);
            if (position == null) {
                throw new IllegalArgumentException("Location requires valid position");
            }
        }

        if (values.containsKey(LocationEntry.COLUMN_LOCATION_LONGITUDE)) {
            Double longitude = values.getAsDouble(LocationEntry.COLUMN_LOCATION_LONGITUDE);
            if (longitude == null) {
                throw new IllegalArgumentException("Location requires valid longitude");
            }
        }

        if (values.containsKey(LocationEntry.COLUMN_LOCATION_LATITUDE)) {
            Double latitude = values.getAsDouble(LocationEntry.COLUMN_LOCATION_LATITUDE);
            if (latitude == null) {
                throw new IllegalArgumentException("Location requires valid latitude");
            }
        }

        SQLiteDatabase db = mLocationDbHelper.getWritableDatabase();

        int rowsUpdated = db.update(LocationEntry.TABLE_NAME, values, selection, selectionArgs);

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

}
