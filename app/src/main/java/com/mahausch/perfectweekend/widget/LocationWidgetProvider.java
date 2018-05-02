package com.mahausch.perfectweekend.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

import com.mahausch.perfectweekend.DetailActivity;
import com.mahausch.perfectweekend.R;

public class LocationWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WidgetUpdateService.enqueueWork(context, new Intent(context, WidgetUpdateService.class));
        } else {
            context.startService(new Intent(context, WidgetUpdateService.class));
        }
    }

    public static void updateWidgets(Context context, AppWidgetManager appWidgetManager,
                                     int[] appWidgetIds) {
        for (int i = 0; i < appWidgetIds.length; ++i) {
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.location_widget);

            Intent intent = new Intent(context, LocationWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            rv.setRemoteAdapter(appWidgetIds[i], R.id.widget_list, intent);

            Intent appIntent = new Intent(context, DetailActivity.class);
            PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0,
                    appIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.widget_list, appPendingIntent);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
    }
}
