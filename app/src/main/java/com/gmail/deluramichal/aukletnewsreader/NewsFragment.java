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

import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;


/**
 * A placeholder fragment containing a simple view.
 */
public class NewsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private final static String DEBUG_TAG = NewsFragment.class.getSimpleName() + " DEBUG: ";
    private static final String ACTIVE_ITEM = "active_item";
    private static final int NEWS_LOADER = 0;

    private NewsAdapter mNewsAdapter;
    private int mPosition;
    private boolean mUseImage;
    private View mRootView;
    private ListView mListView;


    public NewsFragment() {
    }

    @Override
    public void onResume() {
        if (mUseImage != Utils.getPreferenceShowNewsImage(getActivity().getApplicationContext())) {
            setNewsAdapter();
        }
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mRootView =  inflater.inflate(R.layout.list_view, container, false);

        //Initialize CursorAdapter
        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        // Get a reference to the ListView, and attach this adapter to it.
        mListView = (ListView) mRootView.findViewById(R.id.list_view);
        setNewsAdapter();

        if (null !=savedInstanceState && savedInstanceState.containsKey(ACTIVE_ITEM)) {
            mPosition = savedInstanceState.getInt(ACTIVE_ITEM);
        }
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);

                String url = cursor.getString(NewsContract.ItemEntry.COL_LINK);
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

        return mRootView;
    }

    private void setNewsAdapter() {
        mUseImage = Utils.getPreferenceShowNewsImage(getActivity().getApplicationContext());
        if (mUseImage) {
            mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_WITH_IMAGE);
        }else
            mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_NO_IMAGE);
        mListView.setAdapter(mNewsAdapter);
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
        Uri newsUri = NewsContract.ItemEntry.CONTENT_URI;

        return new CursorLoader(getActivity(),
                newsUri,
                NewsContract.ItemEntry.NEWS_COLUMNS,
                null,
                null,
                NewsContract.ItemEntry.SORT_PUB_DATE_DESC);
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
