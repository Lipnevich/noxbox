package live.noxbox.notifications.factory;

import android.content.Context;

import java.util.Map;

import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.NotificationAccepting;
import live.noxbox.notifications.NotificationPerforming;

public class NotificationFactory {

    public static void showNotification(Context context, Profile profile, Map<String, String> data) {
        NotificationType type = NotificationType.valueOf(data.get("type"));
        switch (type) {
            case requesting:
            case accepting: new NotificationAccepting(context, profile, data).show(); break;
            case performing: new NotificationPerforming(context, profile, data).show(); break;
            case moving:
            case confirm:
            case verifyPhoto:
            case supplierCanceled:
            case demanderCanceled:
            case lowBalance:
            case completed:
            default:
                throw new UnsupportedOperationException();
        }
    }

}
