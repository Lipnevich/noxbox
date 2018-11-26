package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Map;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;
import live.noxbox.tools.Task;

public class NotificationMoving extends Notification {

    private int maxProgress;
    private int progress;
    private NoxboxType noxboxType;


    public NotificationMoving(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        if (profile != null) {
            noxboxType = profile.getCurrent().getType();
        }
        maxProgress = Integer.parseInt(data.get("timeToMeet"));
        progress = 0;

        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_moving);
        contentView.setTextViewText(R.id.time, String.valueOf(maxProgress / 60000).concat("min"));
        contentView.setProgressBar(R.id.progress, maxProgress, progress, false);

        isAlertOnce = true;

        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        Task<Noxbox> task = new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                contentView.setImageViewResource(R.id.noxboxType, noxbox.getType().getImage());

                final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                getNotificationService(context).notify(type.getGroup(), builder.build());

                //TODO (vl) обновлять время без потока
                stateRunnable = new Runnable() {
                    @Override
                    public void run() {
                        isStateMovingThreadWorked = true;
                        int totalTimeInSeconds = maxProgress / 1000;

                        for (int i = totalTimeInSeconds; i >= 0; i--) {
                            contentView.setTextViewText(R.id.time, String.valueOf(i / 60).concat("min"));
                            contentView.setProgressBar(R.id.progress, totalTimeInSeconds, totalTimeInSeconds - i, false);

                            if (!isStateMovingThreadWorked) {
                                stateThread.interrupt();
                                removeNotificationByGroup(context, type.getGroup());
                                return;
                            }

                            updateNotification(context, builder);
                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                Crashlytics.logException(e);
                            }
                        }

                    }
                };
                stateThread = new Thread(stateRunnable);
                stateThread.start();
            }
        };


        if (profile == null) {
            Firestore.readNoxbox(data.get("id"), task);
        } else {
            task.execute(profile.getCurrent());
        }
    }
}
