package com.mahausch.perfectweekend;

import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mahausch.perfectweekend.data.LocationContract.BASE_CONTENT_URI;
import static com.mahausch.perfectweekend.data.LocationContract.PATH_LOCATIONS;

public class DetailActivity extends AppCompatActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap map;
    public static final String EXTRA_LOCATION_ID = "com.mahausch.perfectweekend.extra.LOCATION_ID";
    private static final int LOCATION_DETAIL_LOADER_ID = 1;
    private static Uri locationUri;
    private static CameraPosition mapPosition;

    private double longitude;
    private double latitude;
    private String name;

    @BindView(R.id.detail_image)
    ImageView locationImageView;

    @BindView(R.id.location_name)
    TextView locationNameTextView;

    @BindView(R.id.location_description)
    TextView locationDescriptionTextView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.scrollview)
    NestedScrollView scrollView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow();
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        long locationID = getIntent().getLongExtra(EXTRA_LOCATION_ID, 0);
        locationUri = ContentUris.withAppendedId(
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_LOCATIONS).build(), locationID);

        getSupportLoaderManager().initLoader(LOCATION_DETAIL_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (map != null) {
            outState.putParcelable("mapPosition", map.getCameraPosition());
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mapPosition = savedInstanceState.getParcelable("mapPosition");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng location = new LatLng(latitude, longitude);
        map.addMarker(new MarkerOptions().position(location).title(name));

        if (mapPosition != null) {
            map.animateCamera(CameraUpdateFactory.newCameraPosition(mapPosition));
        } else {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 6));
        }
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
        int longitudeIndex = data.getColumnIndex(LocationEntry.COLUMN_LOCATION_LONGITUDE);
        int latitudeIndex = data.getColumnIndex(LocationEntry.COLUMN_LOCATION_LATITUDE);

        String locationName = data.getString(nameIndex);
        String locationImage = data.getString(imageIndex);
        String locationDescription = data.getString(descriptionIndex);
        double locationLongitude = data.getDouble(longitudeIndex);
        double locationLatitude = data.getDouble(latitudeIndex);

        locationNameTextView.setText(String.valueOf(locationName));
        name = locationName;
        Picasso.get().load(Uri.parse(locationImage)).into(locationImageView);
        locationDescriptionTextView.setText(locationDescription);
        longitude = locationLongitude;
        latitude = locationLatitude;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        if (map == null) {
            ScrollableMapFragment mapFragment = (ScrollableMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
            mapFragment.setListener(new ScrollableMapFragment.OnTouchListener() {
                @Override
                public void onTouch() {
                    scrollView.requestDisallowInterceptTouchEvent(true);
                }
            });
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
