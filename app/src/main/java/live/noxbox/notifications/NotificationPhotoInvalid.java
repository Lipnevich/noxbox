package live.noxbox.notifications;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.InvalidAcceptance;
import live.noxbox.model.Profile;

public class NotificationPhotoInvalid extends Notification {

    private InvalidAcceptance invalidAcceptance;

    public NotificationPhotoInvalid(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        invalidAcceptance = profile.getAcceptance().getInvalidAcceptance();

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_invalid_photo);
        contentView.setTextViewText(R.id.message, context.getString(R.string.photoInvalidContent,
                context.getString(invalidAcceptance.getContent())));
        contentView.setImageViewResource(R.id.invalidPhotoImage, invalidAcceptance.getImage());
    }

    @Override
    public void show() {
        NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
