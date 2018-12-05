package live.noxbox.model;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.widget.RemoteViews;

import live.noxbox.Configuration;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.ChatActivity;
import live.noxbox.database.AppCache;
import live.noxbox.menu.HistoryActivity;
import live.noxbox.menu.WalletActivity;
import live.noxbox.menu.profile.ProfileActivity;
import live.noxbox.notifications.Notification;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.COMISSION_FEE;
import static live.noxbox.Configuration.CURRENCY;
import static live.noxbox.services.MessagingService.getNotificationService;

/**
 * Created by nicolay.lipnevich on 13/05/2017.
 */
public enum NotificationType {

    photoUploadingProgress(0, R.string.uploadingStarted, R.string.uploadingProgressTitle),
    photoValidationProgress(0, R.string.NoxBox, R.string.photoValidationProgressContent),
    photoValid(0, R.string.NoxBox, R.string.photoValidContent),
    photoInvalid(0, R.string.photoInvalidTitle, R.string.photoInvalidContent),

    balance(1, R.string.balancePushTitle, R.string.balancePushContent),

    requesting(2, R.string.requestText, R.string.requestingPushContent),
    accepting(2, R.string.acceptText, R.string.acceptingPushContent),
    moving(2, R.string.acceptPushTitle, R.string.replaceIt),
    verifyPhoto(2, R.string.replaceIt, R.string.replaceIt),
    performing(2, R.string.performing, R.string.performingPushContent),
    lowBalance(2, R.string.outOfMoney, R.string.beforeSpendingMoney),
    completed(2, R.string.noxboxCompleted, R.string.completedPushContent),
    supplierCanceled(2, R.string.supplierCancelPushTitle, R.string.supplierCanceledPushContent),
    demanderCanceled(2, R.string.demanderCancelPushTitle, R.string.demanderCanceledPushContent),

    refund(3, R.string.replaceIt, R.string.replaceIt),

    message(4, R.string.newMessage, R.string.replaceIt),

    support(5, R.string.messageFromTheSupport, R.string.replaceIt);


    private int group;
    private int title;
    private int content;

    NotificationType(int id, int title, int content) {
        this.group = id;
        this.title = title;
        this.content = content;
    }

    public int getGroup() {
        return group;
    }

    public int getTitle() {
        return title;
    }

    public int getContent() {
        return content;
    }

    public static NotificationCompat.Builder getBuilder(Context context, String channelId, NotificationData notificationData) {
        if (notificationData.getType() == message && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return buildReplyNotification(context, channelId, notificationData);
        }

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(getVibrate(notificationData))
                .setContent(getCustomContentView(context, notificationData))
                .setSound(getSound(context, notificationData.getType()))
                //.setCustomContentView(getCustomContentView(context, notification)) //24 Version and upper
                .setOnlyAlertOnce(isAlertOnce(notificationData.getType()))
                .setContentIntent(getIntent(context, notificationData))
                .setAutoCancel(getAutoCancel(notificationData));
    }


    public static void updateNotification(Context context, final NotificationData notification, NotificationCompat.Builder builder) {
        if (notification.getType() != message)
            builder.setCustomContentView(getCustomContentView(context, notification));

        getNotificationService(context).notify(notification.getType().getGroup(), builder.build());
    }

    public static void removeNotifications(Context context) {
        MessagingService.getNotificationService(context).cancelAll();
    }

    public static boolean getAutoCancel(NotificationData notification) {
        switch (notification.getType()) {
            case support:
            case supplierCanceled:
            case demanderCanceled:
            case message:
            case balance:
            case completed:
                return true;
        }
        return false;
    }

