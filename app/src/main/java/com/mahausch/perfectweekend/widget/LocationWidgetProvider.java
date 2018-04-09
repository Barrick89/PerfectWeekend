package com.mahausch.perfectweekend.widget;


import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.mahausch.perfectweekend.R;

public class LocationWidgetProvider extends AppWidgetProvider {

    public static final String EXTRA_ITEM = "extraItem";
    public static final String PREVIOUS_ARROW_CLICKED = "PREVIOUS_ARROW_CLICKED";
    public static final String NEXT_ARROW_CLICKED = "NEXT_ARROW_CLICKED";

    public static String lastArrowClicked;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; ++i) {

            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.location_widget);

            Intent intent = new Intent(context, LocationWidgetService.class);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.putExtra("lastArrowClicked", lastArrowClicked);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

            rv.setOnClickPendingIntent(R.id.widget_left_arrow, getPendingSelfIntent(context, PREVIOUS_ARROW_CLICKED, i));
            rv.setOnClickPendingIntent(R.id.widget_right_arrow, getPendingSelfIntent(context, NEXT_ARROW_CLICKED, i));

            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }

    protected PendingIntent getPendingSelfIntent(Context context, String action, int widgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        intent.putExtra("widgetId", widgetId);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        super.onReceive(context, intent);
        ComponentName component = new ComponentName(context,
                LocationWidgetProvider.class);
        int[] widgetId = AppWidgetManager.getInstance(context).getAppWidgetIds(component);

        if (PREVIOUS_ARROW_CLICKED.equals(intent.getAction())) {
            lastArrowClicked = PREVIOUS_ARROW_CLICKED;
        } else if (NEXT_ARROW_CLICKED.equals(intent.getAction())) {
            lastArrowClicked = NEXT_ARROW_CLICKED;
        }
        onUpdate(context, AppWidgetManager.getInstance(context), widgetId);
    }
}
