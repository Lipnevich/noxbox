package live.noxbox.notifications.factory;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import java.util.Map;

import live.noxbox.debug.DebugMessage;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.Notification;
import live.noxbox.notifications.NotificationAccepting;
import live.noxbox.notifications.NotificationBalance;
import live.noxbox.notifications.NotificationCancel;
import live.noxbox.notifications.NotificationComplete;
import live.noxbox.notifications.NotificationLowBalance;
import live.noxbox.notifications.NotificationMessage;
import live.noxbox.notifications.NotificationMoving;
import live.noxbox.notifications.NotificationPerforming;
import live.noxbox.notifications.NotificationPhotoInvalid;
import live.noxbox.notifications.NotificationPhotoValid;
import live.noxbox.notifications.NotificationPhotoValidationProgress;
import live.noxbox.notifications.NotificationRequesting;
import live.noxbox.notifications.NotificationSupport;
import live.noxbox.notifications.NotificationUploadingProgress;

import static live.noxbox.Configuration.CHANNEL_ID;
import static live.noxbox.services.MessagingService.getNotificationService;

public abstract class NotificationFactory {

    public static Notification buildNotification(Context context, Profile profile, Map<String, String> data) {
        createChannel(context);
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
            case canceled:
                return new NotificationCancel(context, profile, data);
            case completed:
                return new NotificationComplete(context, profile, data);
            case photoUploadingProgress:
                return new NotificationUploadingProgress(context, profile, data);
            case photoValid:
                return new NotificationPhotoValid(context, profile, data);
            case photoInvalid:
                return new NotificationPhotoInvalid(context, profile, data);
            case photoValidationProgress:
                return new NotificationPhotoValidationProgress(context, profile, data);
            case balance:
                return new NotificationBalance(context, profile, data);
            case support:
                return new NotificationSupport(context, profile, data);
            default:
                DebugMessage.popup(context, "Unknown notification type " + type);
                return new Notification(context, profile, data) {
                    @Override
                    public void show() {

                    }
                };
        }
    }


    private static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = getNotificationService(context).getNotificationChannel(CHANNEL_ID);


            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
                getNotificationService(context).createNotificationChannel(notificationChannel);
            }


        }
    }

}
