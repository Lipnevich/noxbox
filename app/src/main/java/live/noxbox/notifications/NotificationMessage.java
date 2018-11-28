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
import live.noxbox.database.Firestore;
import live.noxbox.model.Message;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;
import live.noxbox.pages.ChatActivity;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.CHANNEL_ID;

public class NotificationMessage extends Notification {

    private String name;
    private Message message;

    public NotificationMessage(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
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
        Firestore.readNoxbox(noxbixId, new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                message = getLastMessage(noxbox);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    getNotificationService(context).notify(type.getGroup(), buildReplyNotification(context).build());
                } else {
                    contentView.setTextViewText(R.id.name, name);
                    contentView.setTextViewText(R.id.content, message.getMessage());
                    contentView.setTextViewText(R.id.time, DateTimeFormatter.time(message.getTime()));
                    final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                    getNotificationService(context).notify(type.getGroup(), builder.build());
                }

            }
        });

    }

    private Message getLastMessage(Noxbox noxbox) {
        Message message = new Message().setTime(0L);
        Map<String, Message> ownerMessages = noxbox.getOwnerMessages();
        Map<String, Message> partyMessages = noxbox.getPartyMessages();


        for (Map.Entry<String, Message> ownerMessage : ownerMessages.entrySet()) {
            Message current = ownerMessage.getValue();
            if (message.getTime() < current.getTime()) {
                message = current;
                name = noxbox.getOwner().getName();
            }
        }

        for (Map.Entry<String, Message> partyMessage : partyMessages.entrySet()) {
            Message current = partyMessage.getValue();
            if (message.getTime() < current.getTime()) {
                message = current;
                name = noxbox.getParty().getName();
            }
        }

        return message;
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
                .setContentTitle(name)
                .setContentText(message.getMessage())
                .addAction(action);
    }
}
