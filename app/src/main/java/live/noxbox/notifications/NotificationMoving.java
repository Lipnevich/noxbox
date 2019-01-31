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
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;

public class NotificationMoving extends Notification {

    private final int maxProgressInMinutes = 15;
    private int progressInMinutes;


    public NotificationMoving(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_moving);

        isAlertOnce = true;

        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        Task<Noxbox> task = new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                if (noxbox != null && (
                        (!isNullOrZero(noxbox.getTimePartyVerified()) && !isNullOrZero(noxbox.getTimeOwnerVerified()))
                                || !isNullOrZero(noxbox.getTimeCompleted())
                                || !isNullOrZero(noxbox.getTimeCanceledByParty())
                                || !isNullOrZero(noxbox.getTimeCanceledByOwner())))
                    return;

                removeNotificationByGroup(context, type.getGroup());
                if (noxbox != null && ((!isNullOrZero(noxbox.getTimeOwnerVerified()) && !isNullOrZero(noxbox.getTimePartyVerified()))
                        || !isNullOrZero(noxbox.getTimeCanceledByOwner()) || !isNullOrZero(noxbox.getTimeCanceledByParty())))
                    return;

                if (noxbox != null && noxbox.getOwner() != null && noxbox.getParty() != null) {
                    progressInMinutes = ((int) getTimeInMinutesBetweenUsers(noxbox.getOwner().getPosition(), noxbox.getParty().getPosition(), noxbox.getProfileWhoComes().getTravelMode()));
                    progressInMinutes = Math.max(1, Math.min(progressInMinutes, maxProgressInMinutes - 1));
                    contentView.setImageViewResource(R.id.noxboxTypeImage, noxbox.getType().getImage());
                    contentView.setTextViewText(R.id.time, String.valueOf(context.getResources().getString(R.string.movement, "" + progressInMinutes)));
                    contentView.setProgressBar(R.id.progress, maxProgressInMinutes, Math.max(maxProgressInMinutes - progressInMinutes, 1), false);
                }
                final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                getNotificationService(context).notify(type.getGroup(), builder.build());
            }
        };


        if (profile == null) {
            Firestore.readNoxbox(noxboxId, task);
        } else {
            task.execute(profile.getCurrent());
        }
    }
}
