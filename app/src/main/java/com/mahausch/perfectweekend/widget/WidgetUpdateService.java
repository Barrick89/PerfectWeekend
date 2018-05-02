package com.mahausch.perfectweekend.widget;


import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.mahausch.perfectweekend.R;

public class WidgetUpdateService extends JobIntentService {

    /**
     * Unique job ID for this service.
     */
    static final int JOB_ID = 1000;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    public static void enqueueWork(Context context, Intent work) {
        enqueueWork(context, WidgetUpdateService.class, JOB_ID, work);
    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this, LocationWidgetProvider.class));
        //Trigger data update to handle the ListView widgets and force a data refresh
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);
        //Now update all widgets
        LocationWidgetProvider.updateWidgets(this, appWidgetManager, appWidgetIds);
    }
}
