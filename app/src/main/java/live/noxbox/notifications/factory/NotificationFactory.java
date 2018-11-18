package live.noxbox.notifications.factory;

import android.content.Context;

import java.util.Map;

import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.NotificationAccepting;
import live.noxbox.notifications.NotificationLowBalance;
import live.noxbox.notifications.NotificationMessage;
import live.noxbox.notifications.NotificationMoving;
import live.noxbox.notifications.NotificationPerforming;

public abstract class NotificationFactory {

    public static void showNotification(Context context, Profile profile, Map<String, String> data) {
        NotificationType type = NotificationType.valueOf(data.get("type"));
        switch (type) {
            case accepting: new NotificationAccepting(context, profile, data).show(); break;
            case performing: new NotificationPerforming(context, profile, data).show(); break;
            case message: new NotificationMessage(context, profile, data).show(); break;
            case moving: new NotificationMoving(context, profile, data).show(); break;
            case lowBalance: new NotificationLowBalance(context, profile, data).show(); break;
            case verifyPhoto:
            case supplierCanceled:
            case demanderCanceled:

            case completed:

            default:
                throw new UnsupportedOperationException();
        }
    }

}
