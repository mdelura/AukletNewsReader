package com.gmail.deluramichal.aukletnewsreader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String DEBUG_TAG = NewsFragment.class.getSimpleName() + " DEBUG: ";
    private final static boolean USE_IMAGE = true; //TODO: Get from Settings or something
    private static final String ACTIVE_ITEM = "active_item";
    private static final int EXPANDED_DESCRIPTION_LINES = 6;
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

    private NewsAdapter mNewsAdapter;
    private int mPosition;

    public NewsFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView =  inflater.inflate(R.layout.list_view, container, false);

        //Initialize CursorAdapter
        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        if (USE_IMAGE) {
            mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_WITH_IMAGE);
        }else
            mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_NO_IMAGE);
        // Get a reference to the ListView, and attach this adapter to it.
        final ListView listView = (ListView) rootView.findViewById(R.id.list_view);
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

                String url = cursor.getString(COL_LINK);
                Uri webPage = Uri.parse(url);
                Intent intent = new Intent(Intent.ACTION_VIEW, webPage);
                if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    Log.d(DEBUG_TAG, "Couldn't call " + webPage.toString() + ", no receiving apps " +
                            "installed!");
                }
            }
        });

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
        Uri newsUri = NewsContract.ItemEntry.CONTENT_URI;

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
        if (cursor.getCount() != 0) {
            getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        }
        //TODO: Error Nullpointer exception
//        if (mPosition != ListView.INVALID_POSITION) {
//            listView.smoothScrollToPosition(mPosition);
//        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mNewsAdapter.swapCursor(null);
    }
}
