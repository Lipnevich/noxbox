package live.noxbox.notifications.factory;

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

public class Notification {

    protected long[] vibrate;
    protected Uri sound;
    protected RemoteViews contentView;
    protected boolean isAlertOnce;
    protected PendingIntent onViewOnClickAction;

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

    public void show() {}

    protected NotificationCompat.Builder getNotificationCompatBuilder() {
        return new NotificationCompat.Builder(context, Configuration.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(vibrate)
                .setContent(contentView)
                .setSound(sound)
                .setOnlyAlertOnce(isAlertOnce)
                .setContentIntent(onViewOnClickAction)
                .setAutoCancel(false);
    }
}
