package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;

public class NotificationMoving extends Notification {
    private int maxProgress;
    private int progress;

    public NotificationMoving(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        maxProgress = Integer.parseInt(data.get("maxProgress"));
        progress = Integer.parseInt(data.get("progress"));

        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_moving);
        contentView.setTextViewText(R.id.time, String.valueOf((Long.parseLong(notificationTime)) / 60000).concat("min"));
        contentView.setProgressBar(R.id.progress, maxProgress, progress, false);
        contentView.setOnClickPendingIntent(R.id.navigation, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.NavigationButtonListener.class), 0));

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void show() {
        NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }

}
