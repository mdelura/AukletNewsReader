package com.gmail.deluramichal.aukletnewsreader.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.gmail.deluramichal.aukletnewsreader.R;

/**
 * Created by Michal Delura on 2015-10-04.
 */
public class NewsWidgetProvider extends AppWidgetProvider {

    public static final String NEWS_ITEM_ACTION =
            "com.gmail.deluramichal.aukletnewsreader.NEWS_ITEM_ACTION";
    public static final String EXTRA_ITEM = "com.gmail.deluramichal.aukletnewsreader.EXTRA_ITEM";

    // Called when the BroadcastReceiver receives an Intent broadcast.
    // Checks to see whether the intent's action is NEWS_ITEM_ACTION.
    @Override
    public void onReceive(Context context, Intent intent) {
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(NEWS_ITEM_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            String url = intent.getStringExtra(EXTRA_ITEM);
            Uri webPage = Uri.parse(url);
            Intent goToNewsIntent =
                    new Intent(Intent.ACTION_VIEW, webPage).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (goToNewsIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(goToNewsIntent);
            } else {
                Log.d(NewsWidgetProvider.class.getSimpleName() + " DEBUG: ",
                        "Couldn't call " + webPage.toString() + ", " + "no receiving apps " +
                        "installed!");
            }
        }
        super.onReceive(context, intent);
    }


    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // update each of the app widgets with the remote adapter
        for (int i = 0; i < appWidgetIds.length; ++i) {

            // Set up the intent that starts the NewsWidgetService, which will
            // provide the views for this collection.
            Intent intent = new Intent(context, NewsWidgetService.class);
            // Add the app widget ID to the intent extras.
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            // Instantiate the RemoteViews object for the app widget layout.
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.news_appwidget);
            // Set up the RemoteViews object to use a RemoteViews adapter.
            // This adapter connects
            // to a RemoteViewsService  through the specified intent.
            // This is how you populate the data.
            rv.setRemoteAdapter(R.id.list_view_appwidget, intent);

            // The empty view is displayed when the collection has no items.
            // It should be in the same layout used to instantiate the RemoteViews
            // object above.
            rv.setEmptyView(R.id.list_view_appwidget, R.id.empty_view_appwidget);

            //Setting up a pending intent template for individual items
            Intent goToNewsIntent = new Intent(context, NewsWidgetProvider.class);
            // Set the action for the intent.
            // When the user touches a particular view, it will have the effect of
            // broadcasting NEWS_ITEM_ACTION.
            goToNewsIntent.setAction(NewsWidgetProvider.NEWS_ITEM_ACTION);
            goToNewsIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetIds[i]);
            intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent goToNewsPendingIntent = PendingIntent.getBroadcast(context, 0, goToNewsIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setPendingIntentTemplate(R.id.list_view_appwidget, goToNewsPendingIntent);


            appWidgetManager.updateAppWidget(appWidgetIds[i], rv);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);

    }
}
