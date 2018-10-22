package live.noxbox.pages;

import android.app.Service;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

public class AvailableNoxboxesService extends Service {
    private final IBinder binder = new LocalBinder();


    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }



    @Override
    public void unbindService(ServiceConnection conn) {
        super.unbindService(conn);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class LocalBinder extends Binder {
        AvailableNoxboxesService getService() {
            return new AvailableNoxboxesService();
        }
    }


}
