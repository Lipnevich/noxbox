package live.noxbox.notifications.factory;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.util.MessagingService;

public class Notification {

    //need initialize for NotificationCompat.Builder
    protected long[] vibrate;
    protected Uri sound;
    protected RemoteViews contentView;
    protected boolean isAlertOnce = true;
    protected PendingIntent onViewOnClickAction;
    protected PendingIntent deleteIntent;
    protected boolean isAutoCancel = false;

    protected Context context;
    protected Profile profile;
    protected NotificationType type;
    protected String notificationTime;
    protected Map<String, String> data;

    protected static Thread stateThread;
    protected static Runnable stateRunnable;
    protected static boolean isStateAcceptingThreadWorked;
    protected static boolean isStateMovingThreadWorked;


    public Notification(Context context, Profile profile, Map<String, String> data) {
        this.context = context;
        this.profile = profile;
        this.data = data;
        type = NotificationType.valueOf(data.get("type"));
        notificationTime = data.get("time");

        removeNotifications(context);
        isStateAcceptingThreadWorked = false;
        isStateMovingThreadWorked = false;
    }

    public void show() {
    }

    protected NotificationCompat.Builder getNotificationCompatBuilder() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return new NotificationCompat.Builder(context, Configuration.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setVibrate(vibrate)
                    .setCustomContentView(contentView)
                    .setSound(sound)
                    .setOnlyAlertOnce(isAlertOnce)
                    .setContentIntent(onViewOnClickAction)
                    .setDeleteIntent(deleteIntent)
                    .setAutoCancel(isAutoCancel);
        } else {
            return new NotificationCompat.Builder(context, Configuration.CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setVibrate(vibrate)
                    .setContent(contentView)
                    .setSound(sound)
                    .setOnlyAlertOnce(isAlertOnce)
                    .setContentIntent(onViewOnClickAction)
                    .setDeleteIntent(deleteIntent)
                    .setAutoCancel(isAutoCancel);
        }

    }

    protected static NotificationManager getNotificationService(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    protected static void removeNotifications(Context context) {
        MessagingService.getNotificationService(context).cancelAll();
    }

    protected static void removeNotificationByGroup(Context context, int group) {
        MessagingService.getNotificationService(context).cancel(group);
    }

    protected void updateNotification(Context context, NotificationCompat.Builder builder) {
        if (type != NotificationType.message)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCustomContentView(contentView);
            } else {
                builder.setContent(contentView);
            }

        getNotificationService(context).notify(type.getGroup(), builder.build());
    }

    protected Uri getSound(Context context) {
        int sound = R.raw.push;
//        if (type == requesting) {
//            sound = R.raw.requested;
//        }

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }

    protected PendingIntent createOnDeleteIntent(Context context, int group) {
        Intent intent = new Intent(context, DeleteActionIntent.class);
        return PendingIntent.getBroadcast(context.getApplicationContext(),
                group, intent, 0);
    }


    public static class DeleteActionIntent extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            isStateMovingThreadWorked = false;
            isStateAcceptingThreadWorked = false;
        }
    }

}
