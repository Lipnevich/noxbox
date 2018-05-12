package by.nicolay.lipnevich.noxbox.tools;

import android.app.ActivityManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;

import by.nicolay.lipnevich.noxbox.model.Message;
import by.nicolay.lipnevich.noxbox.model.MessageType;
import by.nicolay.lipnevich.noxbox.model.Notice;
import by.nicolay.lipnevich.noxbox.model.Push;
import by.nicolay.lipnevich.noxbox.model.PushData;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.pages.ChatPage;
import by.nicolay.lipnevich.noxbox.payer.massage.BuildConfig;
import by.nicolay.lipnevich.noxbox.payer.massage.R;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static by.nicolay.lipnevich.noxbox.model.MessageType.balanceUpdated;
import static by.nicolay.lipnevich.noxbox.model.MessageType.ping;
import static by.nicolay.lipnevich.noxbox.model.MessageType.story;
import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;

/**
 * Created by nicolay.lipnevich on 4/30/2018.
 */

public class MessagingService extends FirebaseMessagingService {

    private final String channelId = "channel_id";
    private Context context;

    public MessagingService() {
    }

    public MessagingService(Context context) {
        this.context = context;
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        context = getApplicationContext();

        Notice notice = Notice.create(remoteMessage.getData());

        if(notice.getIgnore()) return;

        showPushNotification(notice);
    }

    public void showPushNotification(Notice notice) {
        if(inForeground()) return;

        createChannel();

        cancelOtherNotifications(notice.getType());

        notify(notice);
    }

    private boolean inForeground() {
        ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
        ActivityManager.getMyMemoryState(appProcessInfo);
        return (appProcessInfo.importance == IMPORTANCE_FOREGROUND
                || appProcessInfo.importance == IMPORTANCE_VISIBLE);
    }

    private NotificationManager getNotificationService() {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    private int getId(MessageType type) {
        switch (type) {
            case ping:
            case move:
            case pong:
            case gnop:
            case cancel:
            case qr:
            case complete: return 1;
            case balanceUpdated: return 2;
            case story: return 9;
            default: return 0;
        }
    }

    private void cancelOtherNotifications(MessageType type) {
        HashSet<MessageType> cancelOther = new HashSet<>(Arrays.asList(
                MessageType.ping,
                MessageType.pong,
                MessageType.gnop,
                MessageType.cancel,
                MessageType.complete));

        if(cancelOther.contains(type)) {
            getNotificationService().cancelAll();
        }
    }

    private void createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationService()
                    .createNotificationChannel(new NotificationChannel(channelId,
                            "Channel title",
                            NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    private void notify(final Notice notice) {
        final RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.push);
        view.setTextViewText(R.id.title, getTitle(notice));
        view.setTextViewText(R.id.text, getContent(notice));

        final Builder builder = new Builder(context, channelId)
                .setVibrate(new long[] { 100, 500, 200, 100, 100 })
                .setSmallIcon(R.mipmap.ic_launcher)
                .setSound(getSound(notice.getType()))
                .setContentTitle(getTitle(notice))
                .setContentText(getContent(notice))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(notice)))
                .setAutoCancel(!BuildConfig.DEBUG)
                .setOnlyAlertOnce(isAlertOnce(notice.getType()))
                .setContentIntent(getIntent(notice));

        Task<Bitmap> iconLoading = new Task<Bitmap>() {
            @Override
            public void execute(Bitmap icon) {
                builder.setLargeIcon(icon);
                view.setImageViewBitmap(R.id.image, icon);
//                builder.setContent(view);
                getNotificationService().notify(getId(notice.getType()), builder.build());
            }
        };

        if(notice.getLocal() != null && notice.getLocal()) {
            loadIconAsync(notice, iconLoading);
        } else {
            iconLoading.execute(loadIcon(notice));
        }
    }

