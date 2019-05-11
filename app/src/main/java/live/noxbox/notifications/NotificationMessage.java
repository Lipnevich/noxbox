package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.google.common.base.Strings;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.activities.ChatActivity;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;

public class NotificationMessage extends Notification {

    private String profession;
    private String name;
    private String message;

    public NotificationMessage(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        name = data.get("name");
        profession = context.getResources().getString(NoxboxType.valueOf(data.get("noxboxType")).getProfession());
        message = data.get("message");

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_message);

        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, ChatActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        isAutoCancel = true;
    }

    @Override
    public void show() {
        contentView.setTextViewText(R.id.profession, Strings.isNullOrEmpty(name) ? profession : name);
        contentView.setTextViewText(R.id.message, message);
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
