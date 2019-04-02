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
import live.noxbox.model.Profile;

import static live.noxbox.tools.Events.inForeground;

public class NotificationAccepting extends Notification {

    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);

        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());

    }

    @Override
    public void show() {
        if (inForeground()) return;

        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
