package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import java.util.Map;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.Constants.MAX_MINUTES;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.DateTimeFormatter.getFormatTimeFromMillis;
import static live.noxbox.tools.Events.inForeground;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;

public class NotificationMoving extends Notification {

    private int progressInMinutes;


    public NotificationMoving(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_moving);

        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        if (inForeground() && !silent) return;
        Task<Noxbox> task = noxbox -> {
            if (noxbox != null && (
                    (!isNullOrZero(noxbox.getTimePartyVerified()) && !isNullOrZero(noxbox.getTimeOwnerVerified()))
                            || !isNullOrZero(noxbox.getTimeCompleted())
                            || !isNullOrZero(noxbox.getTimePartyRejected())
                            || !isNullOrZero(noxbox.getTimeOwnerRejected())
                            || !isNullOrZero(noxbox.getTimeCanceledByParty())
                            || !isNullOrZero(noxbox.getTimeCanceledByOwner()))) {

                removeNotificationByGroup(context, type.getGroup());
                return;
            }

            removeNotificationByGroup(context, type.getGroup());
            String myId = data.get("profileId");
            if (myId == null) return;

            if (noxbox != null && noxbox.getOwner() != null && noxbox.getParty() != null) {
                progressInMinutes = ((int) getTimeInMinutesBetweenUsers(
                        noxbox.getPosition(),
                        noxbox.getProfileWhoComes().getPosition(),
                        noxbox.getProfileWhoComes().getTravelMode()));
                String timeTxt = getFormatTimeFromMillis(progressInMinutes * 60000, context.getResources());
                if (noxbox.getProfileWhoComes().getId().equals(myId)) {
                    contentView.setTextViewText(R.id.time, context.getResources().getString(R.string.movementMove, timeTxt));
                } else {
                    contentView.setTextViewText(R.id.time, context.getResources().getString(R.string.movementWait, timeTxt));
                }

                contentView.setImageViewResource(R.id.noxboxTypeImage, noxbox.getType().getImageDemand());
                contentView.setProgressBar(R.id.progress, MAX_MINUTES, Math.max(MAX_MINUTES - progressInMinutes, 1), false);
            }
            final NotificationCompat.Builder builder = getNotificationCompatBuilder();
            getNotificationService(context).notify(type.getGroup(), builder.build());
        };


        if (profile == null) {
            Firestore.readNoxbox(noxboxId, task);
        } else {
            task.execute(profile.getCurrent());
        }

    }
}
