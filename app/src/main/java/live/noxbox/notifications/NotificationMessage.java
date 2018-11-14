package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.RemoteInput;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;
import live.noxbox.pages.ChatActivity;
import live.noxbox.tools.DateTimeFormatter;

import static live.noxbox.Configuration.CHANNEL_ID;

public class NotificationMessage extends Notification {

    private String name;
    private String message;

    public NotificationMessage(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        name = data.get("name");
        message = data.get("message");


        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_message);
        contentView.setTextViewText(R.id.title, context.getResources().getString(type.getTitle()));
        contentView.setTextViewText(R.id.name, name);
        contentView.setTextViewText(R.id.content, message);
        contentView.setTextViewText(R.id.time, DateTimeFormatter.time(Long.parseLong(notificationTime)));

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, ChatActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        isAutoCancel = true;
    }

    @Override
    public void show() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            buildReplyNotification(context);
        }else{
            final NotificationCompat.Builder builder = getNotificationCompatBuilder();
            getNotificationService(context).notify(type.getGroup(), builder.build());
        }
    }

    private NotificationCompat.Builder buildReplyNotification(Context context) {
        RemoteInput remoteInput =
                new RemoteInput.Builder(context.getResources().getString(R.string.reply).toUpperCase())
                        .setLabel(context.getResources().getString(R.string.enterMessage))
                        .setAllowFreeFormInput(true)
                        .build();

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.UserInputListener.class), PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.mipmap.ic_launcher,
                        context.getResources().getString(R.string.reply).toUpperCase(), replyPendingIntent)
                        .addRemoteInput(remoteInput)
                        .build();

        return new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(name)
                .setContentText(message)
                .addAction(action);
    }
}
