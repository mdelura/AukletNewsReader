package com.gmail.deluramichal.aukletnewsreader;

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
}
