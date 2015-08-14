package com.gmail.deluramichal.aukletnewsreader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Michal Delura on 2015-07-31.
 */
public class AukletSyncService extends Service{
    private static final Object sSyncAdapterLock = new Object();
    private static AukletSyncAdapter sAukletSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("SunshineSyncService", "onCreate - SunshineSyncService");
        synchronized (sSyncAdapterLock) {
            if (sAukletSyncAdapter == null) {
                sAukletSyncAdapter = new AukletSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return sAukletSyncAdapter.getSyncAdapterBinder();
    }
}
