package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.menu.history.HistoryActivity;
import live.noxbox.model.Profile;
import live.noxbox.tools.MoneyFormatter;

import static live.noxbox.model.Noxbox.isNullOrZero;

public class NotificationComplete extends Notification {
    private String total;
    private Map<String, String> data;
    public NotificationComplete(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        this.data = data;

        total = data.get("total");

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_completed);

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, HistoryActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        isAutoCancel = true;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

            Firestore.readNoxbox(noxboxId, noxbox -> {
                final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                if (!isNullOrZero(noxbox.getTimeCompleted())) {
                    String message;

                    if (noxbox.getPerformer().getId().equals(currentUserId)) {
                        message = context.getResources().getString(R.string.toEarn);
                        contentView.setImageViewResource(R.id.estimate,R.drawable.ic_notification_human_balance);
                    } else {
                        message = context.getResources().getString(R.string.spent);
                        contentView.setImageViewResource(R.id.estimate,R.drawable.ic_notification_human_like);
                    }
                    contentView.setTextViewText(R.id.content, message + " "
                            + MoneyFormatter.scale(total)
                            + " " + context.getString(R.string.currency));
                    getNotificationService(context).notify(type.getGroup(), builder.build());

                }
            });
        }


    }
}
