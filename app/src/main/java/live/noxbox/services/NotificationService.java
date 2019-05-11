package live.noxbox.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import java.util.Map;

import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.model.NoxboxState;
import live.noxbox.notifications.factory.NotificationFactory;

import static live.noxbox.services.AlarmNotificationReceiver.createNotificationMovingIntent;

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

            if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.moving) {
                long nextNotificationUpdateTimeInMillis = System.currentTimeMillis();
                Intent notificationIntent = createNotificationMovingIntent(getApplicationContext(), nextNotificationUpdateTimeInMillis, profile.getCurrent().getId(),profile.getId());
                PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(ALARM_SERVICE);
                if (am != null) {
                    am.set(AlarmManager.RTC, nextNotificationUpdateTimeInMillis, pIntent);
                }
            } else {
                NotificationFactory.buildNotification(getApplicationContext(), profile, data)
                        .setSilent(true)
                        .show();
            }
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
