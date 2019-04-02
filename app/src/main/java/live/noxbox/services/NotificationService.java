package live.noxbox.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.Map;

import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.notifications.factory.NotificationFactory;

/**
 * Created by Vladislaw Kravchenok on 02.04.2019.
 */
public class NotificationService extends Service {

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        AppCache.readProfile(profile -> {

            Map<String, String> data = NotificationType.fromNoxboxState(profile);
            if (data.isEmpty()) return;

            NotificationFactory.buildNotification(getApplicationContext(), profile, data)
                    .setSilent(true)
                    .show();
            stopSelf();
        });
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
