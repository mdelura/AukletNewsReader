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

import java.util.Date;

/**
 * Created by Michal Delura on 2015-08-23.
 */
public class NewsAdapter extends CursorAdapter {

    private boolean mUseImage;
    private static final int VIEW_TYPE_WITH_IMAGE = 0;
    private static final int VIEW_TYPE_NO_IMAGE = 1;

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    public void setUseImage(boolean useImage) {
        mUseImage = useImage;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        //return (mUseImage && getCursor().getBlob(MainActivityFragment.COL_IMAGE).length!=0) ?
        return (mUseImage) ?//TODO: Check or implement other
                VIEW_TYPE_WITH_IMAGE : VIEW_TYPE_NO_IMAGE;
    }



    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        int viewType = getItemViewType(cursor.getPosition());
        int layoutId = -1;
        if (viewType == VIEW_TYPE_WITH_IMAGE) {
            layoutId = R.layout.list_item_with_image;
        } else if (viewType==VIEW_TYPE_NO_IMAGE) {
            layoutId = R.layout.list_item_no_image;
        }
        View view = LayoutInflater.from(context).inflate(layoutId, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();
        // Read news image ID from cursor
        viewHolder.newsId = cursor.getInt(NewsFragment.COL_NEWS_ID);
        viewHolder.channelId = cursor.getInt(NewsFragment.COL_CHANNEL_KEY);
        viewHolder.newsLink = cursor.getString(NewsFragment.COL_LINK);

        //Set title
        String title = cursor.getString(NewsFragment.COL_TITLE);
        viewHolder.titleView.setText(title);

        //Set image//TODO: Check if zero-length byte array or something
        if (getItemViewType(cursor.getPosition())==VIEW_TYPE_WITH_IMAGE) {
            byte[] imageBytes = cursor.getBlob(NewsFragment.COL_IMAGE);
            Bitmap bitmapFromBytes =
                    BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
            viewHolder.thumbnailView.setImageBitmap(bitmapFromBytes);
            viewHolder.thumbnailView.setContentDescription(title);
        }

        // Read date from cursor
        long dateInMillis = cursor.getLong(NewsFragment.COL_PUB_DATE);
        viewHolder.dateView.setText(
                new Date(dateInMillis).toString());
        viewHolder.descriptionView.setText(cursor.getString(NewsFragment.COL_DESCRIPTION));

    }

    /**
     * Cache of the children views for a news list item.
     */
    public static class ViewHolder {
        public int newsId;
        public String newsLink;
        public int channelId;
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
