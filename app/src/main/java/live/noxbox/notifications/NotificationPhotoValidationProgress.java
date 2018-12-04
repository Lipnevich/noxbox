package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationPhotoValidationProgress extends Notification {
    public NotificationPhotoValidationProgress(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = getVibrate();
        sound = getSound();

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_photo_validation_progress);
        contentView.setTextViewText(R.id.content, context.getResources().getString(type.getContent()));
    }

    @Override
    public void show() {
        NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
