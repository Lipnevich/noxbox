package live.noxbox.notifications.factory;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
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
    protected boolean isAutoCancel = false;

    protected Context context;
    protected Profile profile;
    protected NotificationType type;
    protected String notificationTime;
    protected Map<String, String> data;

    public Notification(Context context, Profile profile, Map<String, String> data) {
        this.context = context;
        this.profile = profile;
        this.data = data;
        type = NotificationType.valueOf(data.get("type"));
        notificationTime = data.get("time");
    }

    public void show() {
    }

    protected NotificationCompat.Builder getNotificationCompatBuilder() {
        return new NotificationCompat.Builder(context, Configuration.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(vibrate)
                .setContent(contentView)
                .setSound(sound)
                .setOnlyAlertOnce(isAlertOnce)
                .setContentIntent(onViewOnClickAction)
                .setAutoCancel(isAutoCancel);
    }
    protected NotificationManager getNotificationService(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public void removeNotifications(Context context) {
        MessagingService.getNotificationService(context).cancelAll();
    }

    public void updateNotification(Context context, NotificationCompat.Builder builder) {
        if (type != NotificationType.message)
            builder.setCustomContentView(contentView);

        getNotificationService(context).notify(type.getGroup(), builder.build());
    }

    public Uri getSound(Context context) {
        int sound = R.raw.push;
//        if (type == requesting) {
//            sound = R.raw.requested;
//        }

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }

}
