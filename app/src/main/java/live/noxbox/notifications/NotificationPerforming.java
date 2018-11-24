package live.noxbox.notifications;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.util.Map;

import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.Notification;
import live.noxbox.notifications.factory.NotificationFactory;

import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.initializeStopwatch;
import static live.noxbox.tools.SeparateStreamForStopwatch.runTimer;
import static live.noxbox.tools.SeparateStreamForStopwatch.seconds;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

public class NotificationPerforming extends Notification {
    public NotificationPerforming(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_accepting);
        contentView.setTextViewText(R.id.countDownTime, String.valueOf(notificationTime));
        contentView.setTextViewText(R.id.title, context.getResources().getString(type.getTitle()));

        isAlertOnce = true;
        onViewOnClickAction = null;
    }

    @Override
    public void show() {
        //TODO (vl) использовать Thread
        final NotificationCompat.Builder builder = getNotificationCompatBuilder();
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
                    contentView.setTextViewText(R.id.stopwatch, time);
                    contentView.setTextViewText(R.id.totalPayment, decimalFormat.format(totalMoney));

                    if (enoughBalanceOnFiveMinutes(profile.getCurrent(), profile)) {
                        updateNotification(context, builder);
                    } else {
                        data.remove("type");
                        data.put("type", NotificationType.lowBalance.name());
                        NotificationFactory.showNotification(context, profile, data);
                    }


                    handler.postDelayed(this, 1000);
                }
            }
        };
        initializeStopwatch(profile, handler, runnable);
        runTimer();
    }
}
