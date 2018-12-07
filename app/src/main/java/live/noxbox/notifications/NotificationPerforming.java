package live.noxbox.notifications;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

import java.math.BigDecimal;
import java.util.Map;

import live.noxbox.Configuration;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.Firestore;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Configuration.START_TIME;
import static live.noxbox.states.Performing.hasMinimumServiceTimePassed;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.seconds;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

public class NotificationPerforming extends Notification {


    public NotificationPerforming(Context context, Profile profile, Map<String, String> data) {
        super(context, profile, data);
        vibrate = null;
        sound = null;

        contentView = new RemoteViews(context.getPackageName(), R.layout.notification_performing);
        contentView.setTextViewText(R.id.timeHasPassed, context.getResources().getString(type.getContent()));
        contentView.setTextViewText(R.id.stopwatch, START_TIME);

        deleteIntent = createOnDeleteIntent(context, type.getGroup());

        isAlertOnce = true;
        onViewOnClickAction = TaskStackBuilder.create(context)
                .addNextIntentWithParentStack(new Intent(context, MapActivity.class))
                .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);


    }

    private NotificationCompat.Builder builder;

    @Override
    public void show() {
        builder = getNotificationCompatBuilder();
        if (profile != null) {
            getNotificationService(context).notify(type.getGroup(), builder.build());
        } else {
            Firestore.readNoxbox(noxboxId, new Task<Noxbox>() {
                @Override
                public void execute(final Noxbox noxbox) {
                    if (noxbox.getTimeCompleted() == null) {
                        final long timeStartPerforming = noxbox.getTimeStartPerforming();
                        boolean isPerforming = true;
                        if (MessagingService.inForeground()) {
                            while (isPerforming) {
                                long hours = seconds / 3600;
                                long minutes = (seconds % 3600) / 60;
                                long secs = seconds % 60;
                                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                                if (noxbox.getTimeCompleted() == null) {
                                    seconds = (System.currentTimeMillis() - timeStartPerforming) / 1000;

                                    if (hasMinimumServiceTimePassed(noxbox)) {
                                        BigDecimal pricePerHour = new BigDecimal(noxbox.getPrice());
                                        BigDecimal pricePerMinute = pricePerHour.divide(new BigDecimal(noxbox.getType().getMinutes()), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                                        BigDecimal pricePerSecond = pricePerMinute.divide(new BigDecimal("60"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                                        BigDecimal timeFromStartPerformingInMillis = new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(noxbox.getTimeStartPerforming())));
                                        BigDecimal timeFromStartPerformingInSeconds = timeFromStartPerformingInMillis.divide(new BigDecimal("1000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                                        totalMoney = timeFromStartPerformingInSeconds.multiply(pricePerSecond);
                                    }

                                    data.put("price", "" + decimalFormat.format(totalMoney));
                                    data.put("time", time);

                                    if (enoughBalanceOnFiveMinutes(noxbox)) {
                                        update(data);
                                    } else {
                                        //showLowBalanceNotification(activity.getApplicationContext(), human_profile, notification);
                                        isPerforming = false;
                                    }
                                }
                            }
                        } else {
                            contentView.setTextViewText(R.id.timeHasPassed, "");
                            contentView.setTextViewText(R.id.toPay, "");
                            contentView.setTextViewText(R.id.stopwatch, "Услуга выполняется");
                            contentView.setTextViewText(R.id.totalPayment, "");
                            getNotificationService(context).notify(type.getGroup(), builder.build());
                        }

                    }
                }
            });
        }
    }

    @Override
    public void update(Map<String, String> data) {
        contentView.setTextViewText(R.id.stopwatch, data.get("time"));
        contentView.setTextViewText(R.id.totalPayment, (data.get("price").concat(" ")).concat(Configuration.CURRENCY));
        updateNotification(context, builder);
    }
}
