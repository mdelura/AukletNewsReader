package com.gmail.deluramichal.aukletnewsreader.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

/**
 * Created by Michal Delura on 2015-07-28.
 */
public class NewsProvider extends ContentProvider{

    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private NewsDbOpenHelper mOpenHelper;

    /*Neeeded queries:
    -Channel - for updating news
    -Items - for displaying news
    -Item with channel - for complex news display
    */
    static final int CHANNEL = 100;
    static final int ITEM = 101;
    static final int ITEM_WITH_CHANNEL = 102;

    private static final SQLiteQueryBuilder sItemByChannelQueryBuilder;
    static{
        sItemByChannelQueryBuilder = new SQLiteQueryBuilder();

        //Default inner join of channels with items:
        //Item INNER JOIN Channel ON item.channel_id = channel._id
        sItemByChannelQueryBuilder.setTables(
                NewsContract.ItemEntry.TABLE_NAME + " INNER JOIN " +
                        NewsContract.ChannelEntry.TABLE_NAME +
                        " ON " + NewsContract.ItemEntry.TABLE_NAME +
                        "." + NewsContract.ItemEntry.COLUMN_CHANNEL_KEY +
                        " = " + NewsContract.ChannelEntry.TABLE_NAME +
                        "." + NewsContract.ChannelEntry._ID);
    }

    static UriMatcher buildUriMatcher() {
    /*
    All paths added to the UriMatcher have a corresponding code to return when a match is found.
     The code passed into the constructor represents the code to return for the root URI.  It's
     common to use NO_MATCH as the code for this case.
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = NewsContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, NewsContract.PATH_CHANNEL, CHANNEL);
        matcher.addURI(authority, NewsContract.PATH_ITEM, ITEM);
        matcher.addURI(authority, NewsContract.PATH_ITEM + "/*", ITEM_WITH_CHANNEL);

        return matcher;
    }
    @Override
    public boolean onCreate() {
        mOpenHelper = new NewsDbOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case ITEM_WITH_CHANNEL: {
                retCursor = sItemByChannelQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            }
            case ITEM: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.ItemEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case CHANNEL: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        NewsContract.ChannelEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case CHANNEL:
                return NewsContract.ChannelEntry.CONTENT_TYPE;
            case ITEM:
                return NewsContract.ItemEntry.CONTENT_TYPE;
            case ITEM_WITH_CHANNEL:
                return NewsContract.ItemEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case ITEM: {
                normalizeDate(values);
                long _id = db.insert(NewsContract.ItemEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = NewsContract.ItemEntry.buildItemUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CHANNEL: {
                long _id = db.insert(NewsContract.ChannelEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = NewsContract.ChannelEntry.buildChannelUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case ITEM:
                rowsDeleted = db.delete(
                        NewsContract.ItemEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CHANNEL:
                rowsDeleted = db.delete(
                        NewsContract.ChannelEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case ITEM:
                normalizeDate(values);
                rowsUpdated = db.update(NewsContract.ItemEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case CHANNEL:
                rowsUpdated = db.update(NewsContract.ChannelEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        String targetTable = null;
        switch (match) {
            case ITEM:
                targetTable = NewsContract.ItemEntry.TABLE_NAME;
                break;
            case CHANNEL:
                targetTable = NewsContract.ChannelEntry.TABLE_NAME;
                break;
        }
        long _id = -1;
        int returnCount = 0;
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                      if (match==ITEM) normalizeDate(value);
                        _id = db.insert(targetTable, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
    }

    private void normalizeDate(ContentValues values) {//TODO: ?Implement?
        // normalize the date value
//        if (values.containsKey(NewsContract.ItemEntry.COLUMN_PUB_DATE)) {
//            long dateValue = values.getAsLong(NewsContract.ItemEntry.COLUMN_PUB_DATE);
//            values.put(NewsContract.ItemEntry.COLUMN_PUB_DATE, NewsContract.normalizeDate(dateValue));
//        }
    }
}
