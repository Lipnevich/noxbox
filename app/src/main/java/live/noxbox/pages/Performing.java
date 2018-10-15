package live.noxbox.pages;

import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.math.BigDecimal;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MessagingService;
import live.noxbox.tools.NotificationService;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Configuration.QUARTER;
import static live.noxbox.Configuration.START_TIME;
import static live.noxbox.model.NotificationType.showLowBalanceNotification;
import static live.noxbox.model.NotificationType.updateNotification;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.BalanceCalculator.getTotalSpentForNoxbox;
import static live.noxbox.tools.MapController.buildMapPosition;
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

    public Performing(Activity activity, GoogleMap googleMap) {
        this.activity = activity;
        this.googleMap = googleMap;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        buildMapPosition(googleMap, profile, activity.getApplicationContext());

        performingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_performing, null);
        performingView.addView(child);

        seconds = (int) ((System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        String price = profile.getCurrent().getPrice().replaceAll(",", "\\.");
        totalMoney = new BigDecimal(price);
        totalMoney = totalMoney.multiply(QUARTER);
        drawComplete(profile);

        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification()
                .setType(NotificationType.performing)
                .setTime(START_TIME)
                .setPrice(decimalFormat.format(totalMoney));
        messagingService.showPushNotification(notification);

        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                int hours = seconds / 3600;
                int minutes = (seconds % 3600) / 60;
                int secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                if (profile.getCurrent().getTimeCompleted() == null) {
                    seconds++;

                    ((TextView) performingView.findViewById(R.id.timeView)).setText(time);
                    if (hasMinimumServiceTimePassed(profile)) {
                        BigDecimal pricePerSecond = new BigDecimal(profile.getCurrent().getPrice());
                        pricePerSecond = pricePerSecond.divide(new BigDecimal(profile.getCurrent().getType().getDuration()), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                        pricePerSecond = pricePerSecond.divide(new BigDecimal("60"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                        totalMoney = new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(profile.getCurrent().getTimeStartPerforming())));
                        totalMoney = totalMoney.divide(new BigDecimal("1000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
                        totalMoney = totalMoney.divide(pricePerSecond, DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);

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
                profile.getCurrent().setTimeCompleted(System.currentTimeMillis());

                MessagingService messagingService = new MessagingService(activity.getApplicationContext());
                Notification notification = new Notification().setType(NotificationType.completed).setPrice(getTotalSpentForNoxbox(profile).toString());
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

                Long totalTimeInMillis = profile.getCurrent().getTimeCompleted() - profile.getCurrent().getTimeStartPerforming();
                profile.getCurrent().setPrice(decimalFormat.format(totalMoney));

                Long timeInMinutes = (totalTimeInMillis / (1000 * 60)) % 60;
                DebugMessage.popup(activity, String.valueOf(timeInMinutes) + "minutes");

                profile.getCurrent().clean(); //while debug without estimating
                ProfileStorage.fireProfile();
            }
        }));
    }


    @Override
    public void clear() {
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        googleMap.clear();
        removeTimer();
        performingView.removeAllViews();
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (profile.getCurrent().getTimeStartPerforming() != null) {
                    scheduleJob();
                }
            }
        });
    }

    private void scheduleJob() {
        activity.startService(NotificationService.newService(activity));
    }


    public static boolean hasMinimumServiceTimePassed(Profile profile) {
        final Long startTime = profile.getCurrent().getTimeStartPerforming();

        if (startTime == null) return false;

        if (startTime >= System.currentTimeMillis() - Configuration.MINIMUM_PAYMENT_TIME_MILLIS) {
            return false;
        } else {
            return true;
        }

    }
}
