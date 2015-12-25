package com.gmail.deluramichal.aukletnewsreader;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
public class NewsFragment extends AukletFragment {

    private final static String DEBUG_TAG = NewsFragment.class.getSimpleName() + " DEBUG: ";
    private static final String ACTIVE_ITEM = "active_item";

    private int mPosition;
    private boolean mUseImage;


    public NewsFragment() {
    }

    @Override
    public void onResume() {
        if (mUseImage != Utils.getPreferenceShowNewsImage(getActivity().getApplicationContext())) {
            setNewsAdapterViewType();
        }
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        if (null != savedInstanceState && savedInstanceState.containsKey(ACTIVE_ITEM)) {
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

    @Override
    protected void setNewsAdapterViewType() {
        mUseImage = Utils.getPreferenceShowNewsImage(getActivity().getApplicationContext());
        if (mUseImage) {
            mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_WITH_IMAGE);
        }else
            mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_NO_IMAGE);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        if (mPosition!=ListView.INVALID_POSITION){
            outState.putInt(ACTIVE_ITEM, mPosition);
        }
        super.onSaveInstanceState(outState);
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
        super.onLoadFinished(cursorLoader, cursor);
        //TODO: Error Nullpointer exception
//        if (mPosition != ListView.INVALID_POSITION) {
//            listView.smoothScrollToPosition(mPosition);
//        }
    }

    @Override
    protected void onLoadFinishedEmptyCursor() {
        Intent channelsIntent = new Intent(getActivity().getApplicationContext(),
                ChannelsActivity.class);
        startActivity(channelsIntent);
    }
}
