package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.widget.RemoteViews;

import com.google.common.base.Strings;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;
import live.noxbox.pages.ChatActivity;

import static live.noxbox.Configuration.CHANNEL_ID;

public class NotificationMessage extends Notification {

    private String profession;
    private String name;
    private String message;

    public NotificationMessage(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        name = data.get("name");
        profession = context.getResources().getString(NoxboxType.valueOf(data.get("noxboxType")).getProfession());
        message = data.get("message");

        vibrate = getVibrate();
        sound = getSound();

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_message);

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, ChatActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        isAutoCancel = true;
    }

    @Override
    public void show() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//            getNotificationService(context).notify(type.getGroup(), buildReplyNotification(context).build());
//        } else {
//            contentView.setTextViewText(R.id.profession, Strings.isNullOrEmpty(name) ? profession : name);
//            contentView.setTextViewText(R.id.message, message);
//            final NotificationCompat.Builder builder = getNotificationCompatBuilder();
//            getNotificationService(context).notify(type.getGroup(), builder.build());
//        }
        contentView.setTextViewText(R.id.profession, Strings.isNullOrEmpty(name) ? profession : name);
        contentView.setTextViewText(R.id.message, message);
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());

    }


    private NotificationCompat.Builder buildReplyNotification(Context context) {
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

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(profession)
                .setContentText(message)
                .addAction(action);
    }
}
