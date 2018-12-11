package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.crashlytics.android.Crashlytics;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

public class NotificationAccepting extends Notification {

    private long timeRequesting;

    public NotificationAccepting(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        timeRequesting = Long.valueOf(notificationTime);

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.content, context.getResources().getString(type.getContent()));

        isAlertOnce = true;
        onViewOnClickAction = null;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    @Override
    public void show() {
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), builder.build());

        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
            Crashlytics.logException(e);
        }

        Firestore.readNoxbox(noxboxId, new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                if (noxbox.getTimeAccepted() == null) {
                    noxbox.setTimeTimeout(System.currentTimeMillis());
                    Firestore.writeNoxbox(noxbox);
                }
            }
        });
    }
}
