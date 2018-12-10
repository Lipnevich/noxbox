package live.noxbox.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationRequesting extends Notification {

    public NotificationRequesting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_requesting);
        contentView.setOnClickPendingIntent(R.id.cancel, PendingIntent.getBroadcast(context, 0, new Intent(context, CancelRequestListener.class), 0));

        isAlertOnce = true;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());

    }
}
