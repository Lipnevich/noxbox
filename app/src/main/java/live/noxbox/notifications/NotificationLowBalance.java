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
import live.noxbox.model.MarketRole;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;

public class NotificationLowBalance extends Notification {
    private String message;

    public NotificationLowBalance(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = null;
        sound = getSound(context);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_low_balance);
        contentView.setTextViewText(R.id.title, context.getResources().getString(type.getTitle()));
        contentView.setTextViewText(R.id.content, context.getResources().getString(type.getContent()));

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        if (profile.getCurrent().getOwner().equals(profile)) {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                message = "supply";
            } else {
                message = "demand";
            }
        } else {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                message = "demand";
            } else {
                message = "supply";
            }
        }
        updateNotification(context, builder);
    }
}
