package live.noxbox.states;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.MarketRole;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.notifications.Notification;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.SwipeButton;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Configuration.QUARTER;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.BalanceCalculator.getTotalSpentForNoxbox;
import static live.noxbox.tools.SeparateStreamForStopwatch.decimalFormat;
import static live.noxbox.tools.SeparateStreamForStopwatch.initializeStopwatch;
import static live.noxbox.tools.SeparateStreamForStopwatch.removeTimer;
import static live.noxbox.tools.SeparateStreamForStopwatch.runTimer;
import static live.noxbox.tools.SeparateStreamForStopwatch.seconds;
import static live.noxbox.tools.SeparateStreamForStopwatch.totalMoney;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private LinearLayout performingView;

    public Performing(final Activity activity, final GoogleMap googleMap) {
        this.activity = activity;
        this.googleMap = googleMap;
        MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Performing", "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeOwnerVerified()));
        Log.d(TAG + "Performing", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeOwnerVerified()));
        Log.d(TAG + "Performing", "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeOwnerVerified()));
        Log.d(TAG + "Performing", "timeStartPerforming: " + DateTimeFormatter.time(profile.getCurrent().getTimeStartPerforming()));
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        performingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_performing, null);
        performingView.addView(child);

        seconds = (System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000;
        final String price = profile.getCurrent().getPrice();
        totalMoney = new BigDecimal(price);
        totalMoney = totalMoney.multiply(QUARTER);
        drawComplete(profile);

        final Map<String, String> data = new HashMap<>();
        data.put("type", NotificationType.performing.name());
        data.put("id", profile.getNoxboxId());

        final Notification notificationPerformin = NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data);
        notificationPerformin.show();
        final long timeStartPerforming = profile.getCurrent().getTimeStartPerforming();

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                if (profile.getCurrent().getTimeCompleted() == null) {
                    seconds = (System.currentTimeMillis() - timeStartPerforming) / 1000;

                    ((TextView) performingView.findViewById(R.id.timeView)).setText(time);
                    if (hasMinimumServiceTimePassed(profile.getCurrent())) {
                        BigDecimal pricePerHour = new BigDecimal(profile.getCurrent().getPrice());
                        BigDecimal pricePerMinute = pricePerHour.divide(new BigDecimal(profile.getCurrent().getType().getMinutes()), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                        BigDecimal pricePerSecond = pricePerMinute.divide(new BigDecimal("60"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                        BigDecimal timeFromStartPerformingInMillis = new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(profile.getCurrent().getTimeStartPerforming())));
                        BigDecimal timeFromStartPerformingInSeconds = timeFromStartPerformingInMillis.divide(new BigDecimal("1000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                        totalMoney = timeFromStartPerformingInSeconds.multiply(pricePerSecond);

                        ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(decimalFormat.format(totalMoney));
                    } else {
                        ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(decimalFormat.format(totalMoney));
                    }

                    data.put("price", decimalFormat.format(totalMoney));
                    data.put("time", time);

                    if (enoughBalanceOnFiveMinutes(profile.getCurrent())) {
                        notificationPerformin.update(data);
                    } else {
                        //showLowBalanceNotification(activity.getApplicationContext(), human_profile, notification);
                        return;
                    }

                    handler.postDelayed(this, 1000);
                }
            }
        };

        initializeStopwatch(profile, handler, runnable);

        runTimer();
    }

    private void drawComplete(final Profile profile) {
        SwipeButton completeSwipeButton = performingView.findViewById(R.id.completeSwipeButton);
        completeSwipeButton.setText(activity.getResources().getString(R.string.completeText));
        completeSwipeButton.setParametrs(activity.getDrawable(R.drawable.yes), activity.getResources().getString(R.string.completeText), activity);
        completeSwipeButton.setOnTouchListener(completeSwipeButton.getButtonTouchListener(new Task<Object>() {
            @Override
            public void execute(Object object) {
                long timeCompleted = System.currentTimeMillis();

                Log.d(TAG + "Performing", "timeCompleted: " + DateTimeFormatter.time(timeCompleted));

                MessagingService messagingService = new MessagingService(activity.getApplicationContext());
                NotificationData notification = new NotificationData().setType(NotificationType.completed).setPrice(getTotalSpentForNoxbox(profile.getCurrent(), timeCompleted).toString());
                if (profile.getCurrent().getOwner().equals(profile)) {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        notification.setMessage(activity.getResources().getString(R.string.toEarn).concat(":"));
                    } else {
                        notification.setMessage(activity.getResources().getString(R.string.spent).concat(":"));
                    }
                } else {
                    if (profile.getCurrent().getRole() == MarketRole.supply) {
                        notification.setMessage(activity.getResources().getString(R.string.toEarn).concat(":"));
                    } else {
                        notification.setMessage(activity.getResources().getString(R.string.spent).concat(":"));
                    }
                }
               // messagingService.showPushNotification(notification);
                profile.getCurrent().setTimeCompleted(timeCompleted);
                updateNoxbox();
            }
        }));
    }


    @Override
    public void clear() {
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        googleMap.clear();
        removeTimer();
        performingView.removeAllViews();
        MessagingService.removeNotifications(activity);
        Log.d(TAG + "Performing", "time return to AvailableService: " + DateTimeFormatter.time(System.currentTimeMillis()));
    }


    public static boolean hasMinimumServiceTimePassed(Noxbox noxbox) {
        final Long startTime = noxbox.getTimeStartPerforming();

        if (startTime == null) return false;

        return startTime < System.currentTimeMillis() - Configuration.MINIMUM_PAYMENT_TIME_MILLIS;

    }
}
