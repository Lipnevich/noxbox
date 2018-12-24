package live.noxbox.notifications;

import android.app.PendingIntent;
import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationBalance extends Notification {

    private String balance;

    public NotificationBalance(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        balance = data.get("balance");
        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_balance);
        contentView.setTextViewText(R.id.content, "Your current balance: "
                + balance + " " + context.getString(R.string.currency));

        onViewOnClickAction = PendingIntent.getActivity(context, 0, context.getPackageManager()
                        .getLaunchIntentForPackage(context.getPackageName()),
                PendingIntent.FLAG_UPDATE_CURRENT);

        isAutoCancel = true;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());

    }

    @Override
    public void show() {
        NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());
    }
}
