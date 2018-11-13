package live.noxbox.notifications.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;

import live.noxbox.R;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.tools.MessagingService;

import static live.noxbox.model.NotificationType.showLowBalanceNotification;
import static live.noxbox.model.NotificationType.updateNotification;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.initializeStopwatch;
import static live.noxbox.tools.SeparateStreamForStopwatch.runTimer;
import static live.noxbox.tools.SeparateStreamForStopwatch.seconds;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

public class NotificationPerforming extends Notification {
    public NotificationPerforming(Context context, NotificationData notificationData) {
        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.countDownTime, notificationData.getTime());
        contentView.setTextViewText(R.id.title, context.getResources().getString(notificationData.getType().getTitle()));
        contentView.setOnClickPendingIntent(R.id.accept, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.AcceptRequestListener.class), 0));
        contentView.setOnClickPendingIntent(R.id.cancel, PendingIntent.getBroadcast(context, 0, new Intent(context, NotificationType.CancelRequestListener.class), 0));

        isAlertOnce = true;
        onViewOnClickAction = null;
    }

    @Override
    public void showNotification(final Context context, final Profile profile, final NotificationData notificationData, String channelId) {
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);

                if (profile.getCurrent().getTimeCompleted() == null) {
                    seconds++;
                    Log.e("NotificationType.class", "run()");
                    notificationData.setTime(time);
                    notificationData.setPrice(decimalFormat.format(totalMoney));

                    if (enoughBalanceOnFiveMinutes(profile.getCurrent(), profile)) {
                        updateNotification(context, notificationData, MessagingService.builder);
                    } else {
                        showLowBalanceNotification(context, profile, notificationData);
                    }


                    handler.postDelayed(this, 1000);
                }
            }
        };
        initializeStopwatch(profile, handler, runnable);
        runTimer();
    }
}
