package live.noxbox.states;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.location.Location;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.Configuration.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.tools.MapOperator.drawPath;

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
        MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Accepting", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        drawPath(activity, googleMap, profile);

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
                countDownTimer.cancel();
                updateNoxbox();
            }
        });

        acceptingView.findViewById(R.id.joinButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long timeAccepted = System.currentTimeMillis();
                long timeToMeet = (long) (Math.ceil(getTravelTimeInMinutes(profile)) * 60000);
                Log.d(TAG + "Accepting", "timeAccepted: " + DateTimeFormatter.time(timeAccepted));
                Log.d(TAG + "Accepting", "timeToMeet: " + DateTimeFormatter.time(timeToMeet));

                profile.getCurrent().setTimeAccepted(timeAccepted);
                profile.getCurrent().setTimeToMeet(timeToMeet);
                profile.getCurrent().setOwner(profile.privateInfo());
                profile.getCurrent().getOwner().setWallet(profile.getWallet());
                updateNoxbox();
            }
        });

        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        MapOperator.buildMapMarkerListener(googleMap, profile, activity);

        anim = ObjectAnimator.ofInt(activity.findViewById(R.id.circular_progress_bar), "progress", 0, 100);
        anim.setDuration(15000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

        animationDrawable = (AnimationDrawable) activity.findViewById(R.id.blinkingInfoLayout).getBackground();
        animationDrawable.setEnterFadeDuration(600);
        animationDrawable.setExitFadeDuration(1200);
        animationDrawable.start();

        long requestTimePassed = System.currentTimeMillis() - profile.getCurrent().getTimeRequested();
        if (requestTimePassed > REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS) {
            autoDisconnectFromService(profile);
            return;
        }

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - requestTimePassed, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTime = acceptingView.findViewById(R.id.countdownTime);
                if (countdownTime != null) {
                    countdownTime.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                if (profile.getCurrent().getTimeAccepted() == null) {
                    profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
                    updateNoxbox();
                }
            }
        }.start();
    }

    private void autoDisconnectFromService(final Profile profile) {
        if (profile.getCurrent().getTimeAccepted() == null) {
            profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
            updateNoxbox();
        }
    }

    @Override
    public void clear() {
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.clear();
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        if (anim != null && animationDrawable != null) {
            anim.cancel();
            animationDrawable.stop();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();

        }
        MessagingService.removeNotifications(activity);
        acceptingView.removeAllViews();
    }


    private Float getDistance(Profile profile) {
        double startLat = profile.getCurrent().getParty().getPosition().getLatitude();
        double startLng = profile.getCurrent().getParty().getPosition().getLongitude();
        double endLat = profile.getCurrent().getPosition().getLatitude();
        double endLng = profile.getCurrent().getPosition().getLongitude();

        float[] results = new float[1];
        Location.distanceBetween(startLat, startLng, endLat, endLng, results);
        return results[0];
    }

    private Float getTravelTimeInMinutes(Profile profile) {
        if (profile.getCurrent().getParty().getTravelMode() != TravelMode.none && profile.getCurrent().getOwner().getTravelMode() != TravelMode.none)
            return getDistance(profile) / profile.getCurrent().getParty().getTravelMode().getSpeedInMetersPerMinute();

        if (profile.getCurrent().getOwner().getTravelMode() == TravelMode.none) {
            return getDistance(profile) / profile.getCurrent().getParty().getTravelMode().getSpeedInMetersPerMinute();
        } else {
            return getDistance(profile) / profile.getCurrent().getOwner().getTravelMode().getSpeedInMetersPerMinute();
        }
    }
}
