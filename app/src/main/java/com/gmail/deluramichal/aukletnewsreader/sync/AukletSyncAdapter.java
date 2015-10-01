package com.gmail.deluramichal.aukletnewsreader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.gmail.deluramichal.aukletnewsreader.R;
import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.xml.sax.SAXException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;

/**
 * Created by Michal Delura on 2015-07-31.
 */
public class AukletSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final int BYTE_CHUNK_SIZE = 4096;
    private ContentResolver mContentResolver;

    public final String LOG_TAG = AukletSyncAdapter.class.getSimpleName();
    // Interval at which to sync with the weather, in SECONDS
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;//TODO: Get from Settings
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final String NEWS_MIME_TYPE = "text/html; charset=utf-8";
    private static final String IMAGE_ELEMENT_TAG = "img";
    private static final String IMAGE_SOURCE_ATTRIBUTE = "src";
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
    private static final long DAYS_DATA_STORED = DAY_IN_MILLIS * 3;//TODO: Get from Settings
    private static final int YEAR_2000 = 2000;

    private static final String[] CHANNEL_SOURCES = {
            NewsContract.ChannelEntry._ID,
            NewsContract.ChannelEntry.COLUMN_SOURCE_URL
    };

    private static final int COL_CHANNEL_ID = 0;
    private static final int COL_CHANNEL_SOURCE_URL = 1;

    public AukletSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContentResolver = context.getContentResolver();
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");


        Cursor channelCursor = mContentResolver.query(
                NewsContract.ChannelEntry.CONTENT_URI,
                CHANNEL_SOURCES,
                null,
                null,
                null);

            while (channelCursor.moveToNext()) {
                //Fetch RSS feed from channel's link
                getRssFeed(
                        channelCursor.getInt(COL_CHANNEL_ID),
                        channelCursor.getString(COL_CHANNEL_SOURCE_URL));
            }
        channelCursor.close();
    }

    private void getRssFeed(int channelId, String channelSourceUrl) {
        URL url = null;
        try {
            url = new URL(channelSourceUrl);
        } catch (MalformedURLException e) {
            Log.e(e.getClass().getSimpleName(), e.getMessage());
        }
        RssFeed feed = null;
        try {
            feed = RssReader.read(url);
        } catch (SAXException e) {
            Log.e(e.getClass().getSimpleName(), e.getMessage());
        } catch (IOException e) {
            Log.e(e.getClass().getSimpleName(), e.getMessage());
        }

        ArrayList<RssItem> rssItems = feed.getRssItems();
        Vector<ContentValues> cVVector = new Vector<>(rssItems.size());

        long dayTime = System.currentTimeMillis();

        for (RssItem rssItem : rssItems) {
            Log.d(LOG_TAG, "CHANNEL_KEY: " + channelId);
            Log.d(LOG_TAG, "Title: " + rssItem.getTitle());
            Log.d(LOG_TAG, "Content: " + rssItem.getContent());
            Log.d(LOG_TAG, "Description: " + rssItem.getDescription());
            Log.d(LOG_TAG, "Link: " + rssItem.getLink());
            Log.d(LOG_TAG, "PubDate: " + rssItem.getPubDate());

            ContentValues newsValues = new ContentValues();
            newsValues.put(NewsContract.ItemEntry.COLUMN_CHANNEL_KEY, channelId);
            newsValues.put(NewsContract.ItemEntry.COLUMN_CONTENT, rssItem.getContent());
            newsValues.put(NewsContract.ItemEntry.COLUMN_LINK, rssItem.getLink());
            newsValues.put(NewsContract.ItemEntry.COLUMN_TITLE, rssItem.getTitle());
            newsValues.put(NewsContract.ItemEntry.COLUMN_PUB_DATE,
                    validateDateYear(rssItem.getPubDate()));
            newsValues.put(NewsContract.ItemEntry.COLUMN_SYNC_DATE, dayTime);

            //Get data from description
            Document docFromDescription = Jsoup.parse(
                    rssItem.getDescription(), NEWS_MIME_TYPE);
            Element imageFromDescription = docFromDescription.select(IMAGE_ELEMENT_TAG).first();
            //Get actual description string without html tags
            try {
                newsValues.put(NewsContract.ItemEntry.COLUMN_DESCRIPTION,
                        docFromDescription.text());
            } catch (NullPointerException e) {
                Log.d(LOG_TAG, "No description");
            }
            //Get image and image source URL
            try {
                String imageSource = imageFromDescription.attr(IMAGE_SOURCE_ATTRIBUTE);

                newsValues.put(NewsContract.ItemEntry.COLUMN_IMAGE_SRC, imageSource);
                newsValues.put(NewsContract.ItemEntry.COLUMN_IMAGE, getBytesFromImageFromUrl(imageSource));
            } catch (NullPointerException e) {
                Log.d(LOG_TAG, "No image source");
            }
            cVVector.add(newsValues);
        }

        int inserted = 0;
        // add to database
        if (cVVector.size() > 0) {
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);

            inserted = mContentResolver.bulkInsert(NewsContract.ItemEntry.CONTENT_URI, cvArray);

            //Delete old news //TODO: Check
            mContentResolver.delete(
                    NewsContract.ItemEntry.CONTENT_URI,
                    NewsContract.ItemEntry.COLUMN_PUB_DATE + "< ?",
                    new String[]
                            {Long.toString(dayTime-DAYS_DATA_STORED)});
        }

        Log.d(LOG_TAG, "Fetch RSS feed " + channelSourceUrl + " complete. " + inserted + " Inserted");
    }

    private long validateDateYear(Date date) {
        //Validates if date's year is earlier than 2000 - if so change +2000 years
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (calendar.get(Calendar.YEAR) < YEAR_2000) {
            calendar.add(Calendar.YEAR, YEAR_2000);
        }
        return calendar.getTimeInMillis();
    }

    private byte[] getBytesFromImageFromUrl(String sourceUrl) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream;
        URL url;
        byte[] byteArray = null;
        try {
            url = new URL(sourceUrl);
            inputStream = url.openStream();
            byte[] byteChunk = new byte[BYTE_CHUNK_SIZE];
            int n;

            while ((n = inputStream.read(byteChunk)) > 0) {
                byteArrayOutputStream.write(byteChunk, 0, n);
            }
            byteArray = byteArrayOutputStream.toByteArray();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Failed while reading bytes from: " + sourceUrl + " " +
                    e.getMessage());
        }
        return byteArray;

    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            onAccountCreated(newAccount, context);

        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        AukletSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }


}
