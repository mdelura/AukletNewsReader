package com.gmail.deluramichal.aukletnewsreader;


import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.CursorLoader;
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

import com.gmail.deluramichal.aukletnewsreader.data.NewsContract;

/**
 * Created by Michal Delura.
 */
public class ChannelsFragment extends AukletFragment {

    String LOG_TAG = ChannelsFragment.class.getSimpleName();

    public ChannelsFragment() {
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mListView.setDrawSelectorOnTop(false);
        mListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                getActivity().invalidateOptionsMenu();
            }
        });

        return mRootView;
    }

    @Override
    protected void setNewsAdapterViewType() {
        mNewsAdapter.setViewType(NewsAdapter.VIEW_TYPE_CHANNEL_WITH_DESCRIPTION);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_channels_fragment, menu);
        MenuItem removeChannelsMenuItem = menu.findItem(R.id.action_remove_channels);
        if (mListView.getCheckedItemCount() != 0) {
            removeChannelsMenuItem.setVisible(true);
        } else
            removeChannelsMenuItem.setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch (id) {
            case R.id.action_remove_channels:
                removeSelectedChannels();
                item.setVisible(false);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void removeSelectedChannels() {
        //List of checked items (key=position, value = checked or not)
        SparseBooleanArray checkedItemPositions = mListView.getCheckedItemPositions();
        //Get Cursor from Adapter
        Cursor checkedItemsCursor = mNewsAdapter.getCursor();

        checkedItemsCursor.moveToFirst();
        while (!checkedItemsCursor.isAfterLast()) {
            //...and delete channel from a database if position is checked.
            if (checkedItemPositions.get(checkedItemsCursor.getPosition(), false)) {
                Log.d(LOG_TAG, "Removing channel " +
                        checkedItemsCursor.getInt(NewsContract.ChannelEntry.COL_CHANNEL_ID) +
                        " " +
                        checkedItemsCursor.getString(NewsContract.ChannelEntry.COL_TITLE));
                mContentResolver.delete(
                        NewsContract.ChannelEntry.CONTENT_URI,
                        NewsContract.ChannelEntry._ID + "= ?",
                        new String[]
                                {String.valueOf(checkedItemsCursor.getInt(
                                        NewsContract.ChannelEntry.COL_CHANNEL_ID))});
            }
            checkedItemsCursor.moveToNext();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        Uri channelsUri = NewsContract.ChannelEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),
                channelsUri,
                NewsContract.ChannelEntry.CHANNEL_COLUMNS,
                null,
                null,
                NewsContract.ChannelEntry.SORT_CHANNEL_TITLE_ASC);
    }

    @Override
    protected void onLoadFinishedEmptyCursor() {
        TextView textView = new TextView(getActivity());
        textView.setText(R.string.no_channels_selected);
    }
}
