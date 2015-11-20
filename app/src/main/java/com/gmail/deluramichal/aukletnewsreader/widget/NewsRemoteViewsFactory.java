package com.gmail.deluramichal.aukletnewsreader.widget;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.gmail.deluramichal.aukletnewsreader.NewsFragment;
import com.gmail.deluramichal.aukletnewsreader.R;
import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;
import com.gmail.deluramichal.aukletnewsreader.data.NewsProvider;

/**
 * Created by Michal Delura on 2015-10-05.
 */
public class NewsRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private ContentResolver mContentResolver;
    private Cursor mNewsCursor;
    private int mAppWidgetId;

    public NewsRemoteViewsFactory(Context context, Intent intent) {
        mContext = context;
        mContentResolver = mContext.getContentResolver();
        mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
        // In onCreate() you setup any connections / cursors to your data source. Heavy lifting,
        // for example downloading or creating content etc, should be deferred to onDataSetChanged()
        // or getViewAt(). Taking more than 20 seconds in this call will result in an ANR.
        mNewsCursor = mContentResolver.query(
                NewsContract.ItemEntry.CONTENT_URI,
                NewsFragment.NEWS_COLUMNS,
                null,
                null,
                NewsContract.ItemEntry.SORT_PUB_DATE_DESC);
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
        mNewsCursor.close();
    }

    @Override
    public int getCount() {
        return mNewsCursor.getCount();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        // Construct a remote views item based on the app widget item XML file,
        // and set the text based on the position.
        RemoteViews rv = new RemoteViews(mContext.getPackageName(), R.layout.list_item_no_image);
        mNewsCursor.moveToPosition(position);
        rv.setTextViewText(R.id.list_item_title, mNewsCursor.getString(NewsFragment.COL_TITLE));
        rv.setTextViewText(R.id.list_item_date,
                NewsProvider.dateFormat(mNewsCursor.getLong(NewsFragment.COL_PUB_DATE)));
        rv.setTextViewText(R.id.list_item_description,
                mNewsCursor.getString(NewsFragment.COL_DESCRIPTION));

        // Next, set a fill-intent, which will be used to fill in the pending intent template
        // that is set on the collection view in StackWidgetProvider.
        Bundle extras = new Bundle();
        extras.putString(NewsWidgetProvider.EXTRA_ITEM,
                mNewsCursor.getString(NewsFragment.COL_LINK));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        // Make it possible to distinguish the individual on-click
        // action of a given item
        rv.setOnClickFillInIntent(R.id.list_item, fillInIntent);

        // Return the remote views object.
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
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
