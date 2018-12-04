package live.noxbox.notifications.factory;

import android.content.Context;

import java.util.Map;

import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.Notification;
import live.noxbox.notifications.NotificationAccepting;
import live.noxbox.notifications.NotificationLowBalance;
import live.noxbox.notifications.NotificationMessage;
import live.noxbox.notifications.NotificationMoving;
import live.noxbox.notifications.NotificationPerforming;
import live.noxbox.notifications.NotificationPhotoInvalid;
import live.noxbox.notifications.NotificationPhotoValid;
import live.noxbox.notifications.NotificationPhotoValidationProgress;
import live.noxbox.notifications.NotificationRequesting;
import live.noxbox.notifications.NotificationUploadingProgress;

public abstract class NotificationFactory {

    public static Notification buildNotification(Context context, Profile profile, Map<String, String> data) {
        NotificationType type = NotificationType.valueOf(data.get("type"));
        switch (type) {
            case requesting:
                return new NotificationRequesting(context, profile, data);
            case accepting:
                return new NotificationAccepting(context, profile, data);
            case performing:
                return new NotificationPerforming(context, profile, data);
            case message:
                return new NotificationMessage(context, profile, data);
            case moving:
                return new NotificationMoving(context, profile, data);
            case lowBalance:
                return new NotificationLowBalance(context, profile, data);
            case verifyPhoto:
            case supplierCanceled:
            case demanderCanceled:

            case completed:

            case photoUploadingProgress:
                return new NotificationUploadingProgress(context, profile, data);
            case photoValid:
                return new NotificationPhotoValid(context, profile, data);
            case photoInvalid:
                return new NotificationPhotoInvalid(context, profile, data);
            case photoValidationProgress:
                return new NotificationPhotoValidationProgress(context, profile, data);

            default:
                throw new UnsupportedOperationException();
        }
    }

}
