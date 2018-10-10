package live.noxbox.tools;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;

import static live.noxbox.Configuration.START_TIME;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.removeTimer;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

public class NotificationService extends Service {
    @Override
    public void onCreate() {
        super.onCreate();
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                NotificationType.showPerformingNotificationInBackground(getApplicationContext(), profile, new Notification()
                        .setType(NotificationType.performing)
                        .setTime(START_TIME)
                        .setPrice(decimalFormat.format(totalMoney)));
            }
        });

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        removeTimer();
        sendBroadcast(new Intent("live.noxbox.RestarterBroadcastReceiver"));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static class RestarterBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            context.startService(new Intent(context, NotificationService.class));
        }
    }
}
