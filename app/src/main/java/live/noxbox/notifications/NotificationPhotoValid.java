package live.noxbox.notifications;

import android.content.Context;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationPhotoValid extends Notification {
    public NotificationPhotoValid(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

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
