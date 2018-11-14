package live.noxbox.pages;

import android.app.Activity;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.math.BigDecimal;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.MarketRole;
import live.noxbox.model.NotificationData;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.MessagingService;
import live.noxbox.notifications.NotificationService;
import live.noxbox.state.AppCache;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapController;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Configuration.QUARTER;
import static live.noxbox.Configuration.START_TIME;
import static live.noxbox.model.NotificationType.showLowBalanceNotification;
import static live.noxbox.model.NotificationType.updateNotification;
import static live.noxbox.state.AppCache.updateNoxbox;
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
        MapController.buildMapPosition(googleMap, activity.getApplicationContext());
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
        String price = profile.getCurrent().getPrice();
        totalMoney = new BigDecimal(price);
        totalMoney = totalMoney.multiply(QUARTER);
        drawComplete(profile);

        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final NotificationData notification = new NotificationData()
                .setType(NotificationType.performing)
                .setTime(START_TIME)
                .setPrice(decimalFormat.format(totalMoney));
        messagingService.showPushNotification(notification);

        final Handler handler = new Handler();
        final Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                if (profile.getCurrent().getTimeCompleted() == null) {
                    seconds = (System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000;

                    ((TextView) performingView.findViewById(R.id.timeView)).setText(time);
                    if (hasMinimumServiceTimePassed(profile)) {
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

                    notification.setPrice(decimalFormat.format(totalMoney));
                    notification.setTime(time);

                    if (enoughBalanceOnFiveMinutes(profile.getCurrent(), profile)) {
                        updateNotification(activity.getApplicationContext(), notification, MessagingService.builder);
                    } else {
                        showLowBalanceNotification(activity.getApplicationContext(), profile, notification);
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
                NotificationData notification = new NotificationData().setType(NotificationType.completed).setPrice(getTotalSpentForNoxbox(profile, timeCompleted).toString());
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
                messagingService.showPushNotification(notification);
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
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (profile.getCurrent().getTimeStartPerforming() != null) {
                    scheduleJob();
                }
            }
        });

        Log.d(TAG + "Performing", "time return to AvailableService: " + DateTimeFormatter.time(System.currentTimeMillis()));
    }

    private void scheduleJob() {
        activity.startService(NotificationService.newService(activity));
    }


    public static boolean hasMinimumServiceTimePassed(Profile profile) {
        final Long startTime = profile.getCurrent().getTimeStartPerforming();

        if (startTime == null) return false;

        return startTime < System.currentTimeMillis() - Configuration.MINIMUM_PAYMENT_TIME_MILLIS;

    }
}
