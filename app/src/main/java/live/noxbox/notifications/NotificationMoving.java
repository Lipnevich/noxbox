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
import live.noxbox.notifications.factory.Notification;
import live.noxbox.tools.Task;

import static live.noxbox.tools.LocationCalculator.getTimeInMillisBetweenUsers;

public class NotificationMoving extends Notification {

    private int maxProgressInSeconds;
    private int progressInSeconds;


    public NotificationMoving(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = null;
        sound = null;

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
                removeNotificationByGroup(context, type.getGroup());
                if (maxProgressInSeconds == 0) {
                    maxProgressInSeconds = (int) getTimeInMillisBetweenUsers(noxbox.getOwner().getPosition(), noxbox.getParty().getPosition(), noxbox.getProfileWhoComes().getTravelMode()) / 1000;
                }

                progressInSeconds =  maxProgressInSeconds - ((int) getTimeInMillisBetweenUsers(noxbox.getOwner().getPosition(), noxbox.getParty().getPosition(), noxbox.getProfileWhoComes().getTravelMode()) / 1000);

                contentView.setImageViewResource(R.id.noxboxTypeImage, noxbox.getType().getImage());
                contentView.setTextViewText(R.id.time, String.valueOf(progressInSeconds / 60 + 1).concat("min"));
                //contentView.setProgressBar(R.id.progress, maxProgressInSeconds, progressInSeconds, false);

                final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                getNotificationService(context).notify(type.getGroup(), builder.build());
            }
        };


        if (profile == null) {
            Firestore.readNoxbox(data.get("id"), task);
        } else {
            task.execute(profile.getCurrent());
        }
    }
}
