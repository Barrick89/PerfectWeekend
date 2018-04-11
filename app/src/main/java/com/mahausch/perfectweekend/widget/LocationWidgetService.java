package com.mahausch.perfectweekend.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.mahausch.perfectweekend.DetailActivity;
import com.mahausch.perfectweekend.R;
import com.mahausch.perfectweekend.data.LocationContract;

import java.util.ArrayList;

import static com.mahausch.perfectweekend.data.LocationContract.LocationEntry.CONTENT_URI;

public class LocationWidgetService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

        private Context context;
        private int mAppWidgetId;
        private ArrayList<Long> locationId = new ArrayList<>();
        private ArrayList<String> locationNames = new ArrayList<>();
        private Cursor cursor;


        public ListRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        @Override
        public void onCreate() {
            cursor = getBaseContext().getContentResolver().query(CONTENT_URI, null,
                    null, null, null);
        }

        @Override
        public void onDataSetChanged() {

            int nameIndex = cursor.getColumnIndex(LocationContract.LocationEntry.COLUMN_LOCATION_NAME);
            int locationIndex = cursor.getColumnIndex(LocationContract.LocationEntry._ID);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    locationNames.add(cursor.getString(nameIndex));
                    locationId.add(cursor.getLong(locationIndex));
                }
                cursor.close();
            }
        }

        @Override
        public void onDestroy() {

        }

        @Override
        public int getCount() {

            if (locationNames != null) {
                return locationNames.size() + 1;
            } else {
                return 0;
            }
        }

        @Override
        public RemoteViews getViewAt(int i) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.location_widget_item);

            if (i == 0) {
                rv.setViewVisibility(R.id.widget_title, View.VISIBLE);
                rv.setViewVisibility(R.id.widget_overview_name, View.GONE);
                rv.setTextViewText(R.id.widget_title, getString(R.string.widget_title));
            } else {
                rv.setViewVisibility(R.id.widget_title, View.GONE);
                rv.setViewVisibility(R.id.widget_overview_name, View.VISIBLE);
                rv.setTextViewText(R.id.widget_overview_name, "- " + locationNames.get(i - 1));
            }


            if (i > 0) {
                Bundle extras = new Bundle();
                extras.putLong(DetailActivity.EXTRA_LOCATION_ID, locationId.get(i - 1));
                Intent fillInIntent = new Intent();
                fillInIntent.putExtras(extras);
                rv.setOnClickFillInIntent(R.id.widget_overview_name, fillInIntent);
            }

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }

}