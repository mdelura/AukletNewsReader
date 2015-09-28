package com.gmail.deluramichal.aukletnewsreader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michal Delura on 2015-08-23.
 */
public class NewsAdapter extends CursorAdapter {

    private int mViewType;
    public static final int VIEW_TYPE_WITH_IMAGE = 0;
    public static final int VIEW_TYPE_NO_IMAGE = 1;
    public static final int VIEW_TYPE_CHANNEL_WITH_DESCRIPTION = 2;

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        mViewType = VIEW_TYPE_WITH_IMAGE;
    }

    public NewsAdapter(Context context, Cursor c, int flags, int viewType) {
        super(context,c, flags);
        mViewType = viewType;
    }

    public void setViewType(int viewType) {
        mViewType = viewType;
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        //return (mViewType = VIEW_TYPE_WITH_IMAGE && getCursor().getBlob(MainActivityFragment
        // .COL_IMAGE)
        // .length!=0) ?
        return mViewType;//TODO: Currently constant for chosen view type. Check for image?
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        switch (viewType) {
            case VIEW_TYPE_WITH_IMAGE:
                layoutId = R.layout.list_item_with_image;
                break;
            case VIEW_TYPE_NO_IMAGE:
                layoutId = R.layout.list_item_no_image;
                break;
            case VIEW_TYPE_CHANNEL_WITH_DESCRIPTION:
                layoutId = R.layout.list_item_channel_with_description;
                break;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        //Common items
        String title = null;
        String description = null;

        int viewType = getItemViewType(cursor.getPosition());
        switch (viewType) {
            case VIEW_TYPE_WITH_IMAGE:
                if (cursor.getBlob(NewsFragment.COL_IMAGE) != null) {
                    byte[] imageBytes = cursor.getBlob(NewsFragment.COL_IMAGE);
                    Bitmap bitmapFromBytes =
                            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    viewHolder.thumbnailView.setImageBitmap(bitmapFromBytes);
                }
                //No break here as rest of the code is the same for these view types
            case VIEW_TYPE_NO_IMAGE:
                //Set itemId, title, description, link
                title = cursor.getString(NewsFragment.COL_TITLE);
                description = cursor.getString(NewsFragment.COL_DESCRIPTION);
                // Read date from cursor
                long dateInMillis = cursor.getLong(NewsFragment.COL_PUB_DATE);
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                viewHolder.dateView.setText(simpleDateFormat.format(new Date(dateInMillis)));
                break;
            case VIEW_TYPE_CHANNEL_WITH_DESCRIPTION:
                title = cursor.getString(SearchChannels.COL_TITLE);
                description = cursor.getString(SearchChannels.COL_DESCRIPTION);
                break;
        }

        //Common items
        viewHolder.titleView.setText(title);
        viewHolder.descriptionView.setText(description);

    }

/**
 * Cache of the children views for a news list item.
 */
public static class ViewHolder {
    public final ImageView thumbnailView;
    public final TextView titleView;
    public final TextView dateView;
    public final TextView descriptionView;

    public ViewHolder(View view) {
        thumbnailView = (ImageView) view.findViewById(R.id.list_item_thumbnail);
        titleView = (TextView) view.findViewById(R.id.list_item_title);
        dateView = (TextView) view.findViewById(R.id.list_item_date);
        descriptionView = (TextView) view.findViewById(R.id.list_item_description);
    }
}
}
