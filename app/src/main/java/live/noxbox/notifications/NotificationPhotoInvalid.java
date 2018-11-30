package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.InvalidAcceptance;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;

public class NotificationPhotoInvalid extends Notification {

    private InvalidAcceptance invalidAcceptance;

    public NotificationPhotoInvalid(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = getVibrate();
        sound = getSound();

        invalidAcceptance = profile.getAcceptance().getInvalidAcceptance();

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_invalid_photo);
        contentView.setTextViewText(R.id.message, context.getResources().getString(invalidAcceptance.getContent()));
        contentView.setImageViewResource(R.id.invalidPhotoImage, invalidAcceptance.getImage());
    }

    @Override
    public void show() {
        NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
