package live.noxbox.tools;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class NotificationService extends JobService {
    private static final String TAG = "NotificationService.class";

    public NotificationService() {
    }

    @Override
    public boolean onStartJob(JobParameters params) {
        Log.e(TAG, "onStartJob()");
//        ProfileStorage.readProfile(new Task<Profile>() {
//            @Override
//            public void execute(Profile profile) {
//                NotificationType.showPerformingNotificationInBackground(getApplicationContext(), profile, new Notification()
//                        .setType(NotificationType.performing)
//                        .setTime(START_TIME)
//                        .setPrice(decimalFormat.format(totalMoney)));
//            }
//        });
        doInBackground(params);
        return true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Log.e(TAG, "onStopJob()");
        return true;
    }

    private static void doInBackground(final JobParameters jobParameters) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (int i = 10; i > 0; i--) {
                    Log.e(TAG, "run()");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                Log.e(TAG, "Job finished");
            }
        }).start();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Log.e(TAG, "onTaskRemoved");
            }
        }).start();
        Log.e(TAG, "onTaskRemoved");
    }

    @Override
    public void onDestroy() {
        Log.e(TAG, "onDestroy");
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.e(TAG, "onUnbind");
        return super.onUnbind(intent);
    }

    public class RestartServiceReceiver extends BroadcastReceiver {

        private static final String TAG = "RestartServiceReceiver";

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e(TAG, "onReceive");
        }
    }

}
