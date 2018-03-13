package com.mahausch.perfectweekend;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.mahausch.perfectweekend.data.LocationContract;
import com.mahausch.perfectweekend.data.LocationContract.LocationEntry;

import butterknife.BindView;
import butterknife.ButterKnife;

public class EditorActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 0;
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1;

    private Uri currentUri;
    private Uri imageUri;

    private static final int LOCATION = 100;
    private static final int LOCATION_ID = 101;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS, LOCATION);
        sUriMatcher.addURI(LocationContract.CONTENT_AUTHORITY, LocationContract.PATH_LOCATIONS + "/#", LOCATION_ID);
    }

    @BindView(R.id.imageView)
    ImageView imageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        ButterKnife.bind(this);

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
                imageView.setImageURI(imageUri);
                imageView.invalidate();
            }
        }
    }

}
