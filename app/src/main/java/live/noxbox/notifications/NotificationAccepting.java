package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;

public class NotificationAccepting extends Notification {

    private long timeRequesting;

    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        timeRequesting = Long.valueOf(notificationTime);
        vibrate = null;
        sound = getSound(context);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.countDownTime, notificationTime);
        contentView.setTextViewText(R.id.title, context.getResources().getString(type.getTitle()));

        isAlertOnce = true;
        onViewOnClickAction = null;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();

        getNotificationService(context).notify(type.getGroup(), builder.build());
        //TODO (VL Оставляю доделать это завтрашнему себе VL) начиная отсчёт с timeRequesting время обратного отсчёта на двух устройствах не синхронны
        long timePassed = System.currentTimeMillis() - timeRequesting;
        final long totalTime = (REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - timePassed) / 1000;
        runnable = new Runnable() {
            @Override
            public void run() {
                isThreadWorked = true;
                for (long i = totalTime; i >= 0; i--) {
                    contentView.setTextViewText(R.id.countDownTime, String.valueOf(i));
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_up);
                    builder.setContent(contentView);
                    updateNotification(context, builder);

                    if (!isThreadWorked || i <= 0) {
                        //TODO (vl) изменять timeTimeout
                        thread.interrupt();
                        removeNotificationByGroup(context, type.getGroup());
                        return;
                    }

                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_down);
                    builder.setContent(contentView);
                    updateNotification(context, builder);

                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_up);
                    builder.setContent(contentView);
                    updateNotification(context, builder);
                    try {
                        Thread.sleep(333);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                }
            }
        };

        thread = new Thread(runnable);
        thread.start();

    }


}
