package com.mahausch.perfectweekend;


import android.content.Intent;
import android.content.UriMatcher;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.mahausch.perfectweekend.data.LocationContract;
import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;

public class EditorActivity extends AppCompatActivity {

    private static Uri currentUri;
    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS, LOCATION);
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS + "/#", LOCATION_ID);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            currentUri = intent.getData();
        }

        if (currentUri.equals(LocationEntry.CONTENT_URI)) {
            setTitle(getString(R.string.add_location_title));
        } else {
            setTitle(getString(R.string.edit_location_title));
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
    }

}
