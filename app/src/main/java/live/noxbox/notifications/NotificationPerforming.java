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

public class NotificationPerforming extends Notification {

    private Map<String, String> data;

    public NotificationPerforming(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        this.data = data;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_performing);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


    }

    private NotificationCompat.Builder builder;

    @Override
    public void show() {
        Task<Noxbox> task = new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                if (noxbox != null && noxbox.getTimeCompleted() != null)
                    return;

                builder = getNotificationCompatBuilder();
                getNotificationService(context).notify(type.getGroup(), builder.build());
            }
        };

        if (profile == null) {
            Firestore.readNoxbox(noxboxId, task);
        } else {
            task.execute(profile.getCurrent());
        }
    }

    @Override
    public void update(Map<String, String> data) {
        //contentView.setTextViewText(R.id.stopwatch, data.get("time"));
        //contentView.setTextViewText(R.id.totalPayment, (data.get("price").concat(" ")).concat(Constants.CURRENCY));
        updateNotification(context, builder);
    }
}
