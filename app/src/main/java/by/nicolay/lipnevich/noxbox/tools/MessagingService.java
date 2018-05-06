package by.nicolay.lipnevich.noxbox.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import by.nicolay.lipnevich.noxbox.model.MessageType;
import by.nicolay.lipnevich.noxbox.model.Notice;
import by.nicolay.lipnevich.noxbox.model.Push;
import by.nicolay.lipnevich.noxbox.model.PushData;
import by.nicolay.lipnevich.noxbox.model.Request;
import by.nicolay.lipnevich.noxbox.model.UserType;
import by.nicolay.lipnevich.noxbox.payer.massage.R;

import static by.nicolay.lipnevich.noxbox.model.MessageType.balanceUpdated;
import static by.nicolay.lipnevich.noxbox.model.MessageType.ping;
import static by.nicolay.lipnevich.noxbox.model.MessageType.story;

/**
 * Created by nicolay.lipnevich on 4/30/2018.
 */

public class MessagingService extends FirebaseMessagingService {

    private final String channelId = "channel_id";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        Notice notice = Notice.create(remoteMessage.getData());
        if(notice.getIgnore()) {
            return;
        }

        createNotificationChannel();

        cancelOtherNotifications(notice.getType());

        Builder builder = builder(notice);

        getNotificationService().notify(getId(notice.getType()), builder.build());
    }

    private NotificationManager getNotificationService() {
        return (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
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
            case story: return ThreadLocalRandom.current().nextInt(100, 1000);
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

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getNotificationService()
                    .createNotificationChannel(new NotificationChannel(channelId,
                            "Channel title",
                            NotificationManager.IMPORTANCE_DEFAULT));
        }
    }

    private Builder builder(Notice notice) {
        return new Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.noxbox)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                .setVibrate(new long[] { 100, 500, 200, 100, 100 })
                .setSound(getSound(notice.getType()))
                .setAutoCancel(true)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(getContent(notice)))
                .setOnlyAlertOnce(isAlertOnce(notice.getType()))
                .setContentTitle(getTitle(notice))
                .setContentText(getContent(notice))
                .setLargeIcon(loadIcon(notice))
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                        0, getPackageManager().getLaunchIntentForPackage(getPackageName()),
                        PendingIntent.FLAG_UPDATE_CURRENT));
    }

    private String getContent(Notice notice) {
        String cryptoCurrency = getResources().getString(R.string.crypto_currency);
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
        return String.format(getResources().getString(resource), args);
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
        if(notice.getType() == balanceUpdated) {
            return BitmapFactory.decodeResource(getResources(), R.drawable.noxbox);
        }

        if(notice.getIcon() != null) {
            try {
                return Glide.with(getApplicationContext()).asBitmap()
                        .load(notice.getIcon())
                        .apply(new RequestOptions().error(R.drawable.profile_picture_blank).circleCrop())
                        .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
            } catch (InterruptedException | ExecutionException e) {
                Crashlytics.log(Log.WARN, "Fail to load icon for notification", e.getMessage());
            }
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture_blank);
    }

    private Uri getSound(MessageType type) {
        int sound = R.raw.push;
        if(type == ping) {
            sound = R.raw.requested;
        }

        return Uri.parse("android.resource://" + getPackageName() + "/raw/"
                + getResources().getResourceEntryName(sound));
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
            case message: {
                if(UserType.performer.equals(request.getRole())) {
                    push.setTo(request.getPayer().getId()).setRole(UserType.payer.toString());
                    push.getData()
                            .setType(MessageType.story.toString())
                            .setMessage(request.getMessage())
                            .setName(request.getPerformer().getName())
                            .setIcon(request.getPerformer().getPhoto());
                } else if (UserType.payer.equals(request.getRole())) {
                    push.setTo(request.getPerformer().getId()).setRole(UserType.performer.toString());
                    push.getData()
                            .setType(MessageType.story.toString())
                            .setMessage(request.getMessage())
                            .setName(request.getPayer().getName())
                            .setIcon(request.getPayer().getPhoto());
                }
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
