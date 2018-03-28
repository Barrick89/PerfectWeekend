package com.mahausch.perfectweekend;


import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mahausch.perfectweekend.data.LocationContract.BASE_CONTENT_URI;
import static com.mahausch.perfectweekend.data.LocationContract.PATH_LOCATIONS;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String EXTRA_LOCATION_ID = "com.mahausch.perfectweekend.extra.LOCATION_ID";
    private static final int LOCATION_DETAIL_LOADER_ID = 1;
    private static Uri locationUri;

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

        long locationID = getIntent().getLongExtra(EXTRA_LOCATION_ID, 0);
        locationUri = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build(), locationID);

        getSupportLoaderManager().initLoader(LOCATION_DETAIL_LOADER_ID, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case R.id.action_edit:
                Intent intent = new Intent(DetailActivity.this, EditorActivity.class);
                intent.setData(locationUri);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                deleteLocation();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (dialogInterface != null) {
                    dialogInterface.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteLocation() {
        int rowsDeleted = getContentResolver().delete(locationUri, null, null);
        if (rowsDeleted == 0) {
            Toast.makeText(this, getString(R.string.delete_location_failed),
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.delete_location_successful),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, locationUri, null,
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
