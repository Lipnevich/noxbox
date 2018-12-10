package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationAccepting extends Notification {

    private long timeRequesting;

    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        timeRequesting = Long.valueOf(notificationTime);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.content, context.getResources().getString(type.getContent()));

        isAlertOnce = true;
        onViewOnClickAction = null;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }


}
