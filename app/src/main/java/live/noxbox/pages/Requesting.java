package live.noxbox.pages;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.R;
import live.noxbox.model.Notification;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.MessagingService;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS;
import static live.noxbox.state.ProfileStorage.fireProfile;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private ObjectAnimator anim;
    private AnimationDrawable animationDrawable;
    private LinearLayout requestingView;
    private CountDownTimer countDownTimer;

    public Requesting(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap);

        requestingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_requesting, null);
        requestingView.addView(child);

        ((TextView) requestingView.findViewById(R.id.blinkingInfo)).setText(R.string.connectionWithInitiator);
        requestingView.findViewById(R.id.circular_progress_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setTimeRequested(null);
                clear();
                fireProfile();
            }
        });

        anim = ObjectAnimator.ofInt(activity.findViewById(R.id.circular_progress_bar), "progress", 0, 100);
        anim.setDuration(15000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

        animationDrawable = (AnimationDrawable) activity.findViewById(R.id.blinkingInfoLayout).getBackground();
        animationDrawable.setEnterFadeDuration(600);
        animationDrawable.setExitFadeDuration(1200);
        animationDrawable.start();

        profile.getViewed().setParty(profile);
        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification()
                .setType(NotificationType.requesting)
                .setTime(String.valueOf(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS));
        messagingService.showPushNotification(notification);

        long timeCountInMilliSeconds = REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - (System.currentTimeMillis() - profile.getCurrent().getTimeRequested());
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ((TextView) requestingView.findViewById(R.id.countdownTime)).setText(String.valueOf(millisUntilFinished / 1000));
                notification.getType().updateNotification(activity.getApplicationContext(),
                        notification.setType(NotificationType.requesting).setTime(String.valueOf(millisUntilFinished / 1000)),
                        MessagingService.builder);
            }

            @Override
            public void onFinish() {
                if (profile.getCurrent().getTimeAccepted() == null) {
                    notification.getType().removeNotification(activity.getApplicationContext());
                    profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
                    profile.setCurrent(ProfileStorage.noxbox());
                    ProfileStorage.fireProfile();
                }
            }

        }.start();

        MapController.buildMapMarkerListener(googleMap, profile, activity);

        MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
    }


    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        if (anim != null && animationDrawable != null) {
            anim.cancel();
            animationDrawable.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();

        }

        requestingView.removeAllViews();
    }

    @Override
    public void onDestroy() {

    }
}
