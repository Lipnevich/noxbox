package by.nicolay.lipnevich.noxbox.tools;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.concurrent.ExecutionException;

import by.nicolay.lipnevich.noxbox.model.MessageType;
import by.nicolay.lipnevich.noxbox.performer.massage.R;

/**
 * Created by nicolay.lipnevich on 4/30/2018.
 */

public class MessagingService extends FirebaseMessagingService {

    private final String channelId = "channelId";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.drawable.profile_picture_blank);
        String url = remoteMessage.getData().get("icon");
        try {
            icon = Glide.with(getApplicationContext()).asBitmap()
                    .load(url)
                    .apply(new RequestOptions().error(R.drawable.profile_picture_blank).circleCrop())
                    .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL).get();
        } catch (InterruptedException | ExecutionException e) {
            Crashlytics.log(Log.WARN, "Fail to load icon for notification", e.getMessage());
        }

        CharSequence title = "";
        CharSequence text = "";
        String sound = "push";
        MessageType type = MessageType.valueOf(remoteMessage.getData().get("type"));
        boolean autoCancel = true;
        if(type == MessageType.ping) {
            title = getText(R.string.requestPushTitle);
            text = getText(R.string.requestPushBody);
            sound = "requested";
        }


        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(getApplicationContext(), channelId)
                        .setSmallIcon(R.drawable.noxbox)
                        .setColor(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                        .setVibrate(new long[] { 100, 500, 200, 100, 100 })
                        .setSound(Uri.parse("android.resource://" + getPackageName() + "/raw/" + sound))
                        .setLargeIcon(icon)
                        .setAutoCancel(autoCancel)
                        .setContentIntent(PendingIntent.getActivity(getApplicationContext(),
                                0, getPackageManager().getLaunchIntentForPackage(getPackageName()),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                        .setContentTitle(title)
                        .setContentText(text);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(3, builder.build());
    }

}
