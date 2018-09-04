package live.noxbox.pages;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.os.CountDownTimer;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.PathFinder;

import static live.noxbox.state.ProfileStorage.fireProfile;

public class Accepting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private ObjectAnimator anim;
    private AnimationDrawable animationDrawable;
    private CountDownTimer countDownTimer;

    public Accepting(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        ((TextView) activity.findViewById(R.id.blinkingInfo)).setText(R.string.acceptingConfirmation);
        activity.findViewById(R.id.countdownLayout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.blinkingInfoLayout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.timeLayout).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.circular_progress_bar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setTimeAccepted(null);
                profile.getCurrent().setTimeRequested(null);
                clear();
                fireProfile();
            }
        });

        activity.findViewById(R.id.acceptButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                ProfileStorage.fireProfile();
            }
        });
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);



        anim = ObjectAnimator.ofInt(activity.findViewById(R.id.circular_progress_bar), "progress", 0, 100);
        anim.setDuration(15000);
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();

        animationDrawable = (AnimationDrawable) activity.findViewById(R.id.blinkingInfoLayout).getBackground();
        animationDrawable.setEnterFadeDuration(600);
        animationDrawable.setExitFadeDuration(1200);
        animationDrawable.start();

        PathFinder.createRequestPoints(profile.getCurrent(), googleMap, activity);

        long timeCountInMilliSeconds = 30000;
        countDownTimer = new CountDownTimer(timeCountInMilliSeconds, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                ((TextView)activity.findViewById(R.id.countdownTime)).setText(String.valueOf(millisUntilFinished/1000));
            }

            @Override
            public void onFinish() {
                profile.getCurrent().setTimeAccepted(null);
                profile.getCurrent().setTimeRequested(null);
                ProfileStorage.fireProfile();
            }

        }.start();
        if(profile.equals(profile.getCurrent().getOwner())){
            moveCamera(profile.getCurrent().getPosition().toLatLng(),12);
        }
    }
    private void moveCamera(LatLng latLng, float zoom) {
        CameraPosition cameraPosition
                = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);

    }
    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.blinkingInfoLayout).setVisibility(View.GONE);
        activity.findViewById(R.id.timeLayout).setVisibility(View.GONE);
        activity.findViewById(R.id.countdownLayout).setVisibility(View.GONE);
        activity.findViewById(R.id.acceptButton).setVisibility(View.GONE);
        ((TextView) activity.findViewById(R.id.blinkingInfo)).setText("");
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);

        if (anim != null && animationDrawable != null) {
            anim.cancel();
            animationDrawable.stop();
        }
        countDownTimer.cancel();
    }
}
