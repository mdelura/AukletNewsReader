package com.gmail.deluramichal.aukletnewsreader.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

/**
 * Created by Michal Delura on 2015-10-05.
 */
public class NewsWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NewsRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}
