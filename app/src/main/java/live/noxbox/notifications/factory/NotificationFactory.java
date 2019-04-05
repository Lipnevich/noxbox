package live.noxbox.notifications.factory;

import android.annotation.TargetApi;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Color;
import android.media.AudioAttributes;
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
import live.noxbox.notifications.NotificationMessage;
import live.noxbox.notifications.NotificationMoving;
import live.noxbox.notifications.NotificationPerforming;
import live.noxbox.notifications.NotificationPhotoInvalid;
import live.noxbox.notifications.NotificationPhotoReject;
import live.noxbox.notifications.NotificationPhotoValid;
import live.noxbox.notifications.NotificationPhotoValidationProgress;
import live.noxbox.notifications.NotificationPhotoVerification;
import live.noxbox.notifications.NotificationRequesting;
import live.noxbox.notifications.NotificationSupport;
import live.noxbox.notifications.NotificationUploadingProgress;

import static live.noxbox.Constants.CHANNEL_ID;
import static live.noxbox.notifications.Notification.getSound;
import static live.noxbox.services.MessagingService.getNotificationService;
import static live.noxbox.tools.VersionOperator.isSdkHighestThan26OrEqual;

public abstract class NotificationFactory {

    public static Notification buildNotification(Context context, Profile profile, Map<String, String> data) {
        NotificationType type = NotificationType.valueOf(data.get("type"));
        createChannel(type, context);
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
            case verifyPhoto:
                return new NotificationPhotoVerification(context, profile, data);
            case canceled:
                return new NotificationCancel(context, profile, data);
            case rejected:
                return new NotificationPhotoReject(context, profile, data);
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


    @TargetApi(Build.VERSION_CODES.O)
    private static void createChannel(NotificationType type, Context context) {
        if (isSdkHighestThan26OrEqual()) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = getNotificationService(context).getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                notificationChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, importance);
                getNotificationService(context).createNotificationChannel(notificationChannel);
            }

            notificationChannel.setLightColor(Color.GREEN);
            notificationChannel.enableLights(true);
            notificationChannel.setShowBadge(false);
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .build();
            notificationChannel.setSound(getSound(type, context), audioAttributes);
        }
    }
}
