package live.noxbox.notifications;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationSupport extends Notification {

    private String message;

    public NotificationSupport(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        message = data.get("message");

        contentView  = new RemoteViews(context.getPackageName(), R.layout.notification_support);
        contentView.setTextViewText(R.id.content, message);

        onViewOnClickAction = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