    private static RemoteViews getCustomContentView(final Context context, final NotificationData notification) {
        RemoteViews remoteViews = null;
        if (notification.getType() == photoUploadingProgress) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_uploading_progress);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            if(notification.getProgress().equals(notification.getMaxProgress())) {
                remoteViews.setTextViewText(R.id.uploadingProgress, context.getResources().getString(R.string.uploadingComplete));
            } else {
                remoteViews.setTextViewText(R.id.uploadingProgress, format(context.getResources(), notification.getType().content, notification.getProgress()));
            }
            remoteViews.setProgressBar(R.id.progress, notification.getMaxProgress(), notification.getProgress(), false);
        }
        if (notification.getType() == photoValidationProgress) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_photo_validation_progress);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.content, context.getResources().getString(notification.getType().content));
        }
        if (notification.getType() == photoValid) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_valid_photo);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.content, context.getResources().getString(notification.getType().content));
        }
        if (notification.getType() == photoInvalid) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_invalid_photo);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            //remoteViews.setTextViewText(R.id.content, format(context.getResources(), notification.getType().content, context.getResources().getString(notification.getInvalidAcceptance().getCorrectionMessage())) + " " + notification.getMessage());
        }

        if (notification.getType() == performing) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_performing);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.timeHasPassed, context.getResources().getString(notification.getType().content));
            remoteViews.setTextViewText(R.id.stopwatch, notification.getTime());
            remoteViews.setTextViewText(R.id.totalPayment, (notification.getPrice().concat(" ")).concat(Configuration.CURRENCY));
        }

        if (notification.getType() == accepting) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
            remoteViews.setTextViewText(R.id.countDownTime, notification.getTime());
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));

        }

        if (notification.getType() == moving) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_moving);
            remoteViews.setTextViewText(R.id.time, String.valueOf((Long.parseLong(notification.getTime())) / 60000).concat("min"));
            remoteViews.setProgressBar(R.id.progress, notification.getMaxProgress(), notification.getProgress(), false);
        }

        if (notification.getType() == supplierCanceled || notification.getType() == demanderCanceled) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_canceled);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.content, context.getResources().getString(notification.getType().content));
        }

        if (notification.getType() == support) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_support);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.content, notification.getMessage());
        }

        if (notification.getType() == lowBalance) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_low_balance);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.content, context.getResources().getString(notification.getType().content));
        }
        if (notification.getType() == message) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_message);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.name, notification.getName());
            remoteViews.setTextViewText(R.id.content, notification.getMessage());
            remoteViews.setTextViewText(R.id.time, DateTimeFormatter.time(Long.parseLong(notification.getTime())));
        }
        if (notification.getType() == balance) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_balance);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.content, notification.getBalance());

        }
        if (notification.getType() == completed) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_completed);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.contentRole, notification.getMessage());
            String comission = " " + context.getResources().getString(R.string.withComission) + " " + COMISSION_FEE + " " + CURRENCY;
            remoteViews.setTextViewText(R.id.content, notification.getPrice().concat(" ").concat(CURRENCY).concat(comission));
        }

        return remoteViews;
    }

    public static long[] getVibrate(NotificationData notification) {
        switch (notification.getType()) {
            case requesting:
            case photoUploadingProgress:
            case moving:
            case performing:
            case completed:
            case accepting:
            case lowBalance:
                return null;
            default:
                return new long[]{100, 500, 200, 100, 100};
        }
    }

    public static Uri getSound(Context context, NotificationType type) {
        if (type == photoUploadingProgress || type == performing || type == moving || type == accepting)
            return null;

        int sound = R.raw.push;
        //sound = R.raw.requested;

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }


    public static PendingIntent getIntent(Context context, NotificationData notification) {
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

        if (notification.getType() == moving) return TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notification.getType() == demanderCanceled || notification.getType() == supplierCanceled)
            return TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        if (notification.getType() == lowBalance) {
            if (notification.getMessage().equals("supply")) {
                return TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            } else {
                return TaskStackBuilder.create(context)
                        .addNextIntentWithParentStack(new Intent(context, WalletActivity.class))
                        .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            }
        }

        if (notification.getType() == message) {
            return TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(new Intent(context, ChatActivity.class))
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        if (notification.getType() == completed) {
            return TaskStackBuilder.create(context)
                    .addNextIntentWithParentStack(new Intent(context, HistoryActivity.class))
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        }

        switch (notification.getType()) {
            case accepting:
                return null;
        }


        return PendingIntent.getActivity(context, 0, context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static boolean isAlertOnce(NotificationType type) {
        return type != NotificationType.balance;
    }

    public static void showLowBalanceNotification(Context context, final Profile profile, final NotificationData notification) {
    }

    public static void showPerformingNotification(final Context context, final Profile profile, final NotificationData notification) {
    }

    private static NotificationCompat.Builder buildReplyNotification(Context context, String channelId, NotificationData notification) {
        RemoteInput remoteInput =
                new RemoteInput.Builder(context.getResources().getString(R.string.reply).toUpperCase())
                        .setLabel(context.getResources().getString(R.string.enterMessage))
                        .setAllowFreeFormInput(true)
                        .build();

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(context, 0, new Intent(context, Notification.UserInputListener.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
                        context.getResources().getString(R.string.reply).toUpperCase(), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification.getName())
                .setContentText(notification.getMessage())
                .addAction(action);
    }


    public static class AcceptRequestListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                    AppCache.fireProfile();
                    MessagingService.getNotificationService(context).cancelAll();
                }
            });
        }
    }


    private static String format(Resources resources, int resource, Object... args) {
        return String.format(resources.getString(resource), args);
    }
}
