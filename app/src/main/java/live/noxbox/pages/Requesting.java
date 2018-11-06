package live.noxbox.pages;

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

import live.noxbox.R;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.state.AppCache;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.MessagingService;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS;
import static live.noxbox.state.AppCache.updateNoxbox;

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
        MapController.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Requesting", "timeRequest: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));

        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapController.buildMapPosition(googleMap, activity.getApplicationContext());
            }
        });

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());

        animationProgress = ObjectAnimator.ofInt(activity.findViewById(R.id.circular_progress_bar), "progress", 0, 100);
        animationProgress.setDuration(15000);
        animationProgress.setInterpolator(new DecelerateInterpolator());
        animationProgress.start();

        // TODO (vl) lead to null pointer
        if(activity.findViewById(R.id.blinkingInfoLayout) != null) {
            animationDrawable = (AnimationDrawable) activity.findViewById(R.id.blinkingInfoLayout).getBackground();
            animationDrawable.setEnterFadeDuration(600);
            animationDrawable.setExitFadeDuration(1200);
            animationDrawable.start();
        }

        profile.getViewed().setParty(profile.notPublicInfo());
        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification()
                .setType(NotificationType.requesting)
                .setTime(String.valueOf(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS));
        messagingService.showPushNotification(notification);


        requestingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_requesting, null);
        requestingView.addView(child);

        ((TextView) requestingView.findViewById(R.id.blinkingInfo)).setText(R.string.connectionWithInitiator);
        requestingView.findViewById(R.id.circular_progress_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG + "Requesting", "timeRequest: " + "now is null");
                NotificationType.removeNotifications(activity.getApplicationContext());
                profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                updateNoxbox();
            }
        });

        long requestTimePassed = System.currentTimeMillis() - profile.getCurrent().getTimeRequested();
        if (requestTimePassed > REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS) {
            autoDisconnectFromService(profile);
            return;
        }

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - requestTimePassed, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTime = requestingView.findViewById(R.id.countdownTime);
                if (countdownTime != null) {
                    ((TextView) countdownTime.findViewById(R.id.countdownTime)).setText(String.valueOf(millisUntilFinished / 1000));
                }
                NotificationType.updateNotification(activity.getApplicationContext(),
                        notification.setType(NotificationType.requesting).setTime(String.valueOf(millisUntilFinished / 1000)),
                        MessagingService.builder);
            }

            @Override
            public void onFinish() {
                autoDisconnectFromService(profile);
            }

        }.start();

        MapController.buildMapMarkerListener(googleMap, profile, activity);


    }

    private void autoDisconnectFromService(final Profile profile) {
        if (profile.getCurrent().getTimeAccepted() == null) {
            NotificationType.removeNotifications(activity.getApplicationContext());
            long timeTimeout = System.currentTimeMillis();
            Log.d(TAG + "Requesting", "timeTimeout: " + DateTimeFormatter.time(timeTimeout));
            profile.getCurrent().setTimeTimeout(timeTimeout);
            AppCache.updateNoxbox();
        }
    }


    @Override
    public void clear() {
        MapController.clearMapMarkerListener(googleMap);
        googleMap.clear();
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        if (animationProgress != null && animationDrawable != null) {
            animationProgress.cancel();
            animationDrawable.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();

        }

        requestingView.removeAllViews();
    }

}
