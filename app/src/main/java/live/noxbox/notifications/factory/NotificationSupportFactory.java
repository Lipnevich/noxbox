package live.noxbox.notifications.factory;

import android.content.Context;

import live.noxbox.model.NotificationData;
import live.noxbox.notifications.model.Notification;

public class NotificationSupportFactory extends NotificationFactory {
    @Override
    public Notification createNotificationBehavior(Context context, NotificationData notificationData) {
        return null;
    }
}
