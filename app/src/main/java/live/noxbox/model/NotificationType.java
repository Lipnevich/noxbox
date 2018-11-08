package live.noxbox.model;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.util.Log;
import android.widget.RemoteViews;

import live.noxbox.Configuration;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.menu.HistoryActivity;
import live.noxbox.menu.WalletActivity;
import live.noxbox.pages.ChatActivity;
import live.noxbox.profile.ProfileActivity;
import live.noxbox.state.AppCache;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MessagingService;
import live.noxbox.tools.NavigatorManager;
import live.noxbox.tools.Task;

import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.MessagingService.getNotificationService;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.initializeStopwatch;
import static live.noxbox.tools.SeparateStreamForStopwatch.runTimer;
import static live.noxbox.tools.SeparateStreamForStopwatch.seconds;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

/**
 * Created by nicolay.lipnevich on 13/05/2017.
 */
public enum NotificationType {

    uploadingProgress(0, R.string.uploadingStarted, R.string.uploadingProgressTitle),
    photoValidationProgress(0, R.string.noxbox, R.string.photoValidationProgressContent),
    photoValid(0, R.string.noxbox, R.string.photoValidContent),
    photoInvalid(0, R.string.photoInvalidTitle, R.string.photoInvalidContent),

    balance(1, R.string.balancePushTitle, R.string.balancePushContent),

    requesting(2, R.string.requestText, R.string.requestingPushContent),
    accepting(2, R.string.acceptText, R.string.acceptingPushContent),
    moving(2, R.string.acceptPushTitle, R.string.replaceIt),
    confirm(2, R.string.confirm, R.string.replaceIt),
    verifyPhoto(2, R.string.replaceIt, R.string.replaceIt),
    performing(2, R.string.performing, R.string.performingPushContent),
    lowBalance(2, R.string.outOfMoney, R.string.beforeSpendingMoney),
    completed(2, R.string.noxboxCompleted, R.string.completedPushContent),
    supplierCanceled(2, R.string.supplierCancelPushTitle, R.string.supplierCanceledPushContent),
    demanderCanceled(2, R.string.demanderCancelPushTitle, R.string.demanderCanceledPushContent),

    refund(3, R.string.replaceIt, R.string.replaceIt),

    message(4, R.string.newMessage, R.string.replaceIt),

    support(5, R.string.messageFromTheSupport, R.string.replaceIt);


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
        if (notification.getType() == message && Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return buildReplyNotification(context, channelId, notification);
        }

