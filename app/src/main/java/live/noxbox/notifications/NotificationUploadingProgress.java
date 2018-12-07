package live.noxbox.notifications;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import com.google.common.base.MoreObjects;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.Profile;

public class NotificationUploadingProgress extends Notification {

    private final int maxProgress = 100;
    private int progress;

    public NotificationUploadingProgress(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        progress = Integer.valueOf(MoreObjects.firstNonNull(data.get("progress"), "0"));

        vibrate = getVibrate();
        sound = getSound();

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_uploading_progress);
        contentView.setTextViewText(R.id.uploadingProgress, format(context.getResources(), type.getContent(), progress));
        contentView.setProgressBar(R.id.progress, maxProgress, progress, false);

        onViewOnClickAction = null;

        deleteIntent = createOnDeleteIntent(context, type.getGroup());
    }

    private NotificationCompat.Builder notificationBuilder;

    @Override
    public void show() {
        notificationBuilder = getNotificationCompatBuilder();
        getNotificationService(context).notify(type.getGroup(), notificationBuilder.build());
    }

    @Override
    public void update(Map<String, String> data) {
        progress = Integer.parseInt(data.get("progress"));
        contentView.setTextViewText(R.id.uploadingProgress, format(context.getResources(), type.getContent(), progress));
        contentView.setProgressBar(R.id.progress, maxProgress, progress, false);
        updateNotification(context, notificationBuilder);
    }
}
