package com.mahausch.perfectweekend;


import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mahausch.perfectweekend.data.LocationContract.BASE_CONTENT_URI;
import static com.mahausch.perfectweekend.data.LocationContract.PATH_LOCATIONS;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_LOCATION_ID = "com.mahausch.perfectweekend.extra.LOCATION_ID";
    private static final int LOCATION_DETAIL_LOADER_ID = 1;
    private long locationID;

    @BindView(R.id.detail_image)
    ImageView locationImageView;

    @BindView(R.id.location_name)
    TextView locationNameTextView;

    @BindView(R.id.location_description)
    TextView locationDescriptionTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        locationID = getIntent().getLongExtra(EXTRA_LOCATION_ID, 0);

        getSupportLoaderManager().initLoader(LOCATION_DETAIL_LOADER_ID, null, this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri SINGLE_LOCATION_URI = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build(), locationID);
        return new CursorLoader(this, SINGLE_LOCATION_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();

        int nameIndex = data.getColumnIndex(LocationEntry.COLUMN_LOCATION_NAME);
        int imageIndex = data.getColumnIndex(LocationEntry.COLUMN_LOCATION_IMAGE);
        int descriptionIndex = data.getColumnIndex(LocationEntry.COLUMN_LOCATION_DESCRIPTION);
        int positionIndex = data.getColumnIndex(LocationEntry.COLUMN_LOCATION_POSITION);

        String locationName = data.getString(nameIndex);
        String locationImage = data.getString(imageIndex);
        String locationDescription = data.getString(descriptionIndex);
        String locationPosition = data.getString(positionIndex);

        locationNameTextView.setText(String.valueOf(locationName));
        locationImageView.setImageURI(Uri.parse(locationImage));
        locationDescriptionTextView.setText(locationDescription);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
