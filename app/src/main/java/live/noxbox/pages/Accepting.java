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

public class Accepting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private ObjectAnimator anim;
    private AnimationDrawable animationDrawable;
    private CountDownTimer countDownTimer;
    private LinearLayout acceptingView;


    public Accepting(final GoogleMap googleMap, final Activity activity) {
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
        Log.d(TAG + "Accepting", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        acceptingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_accepting, null);
        acceptingView.addView(child);

        ((TextView) acceptingView.findViewById(R.id.blinkingInfo)).setText(R.string.acceptingConfirmation);
        acceptingView.findViewById(R.id.circular_progress_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeCanceled = System.currentTimeMillis();
                Log.d(TAG + "Accepting", "timeCanceledByOwner: " + DateTimeFormatter.time(timeCanceled));
                profile.getCurrent().setTimeCanceledByOwner(timeCanceled);
                updateNoxbox();
            }
        });

        acceptingView.findViewById(R.id.joinButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeAccepted = System.currentTimeMillis();
                Log.d(TAG + "Accepting", "timeAccepted: " + DateTimeFormatter.time(timeAccepted));
                profile.getCurrent().setTimeAccepted(timeAccepted);
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


        final MessagingService messagingService = new MessagingService(activity.getApplicationContext());
        final Notification notification = new Notification()
                .setType(NotificationType.accepting)
                .setTime(String.valueOf(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS));
        messagingService.showPushNotification(notification);

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTime = acceptingView.findViewById(R.id.countdownTime);
                if (countdownTime != null) {
                    countdownTime.setText(String.valueOf(millisUntilFinished / 1000));
                    NotificationType.updateNotification(activity.getApplicationContext(),
                            notification.setType(NotificationType.accepting).setTime(String.valueOf(millisUntilFinished / 1000)),
                            MessagingService.builder);
                }
            }

            @Override
            public void onFinish() {
                if (profile.getCurrent().getTimeAccepted() == null) {
                    NotificationType.removeNotifications(activity.getApplicationContext());
                    profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
                    updateNoxbox();
                }
            }

        }.start();

        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        MapController.buildMapMarkerListener(googleMap, profile, activity);
    }

    @Override
    public void clear() {
        googleMap.clear();
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        if (anim != null && animationDrawable != null) {
            anim.cancel();
            animationDrawable.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();

        }
        acceptingView.removeAllViews();
    }

}
