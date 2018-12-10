package live.noxbox.states;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.GoogleMap;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.SwipeButton;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Configuration.PRICE_FORMAT;
import static live.noxbox.Configuration.QUARTER;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.SeparateStreamForStopwatch.startHandler;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private LinearLayout performingView;
    private static long seconds = 0;
    private static BigDecimal totalMoney;
    private static final DecimalFormat decimalFormat = new DecimalFormat(PRICE_FORMAT);

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

        final long timeStartPerforming = profile.getCurrent().getTimeStartPerforming();
        seconds = (int) ((System.currentTimeMillis() - timeStartPerforming) / 1000);
        totalMoney = new BigDecimal(profile.getCurrent().getPrice()).multiply(QUARTER);
        final Task task = new Task() {
            @Override
            public void execute(Object object) {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                if (profile.getCurrent().getTimeCompleted() == null) {
                    seconds = (System.currentTimeMillis() - timeStartPerforming) / 1000;

                    ((TextView) performingView.findViewById(R.id.timeView)).setText(time);
                    if (hasMinimumServiceTimePassed(profile.getCurrent())) {
                        calculateTotalAmount(profile);

                        ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(decimalFormat.format(totalMoney));
                    } else {
                        ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(decimalFormat.format(totalMoney));
                    }


                    if (!enoughBalanceOnFiveMinutes(profile.getCurrent())) {
                        //showLowBalanceNotification(activity.getApplicationContext(), human_profile, notification);
                        return;
                    }
                    Log.d("PerformingRunnable:", this.toString());
                    Crashlytics.setString(this.toString(), "PerformingRunnable");
                }
            }
        };

        startHandler(task, 1000);

    }

    private BigDecimal calculateTotalAmount(Profile profile) {
        BigDecimal pricePerHour = new BigDecimal(profile.getCurrent().getPrice());
        BigDecimal pricePerMinute = pricePerHour.divide(new BigDecimal(profile.getCurrent().getType().getMinutes()), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal pricePerSecond = pricePerMinute.divide(new BigDecimal("60"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal timeFromStartPerformingInMillis = new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(profile.getCurrent().getTimeStartPerforming())));
        BigDecimal timeFromStartPerformingInSeconds = timeFromStartPerformingInMillis.divide(new BigDecimal("1000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        return timeFromStartPerformingInSeconds.multiply(pricePerSecond);
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

                //Map<String, String> data = new HashMap<>();
                //data.put("type", NotificationType.completed.name());
                //data.put("price", getTotalSpentForNoxbox(profile.getCurrent(), timeCompleted).toString());
                //data.put("time", "" + timeCompleted);
                //NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();
                profile.getCurrent().setTimeCompleted(timeCompleted);
                updateNoxbox();
            }
        }));
    }


    @Override
    public void clear() {
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        googleMap.clear();
        stopHandler();
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
