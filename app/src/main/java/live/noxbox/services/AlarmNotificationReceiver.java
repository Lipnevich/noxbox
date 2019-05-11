package live.noxbox.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;

import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.tools.Task;

import static android.content.Context.ALARM_SERVICE;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.services.MessagingService.inForeground;
import static live.noxbox.tools.DateTimeFormatter.time;

/**
 * Created by Vladislaw Kravchenok on 10.05.2019.
 */
public class AlarmNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmReceiver";
    public static final String KEY = "lastNotificationUpdateTimeInMillis";

    private AlarmManager alarmManager;
    private String currentNoxboxId;
    private String profileId;
    private long lastNotificationUpdateTimeInMillis;
    private long nextNotificationUpdateTimeInMillis;
    private static final int DEFAULT_INTERVAL_IN_MILLIS = 6000;

    @Override
    public void onReceive(Context context, Intent intent) {
        currentNoxboxId = intent.getStringExtra("noxboxId");
        profileId = intent.getStringExtra("profileId");
        lastNotificationUpdateTimeInMillis = intent.getLongExtra(KEY, 0);
        nextNotificationUpdateTimeInMillis = lastNotificationUpdateTimeInMillis + DEFAULT_INTERVAL_IN_MILLIS;
        Log.d(TAG, "" + time(lastNotificationUpdateTimeInMillis));

        Firestore.readNoxbox(currentNoxboxId, new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                if (inForeground()) return;
                Intent intent = createNotificationMovingIntent(context, nextNotificationUpdateTimeInMillis, profileId, currentNoxboxId);
                PendingIntent pIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

                alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);


                if ((!isNullOrZero(noxbox.getTimeOwnerVerified()) && !isNullOrZero(noxbox.getTimePartyVerified()))
                        || !isNullOrZero(noxbox.getTimeCanceledByOwner())
                        || !isNullOrZero(noxbox.getTimeCanceledByParty())
                        || !isNullOrZero(noxbox.getTimeOwnerRejected())
                        || !isNullOrZero(noxbox.getTimePartyRejected())
                        || noxbox.getFinished()
                        || inForeground()) {
                    Log.e(TAG, "alarm manager will be cancel");
                    alarmManager.cancel(pIntent);
                    return;
                }

                HashMap<String, String> data = new HashMap<>();
                data.put("profileId", profileId);
                NotificationFactory.buildNotification(context, null, data).setSilent(true).show();

                if (alarmManager != null) {
                    alarmManager.set(AlarmManager.RTC, nextNotificationUpdateTimeInMillis, pIntent);
                }

            }
        });
    }

    private static final String ACTION = "updateNotificaiton";

    public static Intent createNotificationMovingIntent(Context context, long nextNotificationUpdateTimeInMillis, String profileId, String noxboxId) {
        Intent intent = new Intent(context, AlarmNotificationReceiver.class);
        intent.setAction(TAG);
        intent.putExtra("profileId", profileId);
        intent.putExtra("noxboxId", noxboxId);
        intent.putExtra(KEY, nextNotificationUpdateTimeInMillis);
        return intent;
    }
}
