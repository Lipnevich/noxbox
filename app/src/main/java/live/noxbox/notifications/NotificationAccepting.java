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
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.Firestore;
import live.noxbox.model.Profile;

import static live.noxbox.analitics.BusinessEvent.timeout;
import static live.noxbox.database.AppCache.NONE;
import static live.noxbox.model.Noxbox.isNullOrZero;

public class NotificationAccepting extends Notification {

    private String timeAccepted;

    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        timeAccepted = data.get("timeAccepted");

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());


    }

    @Override
    public void show() {
        if (timeAccepted != null && timeAccepted.length() == 0)
            return;

        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Firestore.readNoxbox(noxboxId, noxbox -> {
            if (isNullOrZero(noxbox.getTimeAccepted())) {
                noxbox.setTimeTimeout(System.currentTimeMillis());
                BusinessActivity.businessEvent(timeout);
                Firestore.createOrUpdateNoxbox(noxbox, NONE, NONE);
            }
        });


    }
}
