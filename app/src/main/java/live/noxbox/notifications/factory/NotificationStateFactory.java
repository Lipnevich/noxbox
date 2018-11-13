package live.noxbox.notifications.factory;

import android.content.Context;

import live.noxbox.model.NotificationData;
import live.noxbox.notifications.model.Notification;
import live.noxbox.notifications.model.NotificationAccepting;
import live.noxbox.notifications.model.NotificationPerforming;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;

public class NotificationStateFactory extends NotificationFactory {

    @Override
    public Notification createNotificationBehavior(Context context, NotificationData notificationData) {
        Notification readyNotification = null;

        switch (notificationData.getType()) {
            case requesting:
            case accepting: {
                notificationData.setTime(String.valueOf(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS));
                readyNotification = new NotificationAccepting(context, notificationData);
                break;
            }
            case moving:
            case confirm:
            case verifyPhoto:
            case supplierCanceled:
            case demanderCanceled:
            case performing: {
                readyNotification = new NotificationPerforming(context, notificationData);
                break;
            }
            case lowBalance:
            case completed:


        }
        return readyNotification;
    }
}
