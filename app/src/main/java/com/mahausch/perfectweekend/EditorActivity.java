package com.mahausch.perfectweekend;


import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.mahausch.perfectweekend.data.LocationContract;
import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;
import com.mahausch.perfectweekend.widget.WidgetUpdateService;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String TAG = EditorActivity.class.getSimpleName();
    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;
    private static final int REQUEST_TAKE_PHOTO = 2;
    private static final int PLACE_PICKER_REQUEST = 3;
    private static final int PERMISSIONS_REQUEST_FINE_LOCATION = 4;

    private Uri currentUri;
    private Uri imageUri;
    private String placeName;
    private double longitude;
    private double latitude;
    private int scrollState;

    private static final int LOCATION_LOADER = 0;
    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS, LOCATION);
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS + "/#", LOCATION_ID);
    }

    private boolean locationHasChanged = false;
    private View.OnTouchListener touchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            locationHasChanged = true;
            return false;
        }
    };

    private GoogleApiClient client;

    @BindView(R.id.editor_scrollview)
    ScrollView scrollView;

    @BindView(R.id.take_photo_button)
    Button takePhotoButton;

    @BindView(R.id.storage_button)
    Button imageFromStorageButton;

    @BindView(R.id.imageview)
    ImageView imageView;

    @BindView(R.id.name_edittext)
    EditText nameEditText;

    @BindView(R.id.description_edittext)
    EditText descriptionEditText;

    @BindView(R.id.location_button)
    Button locationButton;

    @BindView(R.id.location_textview)
    TextView locationTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        Intent intent = getIntent();
        if (intent.getData() != null) {
            currentUri = intent.getData();

            if (currentUri.equals(LocationEntry.CONTENT_URI)) {
                setTitle(getString(R.string.add_location_title));
            } else {
                setTitle(getString(R.string.edit_location_title));
                getLoaderManager().initLoader(LOCATION_LOADER, null, this);
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);
        ButterKnife.bind(this);

        /*
         * Set OnTouchListener on every button and text input
         * field to detect if the user made changes to know if the back button must trigger
         * the unsavedChangesDialog
         */
        takePhotoButton.setOnTouchListener(touchListener);
        imageFromStorageButton.setOnTouchListener(touchListener);
        nameEditText.setOnTouchListener(touchListener);
        descriptionEditText.setOnTouchListener(touchListener);
        locationButton.setOnTouchListener(touchListener);


        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Places.PLACE_DETECTION_API)
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, this)
                .build();

    }

    @Override
    public void onConnected(@Nullable Bundle connectionHint) {
        Log.i(TAG, "API Client Connection Successful!");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "API Client Connection Suspended!");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.e(TAG, "API Client Connection Failed!");
    }

    //Set imageView and locationTextView when the device gets turned to landscape mode
    @Override
    protected void onResume() {
        super.onResume();

        if (imageUri != null) {
            Picasso.get().load(imageUri).into(imageView);
        }

        if (placeName != null) {
            locationTextView.setText(placeName);
        }

        scrollView.setVerticalScrollbarPosition(scrollState);
    }

    // When the "Image from Storage" button was clicked, this method is triggered
    public void getImageFromStorage(View view) {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
            return;
        }
        openImageSelector();
    }

    private void openImageSelector() {
        Intent intent;
        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {

            if (resultData != null) {
                imageUri = resultData.getData();
                final int takeFlags = resultData.getFlags()
                        & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                        | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    getContentResolver().takePersistableUriPermission(imageUri, takeFlags);
                }
                Picasso.get().load(imageUri).fit().centerCrop().into(imageView);
                imageView.invalidate();
            }
        } else if (requestCode == REQUEST_TAKE_PHOTO && resultCode == Activity.RESULT_OK) {

            Picasso.get().load(imageUri).fit().centerCrop().into(imageView);
            imageView.invalidate();

        } else if (requestCode == PLACE_PICKER_REQUEST && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, resultData);
            if (place == null) {
                Log.i(TAG, "No place selected");
                return;
            }

            placeName = place.getName().toString();
            LatLng latLng = place.getLatLng();
            longitude = latLng.longitude;
            latitude = latLng.latitude;
            locationTextView.setText(placeName);
        }
    }

    // When the "Take Photo" button was clicked, this method is triggered
    public void takePhoto(View view) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "com.mahausch.perfectweekend.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                imageUri = photoURI;
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        return image;
    }

    public void pickLocation(View view) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(EditorActivity.this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_FINE_LOCATION);
        }
        try {
            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
            Intent i = builder.build(this);
            startActivityForResult(i, PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(TAG, String.format("GooglePlayServices Not Available [%s]", e.getMessage()));
        } catch (Exception e) {
            Log.e(TAG, String.format("PlacePicker Exception: %s", e.getMessage()));
        }
    }

    public void saveLocation(View view) {
        if (imageUri == null ||
                TextUtils.isEmpty(nameEditText.getText()) ||
                TextUtils.isEmpty(locationTextView.getText())) {
            Toast.makeText(this, getString(R.string.missing_input),
                    Toast.LENGTH_SHORT).show();
        } else {
            ContentValues values = new ContentValues();
            int match = sUriMatcher.match(currentUri);

            switch (match) {
                case LOCATION:
                case LOCATION_ID:
                    values.put(LocationEntry.COLUMN_LOCATION_IMAGE, imageUri.toString());
                    values.put(LocationEntry.COLUMN_LOCATION_NAME, nameEditText.getText().toString().trim());
                    values.put(LocationEntry.COLUMN_LOCATION_DESCRIPTION, descriptionEditText.getText().toString());
                    values.put(LocationEntry.COLUMN_LOCATION_POSITION, placeName);
                    values.put(LocationEntry.COLUMN_LOCATION_LONGITUDE, longitude);
                    values.put(LocationEntry.COLUMN_LOCATION_LATITUDE, latitude);
                    break;

                default:
                    throw new IllegalArgumentException("Unknown URI " + currentUri);
            }

            if (currentUri.equals(LocationEntry.CONTENT_URI)) {

                Uri newUri = getContentResolver().insert(currentUri, values);

                if (newUri == null) {
                    Toast.makeText(this, getString(R.string.editor_insert_location_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_insert_location_successful),
                            Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WidgetUpdateService.enqueueWork(this,
                                new Intent(this, WidgetUpdateService.class));
                    } else {
                        startService(new Intent(this, WidgetUpdateService.class));
                    }
                }
            } else {
                int rowsUpdated = getContentResolver().update(currentUri, values, null, null);

                if (rowsUpdated == 0) {
                    Toast.makeText(this, getString(R.string.editor_update_game_failed),
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, getString(R.string.editor_update_game_successful),
                            Toast.LENGTH_SHORT).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        WidgetUpdateService.enqueueWork(this,
                                new Intent(this, WidgetUpdateService.class));
                    } else {
                        startService(new Intent(this, WidgetUpdateService.class));
                    }
                }
            }
            finish();
        }
    }

    /*
    * If an existing location gets edited, the CursorLoader
    * loads the existing information from the database
    */
    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection;
        projection = new String[]{
                    LocationEntry._ID,
                    LocationEntry.COLUMN_LOCATION_IMAGE,
                    LocationEntry.COLUMN_LOCATION_NAME,
                    LocationEntry.COLUMN_LOCATION_DESCRIPTION,
                LocationEntry.COLUMN_LOCATION_POSITION,
                LocationEntry.COLUMN_LOCATION_LONGITUDE,
                LocationEntry.COLUMN_LOCATION_LATITUDE
        };
        return new CursorLoader(this, currentUri, projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int imageColumnIndex = cursor.getColumnIndex("image");
            int nameColumnIndex = cursor.getColumnIndex("name");
            int descriptionColumnIndex = cursor.getColumnIndex("description");
            int positionColumnIndex = cursor.getColumnIndex("position");
            int longitudeColumnIndex = cursor.getColumnIndex("longitude");
            int latitudeColumnIndex = cursor.getColumnIndex("latitude");

            String locationImage = cursor.getString(imageColumnIndex);
            String locationName = cursor.getString(nameColumnIndex);
            String locationDescription = cursor.getString(descriptionColumnIndex);
            String locationPosition = cursor.getString(positionColumnIndex);
            double locationLongitude = cursor.getDouble(longitudeColumnIndex);
            double locationLatitude = cursor.getDouble(latitudeColumnIndex);

            imageUri = Uri.parse(locationImage);
            imageView.setImageURI(imageUri);
            nameEditText.setText(locationName);
            descriptionEditText.setText(locationDescription);
            placeName = locationPosition;
            locationTextView.setText(placeName);
            longitude = locationLongitude;
            latitude = locationLatitude;
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameEditText.setText("");
        descriptionEditText.setText("");
        locationTextView.setText("");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("imageUri", imageUri);
        outState.putString("placeName", placeName);
        outState.putDouble("longitude", longitude);
        outState.putDouble("latitude", latitude);
        outState.putInt("scrollState", scrollView.getVerticalScrollbarPosition());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        imageUri = savedInstanceState.getParcelable("imageUri");
        placeName = savedInstanceState.getString("placeName");
        longitude = savedInstanceState.getDouble("longitude");
        latitude = savedInstanceState.getDouble("latitude");
        scrollState = savedInstanceState.getInt("scrollState");
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!locationHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!locationHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
