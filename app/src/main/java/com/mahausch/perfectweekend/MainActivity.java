package com.mahausch.perfectweekend;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.fab)
    FloatingActionButton fab;

    @BindView(R.id.recycler_view)
    RecyclerView recycler;

    public static final int LOCATION_LOADER = 0;

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
                startActivity(intent);
            }
        });

        recycler.setLayoutManager(
                new LinearLayoutManager(this)
        );
        adapter = new LocationAdapter(this, null);
        recycler.setAdapter(adapter);

        getSupportLoaderManager().initLoader(GARDEN_LOADER_ID, null, this);
    }
}
