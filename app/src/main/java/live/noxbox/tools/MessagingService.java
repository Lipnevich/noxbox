package live.noxbox.tools;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import io.fabric.sdk.android.Fabric;
import live.noxbox.R;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.model.NotificationType.balance;
import static live.noxbox.model.NotificationType.showAcceptingNotification;

/**
 * Created by nicolay.lipnevich on 4/30/2018.
 */

public class MessagingService extends FirebaseMessagingService {

    private final String channelId = "noxbox_channel";
    private Context context;

    public static NotificationCompat.Builder builder;

    public MessagingService() {
    }

    public MessagingService(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceived(final RemoteMessage remoteMessage) {
        initCrashReporting();
        context = getApplicationContext();
        Notification notification = Notification.create(remoteMessage.getData());
        if (notification.getIgnore()) return;

        if (notification.getType() == NotificationType.accepting) {
            notification.setTime(String.valueOf(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS));
            showAcceptingNotification(context, notification,channelId);
            return;
        }
        showPushNotification(notification);


        //        AppCache.readProfile(new Task<Profile>() {
//            @Override
//            public void execute(final Profile profile) {
//                Notification notification = Notification.create(remoteMessage.getData());
//                if (notification.getIgnore()) return;
//
//                if (notification.getType() == NotificationType.accepting) {
//                    long timeForRequestInMillis = REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - (System.currentTimeMillis() - profile.getCurrent().getTimeRequested());
//                    notification.setTime(String.valueOf(timeForRequestInMillis));
//                    showAcceptingNotification(context, notification,channelId);
//                    return;
//                }
//
//                if (notification.getType() == NotificationType.message) {
//                    notification.setMessage(notification.getId());
//                    notification.setName(profile.getName());
//                    notification.setTime(String.valueOf(System.currentTimeMillis()));
//                }
//
//                if (notification.getType() == NotificationType.performing && !inForeground()) {
//                    showPerformingNotification(context, profile, notification);
//                    return;
//                }
//
//                 showPushNotification(notification);
//            }
//        });
    }

    private void initCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
//                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }

    public void showPushNotification(Notification notification) {
        createChannel();

        cancelOtherNotifications(notification.getType());
        if (notification.getType() != NotificationType.moving && notification.getType() != NotificationType.message) {
            getNotificationService(context).cancel(NotificationType.message.getIndex());
        }
        if (notification.getProgress() != null && notification.getProgress() == 100) {
            getNotificationService(context).cancel(notification.getType().getIndex());
        } else {
            notify(notification);
        }
    }


    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationService(context).createNotificationChannel(
                    new NotificationChannel(channelId,
                            "Noxbox channel",
                            NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    private void cancelOtherNotifications(NotificationType type) {
        HashSet<NotificationType> cancelOther = new HashSet<>(Arrays.asList(
                NotificationType.requesting,
                NotificationType.accepting,
                NotificationType.supplierCanceled,
                NotificationType.demanderCanceled,
                NotificationType.completed,
                NotificationType.performing));

        if (cancelOther.contains(type)) {
            getNotificationService(context).cancelAll();
        }
    }


    private void notify(final Notification notification) {

        builder = NotificationType.getBuilder(context, channelId, notification);

        setProgress(builder, notification);

        Task<Bitmap> iconLoading = new Task<Bitmap>() {
            @Override
            public void execute(Bitmap icon) {
                builder.setLargeIcon(icon);
                getNotificationService(context).notify(notification.getType().getIndex(), builder.build());
            }
        };

        if (notification.getLocal() != null && notification.getLocal()) {
            loadIconAsync(notification, iconLoading);
        } else {
            iconLoading.execute(loadIcon(notification));
        }
    }


    private void setProgress(NotificationCompat.Builder builder, Notification notification) {
        if (notification.getProgress() != null) {
            builder.setProgress(100, notification.getProgress(), false);
        }
    }

    private Bitmap loadIcon(Notification notification) {
        try {
            if (notification.getType() == balance) {
                return Glide.with(context).asBitmap().load(R.drawable.noxbox)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .submit().get();
            }
            if (notification.getIcon() != null) {
                return Glide.with(context).asBitmap().load(notification.getIcon())
                        .apply(new RequestOptions().transform(new RoundedCorners(49))
                                .override(128, 128)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                                .error(R.drawable.profile_picture_blank)).submit().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

    private void loadIconAsync(Notification notification, final Task<Bitmap> task) {
        if (notification.getIcon() != null) {
            Glide.with(context).asBitmap()
                    .load(notification.getIcon())
                    .apply(new RequestOptions().transform(new RoundedCorners(49))
                            .override(128, 128)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .error(R.drawable.profile_picture_blank))
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<?
                                super Bitmap> transition) {
                            task.execute(resource);
                        }
                    });
        }
    }

    public static NotificationManager getNotificationService(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }


    private static boolean inForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

}
