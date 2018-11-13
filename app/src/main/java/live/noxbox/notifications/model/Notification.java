package live.noxbox.notifications.model;

import android.app.PendingIntent;
import android.content.Context;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import live.noxbox.R;
import live.noxbox.model.NotificationData;
import live.noxbox.model.Profile;

public abstract class Notification {

    long[] vibrate;
    Uri sound;
    RemoteViews contentView;
    boolean isAlertOnce;
    PendingIntent onViewOnClickAction;

    public abstract void showNotification(Context context, Profile profile, NotificationData notification, String channelId);

    NotificationCompat.Builder getNotificationCompatBuilder(Context context, String channelId) {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(vibrate)
                .setContent(contentView)
                .setSound(sound)
                .setOnlyAlertOnce(isAlertOnce)
                .setContentIntent(onViewOnClickAction)
                .setAutoCancel(false);
    }
}
