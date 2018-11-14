package live.noxbox.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.notifications.MessagingService.getNotificationService;
import static live.noxbox.notifications.MessagingService.removeNotifications;

public class NotificationAccepting extends Notification {
    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.countDownTime, notificationTime);
        contentView.setTextViewText(R.id.title, context.getResources().getString(type.getTitle()));
        contentView.setOnClickPendingIntent(R.id.accept, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.AcceptRequestListener.class), 0));
        contentView.setOnClickPendingIntent(R.id.cancel, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.CancelRequestListener.class), 0));

        isAlertOnce = true;
        onViewOnClickAction = null;
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();

        getNotificationService(context).notify(type.getGroup(), builder.build());
        //TODO (vl) использовать время с момента timeRequest
        // TODO (vl) работает ли поток при запущенном приложении
        final long totalTime = REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS / 1000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (long i = totalTime; i >= 0; i--) {
                    contentView.setTextViewText(R.id.countDownTime, String.valueOf(i));
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_up);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(type.getGroup(), builder.build());
                    if (i <= 0) {
                        //TODO (vl) remove notification and setTimeTimeout() in current
                        removeNotifications(context);
                        return;
                    }
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_down);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(type.getGroup(), builder.build());

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_up);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(type.getGroup(), builder.build());
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_down);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(type.getGroup(), builder.build());
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                }
            }
        }).start();
    }
}
