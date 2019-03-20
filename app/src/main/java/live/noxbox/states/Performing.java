package live.noxbox.states;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.math.BigDecimal;

import live.noxbox.Constants;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.BalanceChecker;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.Task;

import static live.noxbox.Constants.DEFAULT_BALANCE_SCALE;
import static live.noxbox.Constants.QUARTER;
import static live.noxbox.analitics.BusinessEvent.chatting;
import static live.noxbox.analitics.BusinessEvent.complete;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.BalanceCalculator.enoughBalanceOnFiveMinutes;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.MoneyFormatter.format;
import static live.noxbox.tools.SeparateStreamForStopwatch.startHandler;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private final Profile profile = AppCache.profile();

    private long seconds = 0;
    private BigDecimal totalMoney;
    private boolean initiated;

    //ui
    private int screenOrientation;
    private int wrapContent = LinearLayout.LayoutParams.WRAP_CONTENT;
    private int matchParent = LinearLayout.LayoutParams.MATCH_PARENT;
    private int textColorInt;
    private int btnTextColorInt;
    private int btnBackgroundColorInt;
    private int titleTextSize;//in sp
    private int defaultTextSize;//in sp
    private CharSequence btnText;

    private LinearLayout container;
    private LinearLayout rootLayout;
    private LinearLayout moneyLayout;
    private LinearLayout timeLayout;
    private LinearLayout.LayoutParams textParams;
    private TextView earnedOrSpent;
    private TextView currencyText;
    private TextView moneyToPay;
    private TextView timePassedText;
    private TextView timeView;
    private RelativeLayout proSwipeButtonLayout;
    private ProSwipeButton serviceCompleting;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        if (!initiated) {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
            GeoRealtime.removePosition(profile.getCurrent().getId());
            initiated = true;
        }
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        if (container != null) {
            container.removeAllViews();
        }
        initializeUiVariables();

        drawContainer();
        drawRootLayout();
        drawMoneyToPay();
        drawTimeView();
        drawComplete();

        container.addView(rootLayout);

        seconds = Math.max(0, (System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        totalMoney = new BigDecimal(profile.getCurrent().getPrice());
        totalMoney = totalMoney.multiply(QUARTER);


        final long timeStartPerforming = profile.getCurrent().getTimeStartPerforming();
        seconds = Math.max((System.currentTimeMillis() - timeStartPerforming) / 1000, 0);

        final Task task = object -> {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            String time = String.format("%d:%02d:%02d", hours, minutes, secs);


            if (isNullOrZero(profile.getCurrent().getTimeCompleted())) {
                seconds = (System.currentTimeMillis() - timeStartPerforming) / 1000;

                timeView.setText(time);
                if (hasMinimumServiceTimePassed(profile.getCurrent())) {
                    totalMoney = calculateTotalAmount(profile);
                    moneyToPay.setText(format(totalMoney));
                } else {
                    moneyToPay.setText(format(totalMoney));
                }

                // TODO (vl) по клику на экран, обновляем баланс и максимальное время из блокчейна
                if (!enoughBalanceOnFiveMinutes(profile.getCurrent())) {
                    BalanceChecker.checkBalance(profile, activity, o -> {
                        // TODO (vl) обновляем максимальное время из блокчейна
                        // HashMap<String, String> data = new HashMap<>();
                        //data.put("type", NotificationType.lowBalance.name());
                        //NotificationFactory.buildNotification(activity, profile, data).show();
                    });


                    stopHandler();
                    return;
                }
            }
        };

        startHandler(task, 1000);

    }


    @Override
    public void clear() {
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        googleMap.clear();
        stopHandler();
        if (container != null) {
            container.removeAllViews();
            container = null;
        }
        MessagingService.removeNotifications(activity);
    }

    private void initializeUiVariables() {
        screenOrientation = activity.getResources().getConfiguration().orientation;
        textColorInt = activity.getResources().getColor(R.color.primary);
        textParams = new LinearLayout.LayoutParams(matchParent, wrapContent);
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            textParams.setMargins(0, 0, 0, 0);
        } else {
            textParams.setMargins(dpToPx(6), 0, 0, 0);
        }
        btnTextColorInt = activity.getResources().getColor(R.color.secondary);
        btnBackgroundColorInt = textColorInt;
        btnText = activity.getResources().getString(R.string.complete);
        titleTextSize = 58;
        defaultTextSize = 32;

    }

    private void drawContainer() {
        container = activity.findViewById(R.id.container);
    }

    private void drawRootLayout() {
        rootLayout = new LinearLayout(activity);
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(matchParent, matchParent);
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            rootParams.setMargins(0, dpToPx(12), 0, 0);
        } else {
            rootParams.setMargins(0, dpToPx(24), 0, 0);
        }
        rootLayout.setLayoutParams(rootParams);

    }


    private void drawMoneyToPay() {
        if (rootLayout == null) return;

        earnedOrSpent = new TextView(activity);
        currencyText = new TextView(activity);
        moneyToPay = new TextView(activity);

        if (profile.getCurrent().getPerformer().equals(profile)) {
            earnedOrSpent.setText(R.string.earned);
        } else {
            earnedOrSpent.setText(R.string.spent);
        }
        earnedOrSpent.setPadding(0, 0, 0, 0);
        earnedOrSpent.setTextColor(textColorInt);
        earnedOrSpent.setTextSize(defaultTextSize);


        currencyText.setPadding(0, 0, 0, 0);
        currencyText.setText(R.string.currency);
        currencyText.setTextColor(textColorInt);
        currencyText.setTextSize(defaultTextSize);


        moneyToPay.setPadding(0, 0, 0, 0);
        moneyToPay.setTextColor(textColorInt);
        moneyToPay.setTextSize(titleTextSize);

        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            earnedOrSpent.setGravity(Gravity.CENTER_HORIZONTAL);
            currencyText.setGravity(Gravity.CENTER_HORIZONTAL);
            moneyToPay.setGravity(Gravity.CENTER_HORIZONTAL);

            rootLayout.addView(earnedOrSpent, textParams);
            rootLayout.addView(currencyText, textParams);
            rootLayout.addView(moneyToPay, textParams);
        } else {
            moneyLayout = new LinearLayout(activity);
            moneyLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams moneyLayoutParams = new LinearLayout.LayoutParams(wrapContent, wrapContent);
            moneyLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;


            moneyLayout.addView(earnedOrSpent, textParams);
            moneyLayout.addView(currencyText, textParams);
            moneyLayout.addView(moneyToPay, textParams);

            rootLayout.addView(moneyLayout, moneyLayoutParams);
        }

    }

    private void drawTimeView() {
        if (rootLayout == null) return;
        timePassedText = new TextView(activity);
        timeView = new TextView(activity);

        timePassedText.setPadding(0, 0, 0, 0);
        timePassedText.setText(R.string.timePassed);

        timePassedText.setTextColor(textColorInt);
        timePassedText.setTextSize(defaultTextSize);

        timeView.setPadding(0, 0, 0, 0);
        timeView.setTextColor(textColorInt);
        timeView.setTextSize(titleTextSize);

        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            timePassedText.setGravity(Gravity.CENTER_HORIZONTAL);
            timeView.setGravity(Gravity.CENTER_HORIZONTAL);

            rootLayout.addView(timePassedText, textParams);
            rootLayout.addView(timeView, textParams);
        } else {
            timeLayout = new LinearLayout(activity);
            timeLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams timeLayoutParams = new LinearLayout.LayoutParams(wrapContent, wrapContent);
            timeLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

            timeLayout.addView(timePassedText, textParams);
            timeLayout.addView(timeView, textParams);

            rootLayout.addView(timeLayout, timeLayoutParams);
        }

    }

    private void drawComplete() {
        proSwipeButtonLayout = new RelativeLayout(activity);
        LinearLayout.LayoutParams layoutParamsForProSwipeLayout = new LinearLayout.LayoutParams(matchParent, matchParent);
        RelativeLayout.LayoutParams serviceCompletingParams = new RelativeLayout.LayoutParams(matchParent, wrapContent);
        serviceCompletingParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            serviceCompletingParams.setMargins(dpToPx(16), dpToPx(32), dpToPx(16), dpToPx(46));
        } else {
            serviceCompletingParams.setMargins(dpToPx(46), dpToPx(12), dpToPx(46), dpToPx(46));
        }


        serviceCompleting = new live.noxbox.states.ProSwipeButton(activity, btnTextColorInt, btnBackgroundColorInt, btnText);
        serviceCompleting.setOnSwipeListener(() -> {
            serviceCompleting.setArrowColor(activity.getResources().getColor(R.color.fullTranslucent));
            new android.os.Handler().postDelayed(() -> {
                long timeCompleted = System.currentTimeMillis();
                profile.getCurrent().setTotal(totalMoney.toString());
                profile.getCurrent().setTimeCompleted(timeCompleted);
                profile.getCurrent().setTimeRatingUpdated(timeCompleted);
                BusinessActivity.businessEvent(complete);
                BusinessActivity.businessEvent(chatting);
                updateNoxbox();
            }, 0);
        });
        proSwipeButtonLayout.addView(serviceCompleting, serviceCompletingParams);
        rootLayout.addView(proSwipeButtonLayout, layoutParamsForProSwipeLayout);
    }


    public static boolean hasMinimumServiceTimePassed(Noxbox noxbox) {
        final Long startTime = noxbox.getTimeStartPerforming();

        if (startTime == 0L) return false;

        return startTime < System.currentTimeMillis() - Constants.MINIMUM_PAYMENT_TIME_MILLIS;
    }

    private BigDecimal calculateTotalAmount(Profile profile) {
        BigDecimal pricePerHour = new BigDecimal(profile.getCurrent().getPrice());
        BigDecimal pricePerMinute = pricePerHour.divide(new BigDecimal(profile.getCurrent().getType().getMinutes()), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal pricePerSecond = pricePerMinute.divide(new BigDecimal("60"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        BigDecimal timeFromStartPerformingInMillis = new BigDecimal(String.valueOf(System.currentTimeMillis())).subtract(new BigDecimal(String.valueOf(profile.getCurrent().getTimeStartPerforming())));
        BigDecimal timeFromStartPerformingInSeconds = timeFromStartPerformingInMillis.divide(new BigDecimal("1000"), DEFAULT_BALANCE_SCALE, BigDecimal.ROUND_HALF_DOWN);
        return timeFromStartPerformingInSeconds.multiply(pricePerSecond);
    }
}
