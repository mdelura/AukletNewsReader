package com.gmail.deluramichal.aukletnewsreader.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Michal Delura on 2015-07-27.
 */
public class NewsContract {

    public static final String CONTENT_AUTHORITY = "com.gmail.deluramichal.aukletnewsreader";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://com.gmail.deluramichal.aukletnewsreader");

    public static final String PATH_CHANNEL = "channel";
    public static final String PATH_ITEM = "item";

    public static final class ChannelEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CHANNEL).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHANNEL;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CHANNEL;

        public static final String TABLE_NAME = PATH_CHANNEL;

        //Columns
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_DESCRIPTION = "description";

        //Channel source URL
        public static final String COLUMN_SOURCE_URL = "source_url";

        //Optional RSS channel elements
        public static final String COLUMN_LANGUAGE = "language";
        public static final String COLUMN_CATEGORY = "category";

        public static final String[] CHANNEL_COLUMNS = {
                _ID,
                COLUMN_TITLE,
                COLUMN_LINK,
                COLUMN_DESCRIPTION,
                COLUMN_SOURCE_URL,
                COLUMN_LANGUAGE,
                //        NewsContract.ChannelEntry.COLUMN_CATEGORY
        };
        //Indices for NEWS_COLUMNS
        public static final int COL_CHANNEL_ID = 0;
        public static final int COL_TITLE = 1;
        public static final int COL_LINK = 2;
        public static final int COL_DESCRIPTION = 3;
        public static final int COL_SOURCE_URL = 4;
        public static final int COL_LANGUAGE = 5;
        //public static final int COL_CATEGORY = 6;


        public static Uri buildChannelUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class ItemEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ITEM).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEM;

        public static final String TABLE_NAME = PATH_ITEM;

        //Columns
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_LINK = "link";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_PUB_DATE = "pub_date";

        //Source of thumbnail or image store
        public static final String COLUMN_IMAGE_SRC = "image_src";
        public static final String COLUMN_IMAGE = "image";

        //Channel key & sync date
        public static final String COLUMN_CHANNEL_KEY = "channel_id";
        public static final String COLUMN_SYNC_DATE = "sync_date";

        //Sort by PUB_DATE
        public static final String SORT_PUB_DATE_DESC = COLUMN_PUB_DATE + " DESC";

        public static final String[] NEWS_COLUMNS = {
                _ID,
                COLUMN_TITLE,
                COLUMN_LINK,
                COLUMN_CONTENT,
                COLUMN_DESCRIPTION,
                COLUMN_PUB_DATE,
                COLUMN_IMAGE_SRC,
                COLUMN_IMAGE,
                COLUMN_CHANNEL_KEY,
                COLUMN_SYNC_DATE};
        //Indices for NEWS_COLUMNS
        public static final int COL_NEWS_ID = 0;
        public static final int COL_TITLE = 1;
        public static final int COL_LINK = 2;
        public static final int COL_CONTENT = 3;
        public static final int COL_DESCRIPTION = 4;
        public static final int COL_PUB_DATE = 5;
        public static final int COL_IMAGE_SRC = 6;
        public static final int COL_IMAGE = 7;
        public static final int COL_CHANNEL_KEY = 8;
        public static final int COL_SYNC_DATE = 9;

        public static Uri buildItemUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

}