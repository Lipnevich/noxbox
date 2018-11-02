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
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.MessagingService;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS;
import static live.noxbox.state.ProfileStorage.readProfile;
import static live.noxbox.state.ProfileStorage.updateNoxbox;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private ObjectAnimator anim;
    private AnimationDrawable animationDrawable;
    private LinearLayout requestingView;
    private static CountDownTimer countDownTimer;

    public Requesting(final GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });
    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Requesting", "timeRequest: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));

        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());

        requestingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_requesting, null);
        requestingView.addView(child);

        ((TextView) requestingView.findViewById(R.id.blinkingInfo)).setText(R.string.connectionWithInitiator);
        requestingView.findViewById(R.id.circular_progress_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG + "Requesting", "timeRequest: " + "now is null");
                profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
                updateNoxbox();
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

        profile.getViewed().setParty(profile.notPublicInfo());
        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification()
                .setType(NotificationType.requesting)
                .setTime(String.valueOf(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS));
        messagingService.showPushNotification(notification);

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTime = requestingView.findViewById(R.id.countdownTime);
                if (countdownTime != null) {
                    ((TextView)countdownTime.findViewById(R.id.countdownTime)).setText(String.valueOf(millisUntilFinished / 1000));
                }
                NotificationType.updateNotification(activity.getApplicationContext(),
                        notification.setType(NotificationType.requesting).setTime(String.valueOf(millisUntilFinished / 1000)),
                        MessagingService.builder);
            }

            @Override
            public void onFinish() {
                if (profile.getCurrent().getTimeAccepted() == null) {
                    NotificationType.removeNotifications(activity.getApplicationContext());

                    long timeTimeout = System.currentTimeMillis();
                    Log.d(TAG + "Requesting", "timeTimeout: " + DateTimeFormatter.time(timeTimeout));
                    profile.getCurrent().setTimeTimeout(timeTimeout);
                    ProfileStorage.updateNoxbox();
                }
            }

        }.start();

        MapController.buildMapMarkerListener(googleMap, profile, activity);


    }


    @Override
    public void clear() {
        MapController.clearMapMarkerListener(googleMap);
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

}
