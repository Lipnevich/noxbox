package live.noxbox.states;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;

import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.Constants.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.analitics.BusinessEvent.timeout;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.MapOperator.moveCopyrightLeft;
import static live.noxbox.tools.MapOperator.moveCopyrightRight;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private ObjectAnimator animationProgress;
    private AnimationDrawable animationDrawable;
    private LinearLayout requestingView;
    private static CountDownTimer countDownTimer;

    public Requesting(final GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void draw(final Profile profile) {
        moveCopyrightRight(googleMap);

        Log.d(TAG + "Requesting", "timeRequest: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
        long requestTimePassed = System.currentTimeMillis() - profile.getCurrent().getTimeRequested();
        if (requestTimePassed > REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS) {
            autoDisconnectFromService(profile);
            return;
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("type", NotificationType.requesting.name());
        data.put("time", profile.getCurrent().getTimeRequested() + "");
        NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();

        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(v -> MapOperator.buildMapPosition(googleMap, activity.getApplicationContext()));

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());

        animationProgress = ObjectAnimator.ofInt(activity.findViewById(R.id.circular_progress_bar), "progress", 0, 100);
        animationProgress.setDuration(15000);
        animationProgress.setInterpolator(new DecelerateInterpolator());
        animationProgress.start();

        View blinkingInfo = activity.findViewById(R.id.blinkingInfoLayout);
        if (blinkingInfo != null) {
            animationDrawable = (AnimationDrawable) blinkingInfo.getBackground();
            animationDrawable.setEnterFadeDuration(600);
            animationDrawable.setExitFadeDuration(1200);
            animationDrawable.start();
        }

        profile.getViewed().setParty(profile.privateInfo());

        requestingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_requesting, null);
        requestingView.addView(child);

        ((TextView) requestingView.findViewById(R.id.blinkingInfo)).setText(R.string.connectionWithInitiator);
        requestingView.findViewById(R.id.circular_progress_bar).setOnClickListener(v -> {
            Log.d(TAG + "Requesting", "timeRequest: " + "now is null");
            profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
            updateNoxbox();
        });

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - requestTimePassed, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTime = requestingView.findViewById(R.id.countdownTime);
                if (countdownTime != null) {
                    countdownTime.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                MessagingService.removeNotifications(activity);
                autoDisconnectFromService(profile);
            }

        }.start();

        MapOperator.buildMapMarkerListener(googleMap, profile, activity);
    }

    private void autoDisconnectFromService(final Profile profile) {
        if (isNullOrZero(profile.getCurrent().getTimeAccepted())) {
            long timeTimeout = System.currentTimeMillis();
            profile.getCurrent().setTimeTimeout(timeTimeout);
            AppCache.updateNoxbox();
            BusinessActivity.businessEvent(timeout);
        }
    }


    @Override
    public void clear() {
        moveCopyrightLeft(googleMap);
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.clear();
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        if (animationProgress != null && animationDrawable != null) {
            animationProgress.cancel();
            animationDrawable.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        MessagingService.removeNotifications(activity);
        requestingView.removeAllViews();
    }

}
