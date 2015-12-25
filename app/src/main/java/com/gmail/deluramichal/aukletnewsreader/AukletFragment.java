package com.gmail.deluramichal.aukletnewsreader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/**
 * Created by Michal Delura on 2015-12-17.
 */
public abstract class AukletFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int NEWS_LOADER = 0;

    NewsAdapter mNewsAdapter;
    View mRootView;
    ListView mListView;
    ContentResolver mContentResolver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.list_view, container, false);
        mContentResolver = getActivity().getApplicationContext().getContentResolver();

        //Initialize CursorAdapter
        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) mRootView.findViewById(R.id.list_view);
        setNewsAdapterViewType();
        mListView.setAdapter(mNewsAdapter);

        return mRootView;
    }

    protected abstract void setNewsAdapterViewType();

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(NEWS_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        mNewsAdapter.swapCursor(cursor);
        getActivity().findViewById(R.id.loadingPanel).setVisibility(View.GONE);
        if (cursor.getCount() == 0) {
            onLoadFinishedEmptyCursor();
        }
    }

    protected abstract void onLoadFinishedEmptyCursor();

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mNewsAdapter.swapCursor(null);
    }
}
