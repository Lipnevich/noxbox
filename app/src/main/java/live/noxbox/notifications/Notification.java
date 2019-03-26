package live.noxbox.notifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.Constants;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;

import static android.content.Context.VIBRATOR_SERVICE;
import static android.support.v4.app.NotificationCompat.VISIBILITY_PUBLIC;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.NotificationType.message;

public abstract class Notification {


    //need initialize for NotificationCompat.Builder
    protected long[] vibrate;
    protected Uri sound;
    protected RemoteViews contentView;
    protected PendingIntent onViewOnClickAction;
    protected PendingIntent deleteIntent;
    protected boolean isAutoCancel = false;

    protected Context context;
    protected Profile profile;
    protected NotificationType type;
    protected String notificationTime;
    protected Map<String, String> data;
    protected String noxboxId;

    // first value is Initial delay ..
    // second value is Vibrate for ..
    // third value is Pause for ..
    // fourth value is Vibrate for ..
    // fifth value is Pause for ..
    // sixth value is Vibrate for ..
    private static long[] vibration = new long[]{500, 500, 400, 500, 400, 1000, 400, 200};
    private static int[] amplitude = new int[]{0, 255, 0, 255, 0, 255, 0, 255};

    public Notification(Context context, Profile profile, Map<String, String> data) {
        this.context = context;
        this.profile = profile;
        this.data = data;
        type = NotificationType.valueOf(data.get("type"));
        notificationTime = data.get("time");
        noxboxId = data.get("id");

        this.vibrate = getVibrate();
        this.sound = getSound(type, context);

        removeNotifications(context);
    }

    public void show() {

    }

    public void update(Map<String, String> data) {
    }

    protected NotificationCompat.Builder getNotificationCompatBuilder() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, Constants.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOnlyAlertOnce(true)
                .setVisibility(VISIBILITY_PUBLIC)
                .setContentIntent(onViewOnClickAction)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDeleteIntent(deleteIntent)
                .setAutoCancel(isAutoCancel);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            builder.setCustomContentView(contentView)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH);
        else
            builder.setContent(contentView)
                    .setPriority(android.app.Notification.PRIORITY_MAX);

        switch (type) {
            case photoUploadingProgress:
            case photoValidationProgress:
                break;
            default: {
                Vibrator vibrator;
                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                    if (vibrator.hasVibrator()) {
                        VibrationEffect vibrationEffect;
                        if (vibrator.hasAmplitudeControl()) {
                            vibrationEffect = VibrationEffect.createWaveform(getVibrate(), getAmplitude(), -1);
                        } else {
                            vibrationEffect = VibrationEffect.createWaveform(getVibrate(), -1);
                        }
                        vibrator.vibrate(vibrationEffect);
                    }
                } else {
                    vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
                    vibrator.vibrate(getVibrate(), -1);
                }
                Ringtone ringtone = RingtoneManager.getRingtone(context, getSound(type, context));
                ringtone.play();
            }
        }
        return builder;
    }

    protected static NotificationManager getNotificationService(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    protected static void removeNotifications(Context context) {
        MessagingService.getNotificationService(context).cancelAll();
    }

    protected static void removeNotificationByGroup(Context context, int group) {
        MessagingService.getNotificationService(context).cancel(group);
    }

    protected void updateNotification(Context context, NotificationCompat.Builder builder) {
        if (type != NotificationType.message)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                builder.setCustomContentView(contentView);
            } else {
                builder.setContent(contentView);
            }

        getNotificationService(context).notify(type.getGroup(), builder.build());
    }

    public static Uri getSound(NotificationType type, Context context) {
        switch (type) {
            case photoUploadingProgress:
            case photoValidationProgress:
                return null;
        }
        int sound = R.raw.push;
        if (type == message) {
            sound = R.raw.message;
        }

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }

    protected long[] getVibrate() {
        switch (type) {
            case photoUploadingProgress:
            case photoValidationProgress:
                return null;
        }

        return vibration;
    }

    protected int[] getAmplitude() {
        switch (type) {
            case photoUploadingProgress:
            case photoValidationProgress:
                return null;
        }

        return amplitude;
    }

    protected PendingIntent createOnDeleteIntent(Context context, int group) {
        Intent intent = new Intent(context, DeleteActionIntent.class);
        return PendingIntent.getBroadcast(context.getApplicationContext(),
                group, intent, 0);
    }


    public static class DeleteActionIntent extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            removeNotifications(context);
        }
    }

    public static class CancelRequestListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, Intent intent) {
            AppCache.readProfile(profile -> {
                if (profile.equals(profile.getCurrent().getOwner())) {
                    profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                    profile.getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
                } else {
                    profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                    profile.getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
                }
                updateNoxbox();
                MessagingService.getNotificationService(context).cancelAll();
            });
        }
    }


    public static class SupportNotificationListener extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
            } catch (android.content.ActivityNotFoundException anfe) {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
            }
        }

    }

    private static String getMessageText(Intent intent, Context context) {
        Bundle remoteInput = RemoteInput.getResultsFromIntent(intent);
        if (remoteInput != null) {
            return String.valueOf(remoteInput.getCharSequence(context.getResources().getString(R.string.reply).toUpperCase()));
        }
        return null;
    }

    protected String format(Resources resources, int resource, Object... args) {
        return String.format(resources.getString(resource), args);
    }

}
