package live.noxbox.model;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import live.noxbox.BuildConfig;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.pages.ChatActivity;
import live.noxbox.profile.ProfileActivity;

import static live.noxbox.Configuration.CURRENCY;

/**
 * Created by nicolay.lipnevich on 13/05/2017.
 */
public enum NotificationType {

    uploadingProgress(0, R.string.uploadingProgressTitle, R.string.uploadingProgressContent),
    photoValidationProgress(0, R.string.noxbox, R.string.photoValidationProgressContent),
    photoValid(0, R.string.noxbox, R.string.photoValidContent),
    photoInvalid(0, R.string.noxbox, R.string.photoInvalidContent),

    balance(1, R.string.balancePushTitle, R.string.balancePushContent),

    requesting(2, R.string.replaceIt, R.string.requestingPushContent),
    accepting(2, R.string.replaceIt, R.string.acceptingPushContent),
    moving(2, R.string.acceptPushTitle, R.string.replaceIt),
    verifyPhoto(2, R.string.replaceIt, R.string.replaceIt),
    performing(2, R.string.replaceIt, R.string.perfirmingPushContent),
    lowBalance(2, R.string.replaceIt, R.string.replaceIt),
    completed(2, R.string.replaceIt, R.string.completedPushContent),
    supplierCanceled(2, R.string.replaceIt, R.string.supplierCanceledPushContent),
    demanderCanceled(2, R.string.replaceIt, R.string.demanderCanceledPushContent),

    refund(3, R.string.replaceIt, R.string.replaceIt),

    message(4, R.string.replaceIt, R.string.replaceIt);

    private int index;
    private int title;
    private int content;

    NotificationType(int id, int title, int content) {
        this.index = id;
        this.title = title;
        this.content = content;
    }

    public int getIndex() {
        return index;
    }

    public NotificationCompat.Builder getBuilder(Context context, String channelId, Notification notification) {
        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(getVibrate(notification))
                .setSound(getSound(context, notification.getType()))
                .setContentTitle(notification.getType().getTitle(context.getResources(), notification))
                .setContentText(notification.getType().getContent(context.getResources(), notification))
                .setContentInfo(notification.getType().getContentInfo(context.getResources(), notification))
                .setStyle(notification.getType().getStyle(context.getResources(), notification))
                .setAutoCancel(!BuildConfig.DEBUG)
                .setOnlyAlertOnce(isAlertOnce(notification.getType()))
                .setContentIntent(getIntent(context, notification));
    }

    public long[] getVibrate(Notification notification) {
        switch (notification.getType()) {
            case uploadingProgress:
            case performing:
                return null;
            default:
                return new long[]{100, 500, 200, 100, 100};
        }
    }

    public Uri getSound(Context context, NotificationType type) {
        if (type == uploadingProgress || type == performing) return null;

        int sound = R.raw.push;
        if (type == requesting) {
            sound = R.raw.requested;
        }

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }

    public String getTitle(Resources resources, Notification notification) {
        if (this == uploadingProgress) return format(resources, title, notification.getProgress());

        return resources.getString(title);
    }

    private String format(Resources resources, int resource, Object... args) {
        return String.format(resources.getString(resource), args);
    }

    public String getContent(Resources resources, Notification notification) {
        if (notification.getType() == requesting)
            return format(resources, content, notification.getName(), notification.getEstimation());
        if (notification.getType() == accepting)
            return format(resources, content, notification.getName(), notification.getEstimation());
        if (notification.getType() == performing)
            return format(resources, content, " ", notification.getTime());
        if (notification.getType() == completed)
            return format(resources, content, notification.getPrice(), CURRENCY);
        if (notification.getType() == uploadingProgress) {
            if (notification.getTime() == null) {
                return format(resources, R.string.uploadingStarted);
            }
            return format(resources, content, notification.getTime());
        }
        if (notification.getType() == photoInvalid) {
            return format(resources, content, resources.getString(notification.getInvalidAcceptance().getCorrectionMessage()));
        }
        if (notification.getType() == photoValid) return format(resources, content);
        return "Glad to serve you!";
    }


    public CharSequence getContentInfo(Resources resources, Notification notification) {
        switch (notification.getType()) {
            case performing:
                return format(resources, R.string.currency, ": ", notification.getPrice());
        }
        return null;
    }

    public NotificationCompat.Style getStyle(Resources resources, Notification notification) {

        if (notification.getType() == message) {
            //TODO (vl) собственнай макет для уведомления с возможность ответа прямо из меню уведомлений
            return new NotificationCompat.BigTextStyle().bigText(notification.getType().getContent(resources, notification));
        }

        return new NotificationCompat.BigTextStyle().bigText(notification.getType().getContent(resources, notification));
    }


    //TODO (vl) открываем активность по нажатию на уведомление в меню уведомлений, если необходимо
    private PendingIntent getIntent(Context context, Notification notification) {
        if (notification.getType() == message)
            return TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(new Intent(context, ChatActivity.class))
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notification.getType() == photoInvalid) return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, ProfileActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notification.getType() == photoValid) return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        return PendingIntent.getActivity(context, 0, context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean isAlertOnce(NotificationType type) {
        return type != NotificationType.balance;
    }

}
