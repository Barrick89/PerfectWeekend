package com.mahausch.perfectweekend;


import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mahausch.perfectweekend.data.LocationContract.LocationEntry.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recycler_view)
    RecyclerView recycler;

    private LocationAdapter adapter;
    private LinearLayoutManager manager;

    static Parcelable listState;

    public static final int LOCATION_LOADER_ID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.location_overview);
        ButterKnife.bind(this);

        //Set OnClickListener for the FloatingActionButton to launch EditorActivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(CONTENT_URI);
                startActivity(intent);
            }
        });

        manager = new LinearLayoutManager(this);
        recycler.setLayoutManager(manager);

        adapter = new LocationAdapter(this, null);
        recycler.setAdapter(adapter);

        getSupportLoaderManager().initLoader(LOCATION_LOADER_ID, null, this);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        listState = manager.onSaveInstanceState();
        outState.putParcelable("listState", listState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        listState = savedInstanceState.getParcelable("listState");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(this, CONTENT_URI, null,
                null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        data.moveToFirst();
        adapter.swapCursor(data);
        if (listState != null) {
            manager.onRestoreInstanceState(listState);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    public void onLocationClick(View view) {
        ImageView imgView = (ImageView) view.findViewById(R.id.overview_image);
        long locationID = (long) imgView.getTag();
        Intent intent = new Intent(getBaseContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_LOCATION_ID, locationID);
        startActivity(intent);
    }
}
