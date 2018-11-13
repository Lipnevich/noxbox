package live.noxbox.tools;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.state.AppCache;

import static live.noxbox.Configuration.START_TIME;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

public class NotificationService extends IntentService {
    private static final String TAG = "NotificationService";

    public NotificationService() {
        super(TAG);
    }

    public static Intent newService(Context context) {
        return new Intent(context, NotificationService.class);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand()");
        return super.onStartCommand(intent, flags, startId);
    }

    private static void doInBackground() {
        for (int i = 10; i > 0; i--) {
            Log.d(TAG, "run()");
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Log.d(TAG, "Job finished");
        }
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.d(TAG, "onTaskRemoved()");
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                NotificationType.showPerformingNotification(getApplicationContext(), profile, new NotificationData()
                        .setType(NotificationType.performing)
                        .setTime(START_TIME)
                        .setPrice(decimalFormat.format(totalMoney)));
            }
        });
    }

}
