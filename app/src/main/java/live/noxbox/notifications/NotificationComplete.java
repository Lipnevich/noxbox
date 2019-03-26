package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.menu.history.HistoryActivity;
import live.noxbox.model.Profile;
import live.noxbox.tools.MoneyFormatter;

import static live.noxbox.menu.history.HistoryActivity.KEY_COMPLETE;
import static live.noxbox.menu.history.HistoryActivity.KEY_PERFORMER_ID;
import static live.noxbox.model.Noxbox.isNullOrZero;

public class NotificationComplete extends Notification {
    private String total;
    private Map<String, String> data;
    public NotificationComplete(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        this.data = data;

        total = data.get("total");

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_completed);

        isAutoCancel = true;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            final String currentUserId = user.getUid();

            Firestore.readNoxbox(noxboxId, noxbox -> {

                if (!isNullOrZero(noxbox.getTimeCompleted())) {
                    String message;

                    Intent lastNoxboxIntent = new Intent(context, HistoryActivity.class);
                    lastNoxboxIntent.putExtra(KEY_COMPLETE, noxbox.getTimeCompleted());
                    lastNoxboxIntent.putExtra(KEY_PERFORMER_ID, noxbox.getPerformer().getId());
                    onViewOnClickAction = TaskStackBuilder.create(context)
                            .addNextIntentWithParentStack(lastNoxboxIntent)
                            .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
                    final NotificationCompat.Builder builder = getNotificationCompatBuilder();
                    if (noxbox.getPerformer().getId().equals(currentUserId)) {
                        message = context.getResources().getString(R.string.earned);
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
