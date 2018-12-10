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
import live.noxbox.model.Profile;

public class NotificationPerforming extends Notification {


    public NotificationPerforming(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);

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
        builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }

    @Override
    public void update(Map<String, String> data) {
        //contentView.setTextViewText(R.id.stopwatch, data.get("time"));
        //contentView.setTextViewText(R.id.totalPayment, (data.get("price").concat(" ")).concat(Configuration.CURRENCY));
        updateNotification(context, builder);
    }
}
