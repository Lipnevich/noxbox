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
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.Constants.MAX_MINUTES;
import static live.noxbox.tools.DateTimeFormatter.getFormatTimeFromMillis;
import static live.noxbox.tools.Events.inForeground;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;

public class NotificationMoving extends Notification {

    private int progressInMinutes;
    private Position profileWhoComesPosition;
    private Position profileWhoWaitPosition;
    private NotificationCompat.Builder builder;
    private Task<Noxbox> task = noxbox -> {
        if (noxbox != null && NoxboxState.getState(noxbox, null) != NoxboxState.moving) {
            removeNotificationByGroup(context, type.getGroup());
            return;
        }
        if (builder == null) {
            removeNotificationByGroup(context, type.getGroup());
        }
        String myId = data.get("profileId");
        if (myId == null) return;

        if (noxbox != null) {
            providePositions(noxbox);
            progressInMinutes = ((int) getTimeInMinutesBetweenUsers(
                    profileWhoWaitPosition,
                    profileWhoComesPosition,
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
        if (builder == null) {
            builder = getNotificationCompatBuilder();
        }
        getNotificationService(context).notify(type.getGroup(), builder.build());
    };


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
        Firestore.readNoxbox(noxboxId, task);
    }

    @Override
    public void update(Map<String, String> newData) {
        super.update(newData);
        this.data.put("lat", newData.get("lat"));
        this.data.put("lon", newData.get("lon"));
        Firestore.readNoxbox(noxboxId, task);
    }


    private void providePositions(Noxbox noxbox) {
        profileWhoWaitPosition = noxbox.getProfileWhoWait().getPosition();
        profileWhoComesPosition = getProfileWhoComesPositionFromData(data);
    }

    private Position getProfileWhoComesPositionFromData(Map<String, String> newData) {
        double lat = Double.valueOf(newData.get("lat"));
        double lon = Double.valueOf(newData.get("lon"));
        return new Position(lat, lon);
    }
}
