package com.gmail.deluramichal.aukletnewsreader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.gmail.deluramichal.aukletnewsreader.data.NewsContract.ChannelEntry;
import com.gmail.deluramichal.aukletnewsreader.data.NewsContract.ItemEntry;

/**
 * Created by Michal Delura on 2015-07-27.
 */
public class NewsDbOpenHelper extends SQLiteOpenHelper {

    //Increment the database version if database schema changed.
    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "news.db";

    public NewsDbOpenHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        // Table to store Channels and URL Sources. There can be only one entry for given URL
        final String SQL_CREATE_CHANNEL_TABLE = "CREATE TABLE " + ChannelEntry.TABLE_NAME + " (" +
                ChannelEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ChannelEntry.COLUMN_SOURCE_URL + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_LINK + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                ChannelEntry.COLUMN_LANGUAGE + " TEXT, " +
                ChannelEntry.COLUMN_CATEGORY + " TEXT, " +
                "UNIQUE (" + ChannelEntry.COLUMN_SOURCE_URL + ") ON CONFLICT IGNORE);";

        final String SQL_CREATE_ITEM_TABLE = "CREATE TABLE " + ItemEntry.TABLE_NAME + " (" +
                ItemEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                // the ID of this item channel
                ItemEntry.COLUMN_CHANNEL_KEY + " INTEGER NOT NULL, " +
                ItemEntry.COLUMN_TITLE + " TEXT, " +
                ItemEntry.COLUMN_LINK + " TEXT, " +
                ItemEntry.COLUMN_CONTENT + " TEXT, " +
                ItemEntry.COLUMN_DESCRIPTION + " TEXT, " +
                ItemEntry.COLUMN_PUB_DATE + " INTEGER, " +
                ItemEntry.COLUMN_IMAGE_SRC + " TEXT, " +
                ItemEntry.COLUMN_IMAGE + " BLOB, " +
                ItemEntry.COLUMN_SYNC_DATE + " INTEGER NOT NULL, " +

                // Set up the Channel column as a foreign key to Channel table.
                " FOREIGN KEY (" + ItemEntry.COLUMN_CHANNEL_KEY + ") REFERENCES " +
                ChannelEntry.TABLE_NAME + " (" + ChannelEntry._ID + "), " +

                // Put new items only if there aren't already there
                " UNIQUE (" + ItemEntry.COLUMN_LINK + ", " +
                ItemEntry.COLUMN_CHANNEL_KEY + ") ON CONFLICT IGNORE);";

        Log.d("xxxTag", SQL_CREATE_CHANNEL_TABLE);
        Log.d("xxxTag", SQL_CREATE_ITEM_TABLE);

        db.execSQL(SQL_CREATE_CHANNEL_TABLE);
        db.execSQL(SQL_CREATE_ITEM_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //Just clean database
        db.execSQL("DROP TABLE IF EXISTS " + ChannelEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + ItemEntry.TABLE_NAME);
        onCreate(db);

    }
}
