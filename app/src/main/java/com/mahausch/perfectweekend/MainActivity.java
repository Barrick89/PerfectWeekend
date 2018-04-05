package com.mahausch.perfectweekend;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.mahausch.perfectweekend.DetailActivity.EXTRA_LOCATION_IMAGE_TRANSITION_NAME;
import static com.mahausch.perfectweekend.data.LocationContract.LocationEntry.CONTENT_URI;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        LocationAdapter.LocationItemClickListener {

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

        //Add custom app bar title
        TextView tv = new TextView(getApplicationContext());
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        tv.setLayoutParams(lp);
        tv.setText(R.string.mainactivity_title);
        tv.setTextSize(20);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        tv.setTypeface(Typeface.SERIF, Typeface.BOLD_ITALIC);
        getSupportActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        getSupportActionBar().setCustomView(tv);

        //Set OnClickListener for the FloatingActionButton to launch EditorActivity
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                intent.setData(CONTENT_URI);
                startActivity(intent);
            }
        });

        //Decide whether to display recipes in grid or list depending on display size
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        if (dpWidth >= 500) {
            manager = new GridLayoutManager(this, numberOfColumns(), GridLayoutManager.VERTICAL, false);
        } else {
            manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        }

        recycler.setLayoutManager(manager);
        recycler.setHasFixedSize(true);
        recycler.setItemViewCacheSize(20);
        recycler.setDrawingCacheEnabled(true);
        recycler.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        adapter = new LocationAdapter(this, null, this);
        recycler.setAdapter(adapter);

        getSupportLoaderManager().initLoader(LOCATION_LOADER_ID, null, this);
    }

    //Decide how many columns to display depending on display size
    private int numberOfColumns() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int widthDivider = 600;
        int width = displayMetrics.widthPixels;
        int nColumns = width / widthDivider;
        if (nColumns < 2) return 2;
        return nColumns;
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

    @Override
    public void onLocationItemClick(int position, ImageView imageView) {
        long locationID = (long) imageView.getTag();
        Intent intent = new Intent(getBaseContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.EXTRA_LOCATION_ID, locationID);
        intent.putExtra(EXTRA_LOCATION_IMAGE_TRANSITION_NAME, ViewCompat.getTransitionName(imageView));

        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                this,
                imageView,
                ViewCompat.getTransitionName(imageView));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            startActivity(intent, options.toBundle());
        } else {
            startActivity(intent);
        }
    }
}
