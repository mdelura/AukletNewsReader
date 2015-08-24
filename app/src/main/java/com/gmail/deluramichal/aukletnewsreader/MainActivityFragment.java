package com.gmail.deluramichal.aukletnewsreader;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String URL_TVN24 = "http://www.tvn24.pl/najnowsze.xml";
    private final static String URL_REUTERS = "http://feeds.reuters.com/news/artsculture";
    private final static String URL_WIKIPEDIA = "http://en.wikipedia.org/";
    private final static String NEWS_MIME_TYPE = "text/html; charset=utf-8";
    private final static String DEBUG_TAG = MainActivity.class.getSimpleName() + " DEBUG: ";
    private final static boolean USE_IMAGE = true; //TODO: Get from Settings or something
    private static final String ACTIVE_ITEM = "active_item";
    private static final int NEWS_LOADER = 0;
    private static final String[] NEWS_COLUMNS = {
            NewsContract.ItemEntry._ID,
            NewsContract.ItemEntry.COLUMN_TITLE,
            NewsContract.ItemEntry.COLUMN_LINK,
            NewsContract.ItemEntry.COLUMN_CONTENT,
            NewsContract.ItemEntry.COLUMN_DESCRIPTION,
            NewsContract.ItemEntry.COLUMN_PUB_DATE,
            NewsContract.ItemEntry.COLUMN_IMAGE_SRC,
            NewsContract.ItemEntry.COLUMN_IMAGE,
            NewsContract.ItemEntry.COLUMN_CHANNEL_KEY,
            NewsContract.ItemEntry.COLUMN_SYNC_DATE};

    //Indices for NEWS_COLUMNS
    static final int COL_NEWS_ID = 0;
    static final int COL_TITLE = 1;
    static final int COL_LINK = 2;
    static final int COL_CONTENT = 3;
    static final int COL_DESCRIPTION = 4;
    static final int COL_PUB_DATE = 5;
    static final int COL_IMAGE_SRC = 6;
    static final int COL_IMAGE = 7;
    static final int COL_CHANNEL_KEY = 8;
    static final int COL_SYNC_DATE = 9;

    private NewsAdapter mNewsAdapter;//TODO: Implement adapter
    private int mPosition;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.list_view, container, false);
        
        //Initialize CursorAdapter
        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        mNewsAdapter.setUseImage(USE_IMAGE);
        // Get a reference to the ListView, and attach this adapter to it.
        ListView listView = (ListView) rootView.findViewById(R.id.list_view);
        listView.setAdapter(mNewsAdapter);

        if (null !=savedInstanceState && savedInstanceState.containsKey(ACTIVE_ITEM)) {
            mPosition = savedInstanceState.getInt(ACTIVE_ITEM);
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    //TODO: *5 Intent to go to link
                }
            }});    
        
        //TODO: *3 Add Select channels function, see:
        //http://developer.android.com/training/search/index.html
        //http://developer.android.com/guide/topics/search/search-dialog.html
        //TODO: *4 Try it out!
        //TODO: *6 Add appwidget
        //TODO: *7 Add Settings
        //TODO: 8 Select items visible

        return rootView;


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition!=ListView.INVALID_POSITION){
            outState.putInt(ACTIVE_ITEM, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(NEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

        // Sort order:  Descending, by pub_date.
        String sortOrder = NewsContract.ItemEntry.COLUMN_PUB_DATE + " DESC";
        Uri newsUri = NewsContract.ItemEntry.buildItemUri(i);//TODO: WTF is id?

        return new CursorLoader(getActivity(),
                newsUri,
                NEWS_COLUMNS,
                null,
                null,
                sortOrder);
    }


    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mNewsAdapter.swapCursor(cursor);
        ListView listView = (ListView) getActivity().findViewById(R.id.list_view);
        if (mPosition != ListView.INVALID_POSITION) {
            listView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mNewsAdapter.swapCursor(null);
    }
}
