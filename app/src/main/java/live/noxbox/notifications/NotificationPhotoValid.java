package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;

public class NotificationPhotoValid extends Notification {
    public NotificationPhotoValid(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = getVibrate();
        sound = getSound();

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_valid_photo);
        contentView.setTextViewText(R.id.message, context.getResources().getString(type.getContent()));

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
