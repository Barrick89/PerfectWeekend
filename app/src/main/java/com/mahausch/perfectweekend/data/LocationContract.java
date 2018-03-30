package com.mahausch.perfectweekend.data;


import android.net.Uri;
import android.provider.BaseColumns;

public class LocationContract {

    private LocationContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.mahausch.perfectweekend";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_LOCATIONS = "locations";

    public static abstract class LocationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build();

        public static final String TABLE_NAME = "locations";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_LOCATION_NAME = "name";
        public static final String COLUMN_LOCATION_IMAGE = "image";
        public static final String COLUMN_LOCATION_DESCRIPTION = "description";
        public static final String COLUMN_LOCATION_POSITION = "position";
        public static final String COLUMN_LOCATION_LONGITUDE = "longitude";
        public static final String COLUMN_LOCATION_LATITUDE = "latitude";
    }
}