        return new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setVibrate(getVibrate(notification))
                .setContent(getCustomContentView(context, notification))
                .setSound(getSound(context, notification.getType()))
                //.setCustomContentView(getCustomContentView(context, notification)) //24 Version and upper
                .setOnlyAlertOnce(isAlertOnce(notification.getType()))
                .setContentIntent(getIntent(context, notification))
                .setAutoCancel(getAutoCancel(notification));
    }

    private boolean getAutoCancel(Notification notification) {
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

    public static void updateNotification(Context context, final Notification notification, NotificationCompat.Builder builder) {
        if (notification.getType() != message)
            builder.setCustomContentView(getCustomContentView(context, notification));

        getNotificationService(context).notify(notification.getType().getIndex(), builder.build());
    }

    public static void removeNotifications(Context context) {
        MessagingService.getNotificationService(context).cancelAll();
    }

    private static RemoteViews getCustomContentView(final Context context, final Notification notification) {
        RemoteViews remoteViews = null;
        if (notification.getType() == uploadingProgress) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_uploading_progress);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.uploadingProgress, format(context.getResources(), notification.getType().content, notification.getProgress()));
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
            remoteViews.setTextViewText(R.id.content, format(context.getResources(), notification.getType().content, context.getResources().getString(notification.getInvalidAcceptance().getCorrectionMessage())));
        }

        if (notification.getType() == performing) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_perfirming);
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setTextViewText(R.id.timeHasPassed, context.getResources().getString(notification.getType().content));
            remoteViews.setTextViewText(R.id.stopwatch, notification.getTime());
            remoteViews.setTextViewText(R.id.totalPayment, (notification.getPrice().concat(" ")).concat(Configuration.CURRENCY));
        }

        if (notification.getType() == requesting) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_requesting);
            remoteViews.setTextViewText(R.id.countDownTime, notification.getTime());
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setOnClickPendingIntent(R.id.cancel, PendingIntent.getBroadcast(context, 0, new Intent(context, CancelRequestListener.class), 0));
        }

        if (notification.getType() == accepting) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
            remoteViews.setTextViewText(R.id.countDownTime, notification.getTime());
            remoteViews.setTextViewText(R.id.title, context.getResources().getString(notification.getType().title));
            remoteViews.setOnClickPendingIntent(R.id.accept, PendingIntent.getBroadcast(context, 0, new Intent(context, AcceptRequestListener.class), 0));
            remoteViews.setOnClickPendingIntent(R.id.cancel, PendingIntent.getBroadcast(context, 0, new Intent(context, CancelRequestListener.class), 0));
        }

        if (notification.getType() == moving) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_moving);
            remoteViews.setTextViewText(R.id.time, String.valueOf((Long.parseLong(notification.getTime())) / 60000).concat("min"));
            remoteViews.setProgressBar(R.id.progress, notification.getMaxProgress(), notification.getProgress(), false);
            remoteViews.setOnClickPendingIntent(R.id.navigation, PendingIntent.getBroadcast(context, 0, new Intent(context, NavigationButtonListener.class), 0));
        }

        if (notification.getType() == confirm) {
            remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_confirm);
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
            remoteViews.setTextViewText(R.id.content, notification.getPrice().concat(" ").concat(Configuration.CURRENCY));
        }

        return remoteViews;
    }

    public long[] getVibrate(Notification notification) {
        switch (notification.getType()) {
            case uploadingProgress:
            case moving:
            case performing:
            case completed:
            case requesting:
            case accepting:
            case lowBalance:
                return null;
            default:
                return new long[]{100, 500, 200, 100, 100};
        }
    }

    public Uri getSound(Context context, NotificationType type) {
        if (type == uploadingProgress || type == performing || type == moving || type == requesting || type == accepting)
            return null;

        int sound = R.raw.push;
        if (type == requesting) {
            sound = R.raw.requested;
        }

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }

    private static String format(Resources resources, int resource, Object... args) {
        return String.format(resources.getString(resource), args);
    }

    private static PendingIntent getIntent(Context context, Notification notification) {
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
            case requesting:
            case accepting:
            case confirm:
                return null;
        }


        return PendingIntent.getActivity(context, 0, context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private boolean isAlertOnce(NotificationType type) {
        return type != NotificationType.balance;
    }

    public static class CancelRequestListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                        profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                    } else {
                        profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                    }
                    AppCache.updateNoxbox();
                    MessagingService.getNotificationService(context).cancelAll();
                }
            });
        }
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

    public static class NavigationButtonListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    NavigatorManager.openNavigator(context, profile);
                }
            });
        }
    }

    public static class UserInputListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    // TODO (?) сохранить одно сообщение в базе
                    Message message = new Message()
                            .setMessage(getMessageText(intent, context)).setId("111");
                    if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                        profile.getCurrent().getOwnerMessages().put(message.getId(), message);
                    } else {
                        profile.getCurrent().getPartyMessages().put(message.getId(), message);
                    }
                    AppCache.updateNoxbox();
                    removeNotifications(context);
                }
            });
        }

    }

    private static String getMessageText(Intent intent, Context context) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return String.valueOf(remoteInput.getCharSequence(context.getResources().getString(R.string.reply).toUpperCase()));
        }
        return null;
    }

    private static NotificationCompat.Builder buildReplyNotification(Context context, String channelId, Notification notification) {
        RemoteInput remoteInput =
                new RemoteInput.Builder(context.getResources().getString(R.string.reply).toUpperCase())
                        .setLabel(context.getResources().getString(R.string.enterMessage))
                        .setAllowFreeFormInput(true)
                        .build();

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(context, 0, new Intent(context, UserInputListener.class), PendingIntent.FLAG_UPDATE_CURRENT);

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

    public static void showLowBalanceNotification(Context context, final Profile profile, final Notification notification) {
        if (profile.getCurrent().getOwner().equals(profile)) {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                notification.setType(NotificationType.lowBalance).setMessage("supply");
            } else {
                notification.setType(NotificationType.lowBalance).setMessage("demand");
            }
        } else {
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                notification.setType(NotificationType.lowBalance).setMessage("demand");
            } else {
                notification.setType(NotificationType.lowBalance).setMessage("supply");
            }
        }
        updateNotification(context, notification, MessagingService.builder);
    }

    public static void showPerformingNotificationInBackground(final Context context, final Profile profile, final Notification notification) {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);

                if (profile.getCurrent().getTimeCompleted() == null) {
                    seconds++;
                    Log.e("NotificationType.class", "run()");
                    notification.setTime(time);
                    notification.setPrice(decimalFormat.format(totalMoney));

                    if (enoughBalanceOnFiveMinutes(profile.getCurrent(), profile)) {
                        updateNotification(context, notification, MessagingService.builder);
                    } else {
                        showLowBalanceNotification(context, profile, notification);
                    }


                    handler.postDelayed(this, 1000);
                }
            }
        };
        initializeStopwatch(profile, handler, runnable);
        runTimer();
    }


}
