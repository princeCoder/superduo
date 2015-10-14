package barqsoft.footballscores.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by Prinzly Ngotoum on 9/28/15.
 */
public class FootballSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static FootballSyncAdapter sFootballSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sFootballSyncAdapter == null) {
                sFootballSyncAdapter = new FootballSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sFootballSyncAdapter.getSyncAdapterBinder();
    }
}