    private PendingIntent getIntent(Notice notice) {
            if(notice.getType() == story && tryGetNoxboxInProgress() != null) {
                return PendingIntent.getActivity(context, PageCodes.CHAT.getCode(),
                        new Intent(context, ChatPage.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
            }
            return PendingIntent.getActivity(context,0, context.getPackageManager()
                                .getLaunchIntentForPackage(context.getPackageName()),
                        PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private String getContent(Notice notice) {
        String cryptoCurrency = context.getResources().getString(R.string.crypto_currency);
        switch (notice.getType()) {
            case ping: return format(R.string.pingPushBody, notice.getName(), notice.getEstimation());
            case move:
            case pong: return format(R.string.pongPushBody, notice.getName(), notice.getEstimation());
            case gnop: return format(R.string.gnopPushBody);
            case cancel: return format(R.string.cancelPushBody);
            case qr: return format(R.string.qrPushBody, notice.getName());
            case story: return notice.getMessage();
            case complete: return format(R.string.completePushBody, notice.getPrice(), cryptoCurrency);
            case balanceUpdated: return format(R.string.balancePushBody, notice.getBalance(), cryptoCurrency);
            default: return "Glad to serve you!";
        }
    }

    private String format(int resource, Object ... args) {
        return String.format(context.getResources().getString(resource), args);
    }
    
    private CharSequence getTitle(Notice notice) {
        switch (notice.getType()) {
            case ping: return format(R.string.pingPushTitle);
            case move:
            case pong: return format(R.string.pongPushTitle);
            case gnop: return format(R.string.gnopPushTitle);
            case cancel: return format(R.string.cancelPushTitle);
            case qr: return format(R.string.qrPushTitle);
            case story: return notice.getName();
            case complete: return format(R.string.completePushTitle);
            case balanceUpdated: return format(R.string.balancePushTitle);
            default: return "Noxbox";
        }    
    }

    private boolean isAlertOnce(MessageType type) {
        return type != MessageType.balanceUpdated && type != story;
    }

    private Bitmap loadIcon(Notice notice) {
        try {
            if(notice.getType() == balanceUpdated) {
                return Glide.with(context).asBitmap().load(R.drawable.noxbox)
                        .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                        .submit().get();
            }
            if(notice.getIcon() != null) {
                return Glide.with(context).asBitmap().load(notice.getIcon())
                        .apply(new RequestOptions().transform(new RoundedCorners(49))
                                .override(128, 128)
                                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .error(R.drawable.profile_picture_blank)).submit().get();
            }
        } catch (InterruptedException | ExecutionException e) {
            Crashlytics.log(Log.WARN, "Fail to load icon for notification", e.getMessage());
        }
        return null;
    }

    private void loadIconAsync(Notice notice, final Task<Bitmap> task) {
        if(notice.getIcon() != null) {
            Glide.with(context).asBitmap()
                    .load(notice.getIcon())
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


    private Uri getSound(MessageType type) {
        int sound = R.raw.push;
        if(type == ping) {
            sound = R.raw.requested;
        }

        return Uri.parse("android.resource://" + context.getPackageName() + "/raw/"
                + context.getResources().getResourceEntryName(sound));
    }

    public static Push generatePush(Message message, String to, UserType userType) {
        if(message.getType() == MessageType.story) {
            Push push = new Push().setTo(to).setData(new PushData());
            if(UserType.performer == userType) {
                push.setRole(UserType.payer.name());
            } else if (UserType.payer == userType) {
                push.setRole(UserType.performer.toString());
            }
            push.getData().setType(MessageType.story.name())
                    .setMessage(message.getStory())
                    .setName(message.getSender().getName())
                    .setIcon(message.getSender().getPhoto());
            return push;
        }
        return null;
    }

    public static Push generatePush(Request request) {
        Push push = new Push().setData(new PushData());
        switch (request.getType()) {
            case request: {
                push.setTo(request.getPerformer().getId()).setRole(UserType.performer.toString());
                push.getData()
                        .setType(ping.toString())
                        .setName(request.getPayer().getName())
                        .setIcon(request.getPayer().getPhoto())
                        .setEstimation(request.getEstimationTime());
                break;
            }
            case accept: {
                push.setTo(request.getPayer().getId()).setRole(UserType.payer.toString());
                push.getData()
                        .setType(MessageType.pong.toString())
                        .setName(request.getPerformer().getName())
                        .setIcon(request.getPerformer().getPhoto())
                        .setEstimation(request.getEstimationTime());
                break;
            }
            case cancel: {
                if(UserType.performer.equals(request.getRole())) {
                    push.setTo(request.getPayer().getId()).setRole(UserType.payer.toString());
                    push.getData()
                        .setType(MessageType.gnop.toString())
                        .setName(request.getPerformer().getName())
                        .setIcon(request.getPerformer().getPhoto());
                } else if (UserType.payer.equals(request.getRole())) {
                    push.setTo(request.getPerformer().getId()).setRole(UserType.performer.toString());
                    push.getData()
                            .setType(MessageType.cancel.toString())
                            .setName(request.getPayer().getName())
                            .setIcon(request.getPayer().getPhoto());
                }
                break;
            }
            case complete: {
                push.setTo(request.getPayer().getId()).setRole(UserType.payer.toString());
                push.getData()
                        .setType(MessageType.complete.toString())
                        .setPrice(request.getNoxbox().getPrice())
                        .setName(request.getPerformer().getName())
                        .setIcon(request.getPerformer().getPhoto());
                break;
            }
            case qr: {
                push.setTo(request.getPayer().getId()).setRole(UserType.payer.toString());
                push.getData()
                        .setType(MessageType.story.toString())
                        .setName(request.getPerformer().getName())
                        .setIcon(request.getPerformer().getPhoto());
                break;
            }
            case balance: {
                push.setTo(request.getId()).setRole(request.getRole().toString());
                push.getData().setType(MessageType.balanceUpdated.toString());
                break;
            }
        }

        return push;
    }

}
