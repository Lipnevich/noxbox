package by.nicolay.lipnevich.noxbox.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat.Builder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;
import java.util.concurrent.ExecutionException;

import by.nicolay.lipnevich.noxbox.model.MessageType;
import by.nicolay.lipnevich.noxbox.performer.massage.R;

import static android.app.NotificationManager.IMPORTANCE_DEFAULT;
import static by.nicolay.lipnevich.noxbox.tools.MessagingService.PushParams.icon;
import static by.nicolay.lipnevich.noxbox.tools.MessagingService.PushParams.name;
import static by.nicolay.lipnevich.noxbox.tools.MessagingService.PushParams.time;
import static by.nicolay.lipnevich.noxbox.tools.MessagingService.PushParams.type;

/**
 * Created by nicolay.lipnevich on 4/30/2018.
 */

public class MessagingService extends FirebaseMessagingService {

    private final String channelId = "channelId";

    enum PushParams {
        icon,
        type,
        name,
        time
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Builder builder = defaultBuilder()
                .setLargeIcon(loadIcon(remoteMessage.getData().get(icon.name())));

        createNotificationChannel();

        updateTypeSpecificData(builder, remoteMessage.getData());

        getSystemService(NotificationManager.class)
                .notify(getNotificationId(remoteMessage.getData()), builder.build());
    }

    private void updateTypeSpecificData(Builder builder, Map<String, String> map) {
        switch (MessageType.valueOf(map.get(type.name()))) {
            case ping: builder.setSound(getSound(R.raw.requested))
                    .setContentTitle(getText(R.string.requestPushTitle))
                    .setContentText(getText(R.string.requestPushBody)); break;
            case pong: builder.setContentTitle(getText(R.string.acceptPushTitle))
                    .setContentText(String.format(getResources().getString(R.string.acceptPushBody),
                            map.get(name.name()), map.get(time.name()))); break;
            case gnop: break;
            case move: break;
            case story: break;
            case complete: break;
            case dislike: break;
            case balanceUpdated: break;
        }
    }

    private int getNotificationId(Map<String, String> map) {
        // update notification during performer movement, replace other notifications with each other
        return MessageType.valueOf(map.get(type.name())) == MessageType.move ? 1 : 2;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(new NotificationChannel(channelId,
                            "Channel title",
                            IMPORTANCE_DEFAULT));
        }
    }

    private Builder defaultBuilder() {
        return new Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.noxbox)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                .setVibrate(new long[] { 100, 500, 200, 100, 100 })
                .setSound(getSound(R.raw.push))
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                        0, getPackageManager().getLaunchIntentForPackage(getPackageName()),
                        PendingIntent.FLAG_UPDATE_CURRENT));

    }

    private Bitmap loadIcon(String url) {
        try {
            return Glide.with(getApplicationContext()).asBitmap()
                    .load(url)
                    .apply(new RequestOptions().error(R.drawable.profile_picture_blank).circleCrop())
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (InterruptedException | ExecutionException e) {
            Crashlytics.log(Log.WARN, "Fail to load icon for notification", e.getMessage());
        }
        return BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture_blank);
    }

    private Uri getSound(int sound) {

        return Uri.parse("android.resource://" + getPackageName() + "/raw/"
                + getResources().getResourceEntryName(sound));
    }

}
