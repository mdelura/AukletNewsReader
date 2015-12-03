package com.gmail.deluramichal.aukletnewsreader;

import android.content.Context;
import android.preference.PreferenceManager;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Michal Delura on 2015-11-23.
 */
public class Utils {
    static final SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd-MMM HH:mm");

    public static String dateFormat(long dateInMillis) {
        return SIMPLE_DATE_FORMAT.format(new Date(dateInMillis));
    }

    public static boolean getPreferenceShowNewsImage(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                context.getString(R.string.pref_show_news_image_key),
                Boolean.parseBoolean(context.getString(R.string.pref_show_news_image_default)));
    }
}
