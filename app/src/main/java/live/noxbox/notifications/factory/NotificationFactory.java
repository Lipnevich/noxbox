package live.noxbox.notifications.factory;

import android.content.Context;

import live.noxbox.model.NotificationData;
import live.noxbox.notifications.model.Notification;

public abstract class NotificationFactory {

    public abstract Notification createNotificationBehavior(Context context, NotificationData notificationData);

}
