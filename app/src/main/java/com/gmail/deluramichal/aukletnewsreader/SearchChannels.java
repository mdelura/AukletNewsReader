package com.gmail.deluramichal.aukletnewsreader;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;
import com.gmail.deluramichal.aukletnewsreader.sync.AukletSyncAdapter;

import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssReader;

public class SearchChannels extends AppCompatActivity {

    private NewsAdapter mAdapter;
    private ListView mListView;
    private ContentResolver mContentResolver;

    String LOG_TAG = SearchChannels.class.getSimpleName();
    private static final String[] CHANNEL_COLUMNS = {
            NewsContract.ChannelEntry._ID,
            NewsContract.ChannelEntry.COLUMN_TITLE,
            NewsContract.ChannelEntry.COLUMN_LINK,
            NewsContract.ChannelEntry.COLUMN_DESCRIPTION,
            NewsContract.ChannelEntry.COLUMN_SOURCE_URL,
            NewsContract.ChannelEntry.COLUMN_LANGUAGE,
    //        NewsContract.ChannelEntry.COLUMN_CATEGORY
    };

    //Indices for NEWS_COLUMNS
    static final int COL_CHANNEL_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_LINK = 2;
    static final int COL_DESCRIPTION = 3;
    static final int COL_SOURCE_URL = 4;
    static final int COL_LANGUAGE = 5;
    //static final int COL_CATEGORY = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_channels);
        mContentResolver = getApplicationContext().getContentResolver();
        mListView = (ListView) this.findViewById(R.id.list_search_channels);
        mAdapter = new NewsAdapter(getApplicationContext(), null, 0,
                NewsAdapter.VIEW_TYPE_CHANNEL_WITH_DESCRIPTION);
        mListView.setAdapter(mAdapter);
        handleIntent(getIntent());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {//TODO: Keep selected items
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            //TODO: *Implement search here... somehow

            //Static mockup channels to read channel info from
            String[] channelUrls = {
                    "http://www.tvn24.pl/najwazniejsze.xml",
                    "http://www.tvn24.pl/wiadomosci-z-kraju,3.xml",
                    "http://www.tvn24.pl/internet-hi-tech-media,40.xml",
                    "http://www.tvn24.pl/wiadomosci-ze-swiata,2.xml",
                    "http://www.tvn24.pl/biznes-gospodarka,6.xml",
                    "http://www.tvn24.pl/kultura-styl,8.xml",
                    "http://www.tvn24.pl/ciekawostki-michalki,5.xml",
                    "http://www.tvn24.pl/warszawa,41.xml",
                    "http://feeds.reuters.com/news/artsculture",
                    "http://feeds.reuters.com/reuters/technologyNews",
                    "http://feeds.reuters.com/Reuters/worldNews",
                    "http://feeds.reuters.com/reuters/oddlyEnoughNews",
                    "http://feeds.reuters.com/reuters/scienceNews",
                    "http://www.filmweb.pl/feed/news/latest"
            };

            //FetchChannelsTask here - get RSS channel info
            if (MainActivity.isOnline(getApplicationContext())) {
                new FetchChannelsTask().execute(channelUrls);
            } else {
                Toast.makeText(getApplicationContext().getApplicationContext(), R.string.noConnection, Toast
                        .LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_select_channels, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_select_channels:
                addSelectedChannels();
                finish();
//                Intent newsIntent = new Intent(getApplicationContext(), MainActivity.class);
//                startActivity(newsIntent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void addSelectedChannels() {
        //Get list of selected items
        if (mListView.getCheckedItemCount() != 0) {
            //List of checked items (key=position, value = checked or not)
            SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
            //Get Cursor from Adapter
            Cursor checkedItemsCursor = mAdapter.getCursor();
            Vector<ContentValues> cVVector = new Vector<>();
            checkedItemsCursor.moveToFirst();
            while (!checkedItemsCursor.isAfterLast()) {
                //...and insert cursor item into a database if position is checked.
                if (checkedItemPositions.get(checkedItemsCursor.getPosition(), false)) {
                    ContentValues channelValues = new ContentValues();
                    channelValues.put(NewsContract.ChannelEntry.COLUMN_TITLE,
                            checkedItemsCursor.getString(COL_TITLE));
                    channelValues.put(NewsContract.ChannelEntry.COLUMN_LINK,
                            checkedItemsCursor.getString(COL_LINK));
                    channelValues.put(NewsContract.ChannelEntry.COLUMN_DESCRIPTION,
                            checkedItemsCursor.getString(COL_DESCRIPTION));
                    channelValues.put(NewsContract.ChannelEntry.COLUMN_SOURCE_URL,
                            checkedItemsCursor.getString(COL_SOURCE_URL));
                    channelValues.put(NewsContract.ChannelEntry.COLUMN_LANGUAGE,
                            checkedItemsCursor.getString(COL_LANGUAGE));
                    //channelValues.put(NewsContract.ChannelEntry.COLUMN_CATEGORY,
                    //        checkedItemsCursor.getString(COL_CATEGORY));
                    Log.d(LOG_TAG, "Add Channel: " + checkedItemsCursor.getString
                            (COL_TITLE));
                    cVVector.add(channelValues);
                }
                checkedItemsCursor.moveToNext();
            }
            ContentValues[] cvArray = new ContentValues[cVVector.size()];
            cVVector.toArray(cvArray);
            Log.d(LOG_TAG, "Inserting new Channels into Database");
            mContentResolver.bulkInsert(NewsContract.ChannelEntry.CONTENT_URI, cvArray);

            Log.d(LOG_TAG, "Perform instant sync");
            AukletSyncAdapter.syncImmediately(getApplicationContext());//Sync for new items
        }
    }

    public class FetchChannelsTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String[] params) {

            MatrixCursor cursor = new MatrixCursor(CHANNEL_COLUMNS);

            for (String channelUrl : params) {

                URL url = null;
                try {
                    url = new URL(channelUrl);
                } catch (MalformedURLException e) {
                    Log.e(e.getClass().getSimpleName(), e.getMessage());
                }
                RssFeed feed = null;
                try {
                    feed = RssReader.read(url);
                    //Add channel to Cursor
                    MatrixCursor.RowBuilder newRow = cursor.newRow();
                    newRow.add(0);
                    newRow.add(feed.getTitle());
                    newRow.add(feed.getLink());
                    newRow.add(feed.getDescription());
                    newRow.add(channelUrl);
                    newRow.add(feed.getLanguage());
                } catch (SAXException e) {
                    Log.e(e.getClass().getSimpleName(), e.getMessage());
                } catch (IOException e) {
                    Log.e(e.getClass().getSimpleName(), e.getMessage());
                }
            }
            return cursor;
        }

        @Override
        protected void onPostExecute(Cursor rssFeeds) {
            if (rssFeeds != null) {
                mAdapter.changeCursor(rssFeeds);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            } else {
                Toast.makeText
                        (getApplicationContext(), R.string.noChannels, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
