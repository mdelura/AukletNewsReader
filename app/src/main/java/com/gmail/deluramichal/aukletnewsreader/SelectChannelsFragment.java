package com.gmail.deluramichal.aukletnewsreader;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
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

/**
 * Created by Michal Delura.
 */
public class SelectChannelsFragment extends AukletFragment {

    String LOG_TAG = SelectChannelsFragment.class.getSimpleName();
    public SelectChannelsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mListView.setDrawSelectorOnTop(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        getChannelsList();

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().invalidateOptionsMenu();
            }
        });

        return mRootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getChannelsList();
    }

    @Override
    protected void setNewsAdapterViewType() {
        mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_CHANNEL_WITH_DESCRIPTION);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_search_channels, menu);
        MenuItem addChannelsMenuItem = menu.findItem(R.id.action_add_channels);
        if (mListView.getCheckedItemCount() != 0) {
            addChannelsMenuItem.setVisible(true);
        } else
            addChannelsMenuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_add_channels:
                addSelectedChannels();
                getActivity().finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getChannelsList() {
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
                "http://www.filmweb.pl/feed/news/latest",
                "http://feeds.feedburner.com/Mobilecrunch",
                "http://feeds.feedburner.com/crunchgear",
                "http://feeds.gawker.com/gizmodo/full",
                "http://rss.cnn.com/rss/edition_technology.rss",
                "http://rss.cnn.com/rss/edition_space.rss"
        };
        //FetchChannelsTask here - get RSS channel info
        if (Utils.isOnline(getActivity().getApplicationContext())) {
            new FetchChannelsTask().execute(channelUrls);
        } else {
            Toast.makeText(getActivity().getApplicationContext().getApplicationContext(),
                    R.string.noConnection, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    protected void onLoadFinishedEmptyCursor() {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.no_channels_selected);
    }

    private void addSelectedChannels() {
        //List of checked items (key=position, value = checked or not)
        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
        //Get Cursor from Adapter
        Cursor checkedItemsCursor = mNewsAdapter.getCursor();
        Vector<ContentValues> cVVector = new Vector<>();
        checkedItemsCursor.moveToFirst();
        while (!checkedItemsCursor.isAfterLast()) {
            //...and insert cursor item into a database if position is checked.
            if (checkedItemPositions.get(checkedItemsCursor.getPosition(), false)) {
                ContentValues channelValues = new ContentValues();
                channelValues.put(NewsContract.ChannelEntry.COLUMN_TITLE,
                        checkedItemsCursor.getString(NewsContract.ChannelEntry.COL_TITLE));
                channelValues.put(NewsContract.ChannelEntry.COLUMN_LINK,
                        checkedItemsCursor.getString(NewsContract.ChannelEntry.COL_LINK));
                channelValues.put(NewsContract.ChannelEntry.COLUMN_DESCRIPTION,
                        checkedItemsCursor.getString(NewsContract.ChannelEntry.COL_DESCRIPTION));
                channelValues.put(NewsContract.ChannelEntry.COLUMN_SOURCE_URL,
                        checkedItemsCursor.getString(NewsContract.ChannelEntry.COL_SOURCE_URL));
                channelValues.put(NewsContract.ChannelEntry.COLUMN_LANGUAGE,
                        checkedItemsCursor.getString(NewsContract.ChannelEntry.COL_LANGUAGE));
                //channelValues.put(NewsContract.ChannelEntry.COLUMN_CATEGORY,
                //        checkedItemsCursor.getString(COL_CATEGORY));
                Log.d(LOG_TAG, "Add Channel: " + checkedItemsCursor.getString
                        (NewsContract.ChannelEntry.COL_TITLE));
                cVVector.add(channelValues);
            }
            checkedItemsCursor.moveToNext();
        }
        ContentValues[] cvArray = new ContentValues[cVVector.size()];
        cVVector.toArray(cvArray);
        Log.d(LOG_TAG, "Inserting new Channels into Database");
        mContentResolver.bulkInsert(NewsContract.ChannelEntry.CONTENT_URI, cvArray);

        Log.d(LOG_TAG, "Perform instant sync");
        AukletSyncAdapter.syncImmediately(getActivity().getApplicationContext());//Sync for new items
    }

    public class FetchChannelsTask extends AsyncTask<String, Void, Cursor> {

        @Override
        protected Cursor doInBackground(String[] params) {//TODO: To SearchFragment Loader?

            MatrixCursor cursor = new MatrixCursor(NewsContract.ChannelEntry.CHANNEL_COLUMNS);

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
                mNewsAdapter.changeCursor(rssFeeds);
                mRootView.findViewById(R.id.loadingPanel).setVisibility(View.GONE);
            } else {
                Toast.makeText
                        (getActivity().getApplicationContext(), R.string.noChannels,
                                Toast.LENGTH_SHORT).show();
            }
        }
    }

}
