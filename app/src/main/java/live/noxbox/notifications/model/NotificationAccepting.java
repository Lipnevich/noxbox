package live.noxbox.notifications.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import live.noxbox.R;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;

import static live.noxbox.tools.MessagingService.getNotificationService;
import static live.noxbox.tools.MessagingService.removeNotifications;

public class NotificationAccepting extends Notification {
    public NotificationAccepting(Context context, NotificationData notification) {
        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.countDownTime, notification.getTime());
        contentView.setTextViewText(R.id.title, context.getResources().getString(notification.getType().getTitle()));
        contentView.setOnClickPendingIntent(R.id.accept, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.AcceptRequestListener.class), 0));
        contentView.setOnClickPendingIntent(R.id.cancel, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.CancelRequestListener.class), 0));

        isAlertOnce = true;
        onViewOnClickAction = null;
    }

    @Override
    public void showNotification(final Context context, final Profile profile, final NotificationData notificationData, final String channelId) {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder(context, channelId);

        getNotificationService(context).notify(notificationData.getType().getIndex(), builder.build());
        //TODO (vl) использовать время с момента timeRequest
        final long totalTime = Long.valueOf(notificationData.getTime()) / 1000;
        new Thread(new Runnable() {
            @Override
            public void run() {
                for (long i = totalTime; i >= 0; i--) {
                    contentView.setTextViewText(R.id.countDownTime, String.valueOf(i));
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_up);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(notificationData.getType().getIndex(), builder.build());
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
                    getNotificationService(context).notify(notificationData.getType().getIndex(), builder.build());

                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_up);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(notificationData.getType().getIndex(), builder.build());
                    try {
                        Thread.sleep(250);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                    contentView.setImageViewResource(R.id.animationMan, R.drawable.request_hend_down);
                    builder.setContent(contentView);
                    getNotificationService(context).notify(notificationData.getType().getIndex(), builder.build());
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
