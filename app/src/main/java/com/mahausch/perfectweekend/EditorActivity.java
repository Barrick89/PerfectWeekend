package com.mahausch.perfectweekend;


import android.content.UriMatcher;
import android.support.v7.app.AppCompatActivity;

import com.mahausch.perfectweekend.data.LocationContract;

public class EditorActivity extends AppCompatActivity {

    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS, LOCATION);
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS + "/#", LOCATION_ID);
    }
}
