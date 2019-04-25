package live.noxbox.states;

import android.app.Activity;
import android.content.res.Configuration;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.Constants;
import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.Task;

import static live.noxbox.analitics.BusinessEvent.chatting;
import static live.noxbox.analitics.BusinessEvent.complete;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.SeparateStreamForStopwatch.startHandler;
import static live.noxbox.tools.SeparateStreamForStopwatch.stopHandler;

public class Performing implements State {

    private Activity activity;
    private GoogleMap googleMap;
    private final Profile profile = AppCache.profile();

    private long seconds = 0;
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
    private LinearLayout timeLayout;
    private LinearLayout.LayoutParams textParams;
    private TextView noxboxTypeNameText;
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
        drawTimeView();
        drawComplete();

        container.addView(rootLayout);

        seconds = Math.max(0, (System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);

        final long timeStartPerforming = profile.getCurrent().getTimeStartPerforming();
        seconds = Math.max((System.currentTimeMillis() - timeStartPerforming) / 1000, 0);

        final Task task = object -> {
            long hours = seconds / 3600;
            long minutes = (seconds % 3600) / 60;
            long secs = seconds % 60;
            String time = String.format("%d:%02d:%02d", hours, minutes, secs);


            if (isNullOrZero(profile.getCurrent().getTimeCompleted())) {
                seconds = Math.max((System.currentTimeMillis() - timeStartPerforming) / 1000, 0);

                timeView.setText(time);
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
            textParams.setMargins(0, 72, 0, 0);
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

    private void drawTimeView() {
        if (rootLayout == null) return;
        timePassedText = new TextView(activity);
        timeView = new TextView(activity);
        noxboxTypeNameText = new TextView(activity);

        timePassedText.setPadding(0, 0, 0, 0);
        timePassedText.setText(R.string.timePassed);
        timePassedText.setTextColor(textColorInt);
        timePassedText.setTextSize(defaultTextSize);

        timeView.setPadding(0, 0, 0, 0);
        timeView.setTextColor(textColorInt);
        timeView.setTextSize(titleTextSize);


        noxboxTypeNameText.setPadding(0, 0, 0, 0);
        switch (profile.getCurrent().getType()) {
            case nanny:
                noxboxTypeNameText.setText("");
                break;
            default:
                noxboxTypeNameText.setText(profile.getCurrent().getType().getName());
        }
        noxboxTypeNameText.setTextColor(textColorInt);
        noxboxTypeNameText.setTextSize(defaultTextSize);

        if (screenOrientation == Configuration.ORIENTATION_PORTRAIT) {
            timePassedText.setGravity(Gravity.CENTER_HORIZONTAL);
            timeView.setGravity(Gravity.CENTER_HORIZONTAL);
            noxboxTypeNameText.setGravity(Gravity.CENTER_HORIZONTAL);

            rootLayout.addView(noxboxTypeNameText, textParams);
            rootLayout.addView(timePassedText, textParams);
            rootLayout.addView(timeView, textParams);
        } else {
            timeLayout = new LinearLayout(activity);
            timeLayout.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams timeLayoutParams = new LinearLayout.LayoutParams(wrapContent, wrapContent);
            timeLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;

            timeLayout.addView(noxboxTypeNameText, textParams);
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
                profile.getCurrent().setTimeCompleted(timeCompleted);
                profile.getCurrent().setTimeRatingUpdated(timeCompleted);
                profile.getCurrent().setTimeOwnerLiked(timeCompleted);
                profile.getCurrent().setTimePartyLiked(timeCompleted);

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

}
