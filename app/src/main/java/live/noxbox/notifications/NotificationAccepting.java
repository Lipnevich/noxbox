package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Map;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

public class NotificationAccepting extends Notification {

    private String name;

    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

        if(profile != null){
            name = profile.getCurrent().getParty().getName();
        }else {
            name = data.get("name");
        }

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Crashlytics.logException(e);
        }

        Firestore.readNoxbox(noxboxId, new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                if (noxbox.getTimeAccepted() == null) {
                    noxbox.setTimeTimeout(System.currentTimeMillis());
                    Firestore.writeNoxbox(noxbox);
                }
            }
        });
    }
}
