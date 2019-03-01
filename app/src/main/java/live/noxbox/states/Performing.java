package live.noxbox.states;

import android.app.Activity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.maps.GoogleMap;

import java.math.BigDecimal;
import java.util.HashMap;

import in.shadowfax.proswipebutton.ProSwipeButton;
import live.noxbox.Constants;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.BalanceChecker;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.Task;

import static live.noxbox.Constants.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Constants.QUARTER;
import static live.noxbox.analitics.BusinessEvent.chatting;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.MoneyFormatter.format;
import static live.noxbox.tools.SeparateStreamForStopwatch.startHandler;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private Profile profile = AppCache.profile();
    private LinearLayout performingView;
    private static long seconds = 0;
    private static BigDecimal totalMoney;
    private boolean initiated;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        if(!initiated) {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
            GeoRealtime.removePosition(profile.getCurrent().getId());
            initiated = true;
        }
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        performingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_performing, null);
        performingView.addView(child);

        seconds = Math.max(0, (System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        totalMoney = new BigDecimal(profile.getCurrent().getPrice());
        totalMoney = totalMoney.multiply(QUARTER);
        drawComplete(profile);

        final long timeStartPerforming = profile.getCurrent().getTimeStartPerforming();
        seconds = (int) ((System.currentTimeMillis() - timeStartPerforming) / 1000);
        final Task task = new Task() {
            @Override
            public void execute(Object object) {
                long hours = seconds / 3600;
                long minutes = (seconds % 3600) / 60;
                long secs = seconds % 60;
                String time = String.format("%d:%02d:%02d", hours, minutes, secs);


                if (isNullOrZero(profile.getCurrent().getTimeCompleted())) {
                    seconds = (System.currentTimeMillis() - timeStartPerforming) / 1000;

                    ((TextView) performingView.findViewById(R.id.timeView)).setText(time);
                    if (hasMinimumServiceTimePassed(profile.getCurrent())) {
                        totalMoney = calculateTotalAmount(profile);
                        ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(format(totalMoney));
                    } else {
                        ((TextView) performingView.findViewById(R.id.moneyToPay)).setText(format(totalMoney));
                    }

                    // TODO (vl) по клику на экран, обновляем баланс и максимальное время из блокчейна
                    if (!enoughBalanceOnFiveMinutes(profile.getCurrent())) {
                        BalanceChecker.checkBalance(profile, activity, o -> {
                            // TODO (vl) обновляем максимальное время из блокчейна

                            HashMap<String, String> data = new HashMap<>();
                            data.put("type", NotificationType.lowBalance.name());
                            NotificationFactory.buildNotification(activity, profile, data).show();
                        });


                        stopHandler();
                        return;
                    }
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
        ProSwipeButton proSwipeBtn = activity.findViewById(R.id.proSwipeButton);
        proSwipeBtn.setOnSwipeListener(() -> {
            proSwipeBtn.setArrowColor(activity.getResources().getColor(R.color.fullTranslucent));
            new android.os.Handler().postDelayed(() -> {
                long timeCompleted = System.currentTimeMillis();
                profile.getCurrent().setTotal(totalMoney.toString());
                profile.getCurrent().setTimeCompleted(timeCompleted);

                BusinessActivity.businessEvent(chatting);
                updateNoxbox();
            }, 0);
        });
    }


    @Override
    public void clear() {
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        googleMap.clear();
        stopHandler();
        performingView.removeAllViews();
        MessagingService.removeNotifications(activity);
    }


    public static boolean hasMinimumServiceTimePassed(Noxbox noxbox) {
        final Long startTime = noxbox.getTimeStartPerforming();

        if (startTime == 0L) return false;

        return startTime < System.currentTimeMillis() - Constants.MINIMUM_PAYMENT_TIME_MILLIS;
    }
}
