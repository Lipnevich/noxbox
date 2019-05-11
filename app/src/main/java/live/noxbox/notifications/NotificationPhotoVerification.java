package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.activities.ConfirmationActivity;
import live.noxbox.model.Profile;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.Noxbox.isNullOrZero;

/**
 * Created by Vladislaw Kravchenok on 11.03.2019.
 */
public class NotificationPhotoVerification extends Notification {


    public NotificationPhotoVerification(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_photo_validation_progress);
        contentView.setTextViewText(R.id.content, context.getResources().getString(type.getContent()));

        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, ConfirmationActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        isAutoCancel = true;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        if (profile() != null) {
            if ((profile().getCurrent().getMe(profile().getId()).equals(profile().getCurrent().getOwner())
                        && isNullOrZero(profile().getCurrent().getTimeOwnerVerified()))
                    || (profile().getCurrent().getMe(profile().getId()).equals(profile().getCurrent().getOwner())
                        && isNullOrZero(profile().getCurrent().getTimePartyVerified()))) {
                NotificationCompat.Builder builder = getNotificationCompatBuilder();
                getNotificationService(context).notify(type.getGroup(), builder.build());
            }
        }
    }
}
