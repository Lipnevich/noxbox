package live.noxbox.states;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.Constants.DEFAULT_MARKER_SIZE;
import static live.noxbox.Constants.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.states.Accepting.timeoutCurrent;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private Profile profile = AppCache.profile();
    private LinearLayout requestingView;
    private CountDownTimer countDownTimer;
    private Marker memberWhoMoving;
    private long requestTimePassed;

    private boolean initiated;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;


        if (!initiated) {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
            initiated = true;
        }

        requestTimePassed = System.currentTimeMillis() - profile.getCurrent().getTimeRequested();
        if (requestTimePassed > REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS) {
            timeoutCurrent();
            return;
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("type", NotificationType.requesting.name());
        NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();

        drawPath(activity, googleMap, profile);
        createCustomMarker(profile.getCurrent(), googleMap, activity.getResources(), DEFAULT_MARKER_SIZE);
        Profile profileWhoComes = profile.getCurrent().getProfileWhoComes();
        if (profileWhoComes == null) return;

        Position memberWhoMovingPosition = profileWhoComes.getPosition();
        memberWhoMoving = MarkerCreator.drawMovingMemberMarker(profileWhoComes.getTravelMode(),
                memberWhoMovingPosition, googleMap, activity.getResources());

        drawUi(googleMap, activity);

        MapOperator.setNoxboxMarkerListener(googleMap, profile, activity);
    }

    public void drawUi(GoogleMap googleMap, MapActivity activity) {
        activity.hideUi();
        activity.findViewById(R.id.container).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        requestingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_requesting, null);
        requestingView.addView(child);

        requestingView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            GeoRealtime.offline(profile.getCurrent());
            profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
            profile.getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
            updateNoxbox();
        });

        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - requestTimePassed, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countDownTimerView = requestingView.findViewById(R.id.countdownTime);
                if (countDownTimerView != null) {
                    countDownTimerView.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                MessagingService.removeNotifications(activity);
                timeoutCurrent();
            }

        }.start();
    }

    @Override
    public void clear() {
        clearHandlers();
        clearUi();
    }

    public void clearUi() {
        clearContainer();
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
    }

    @Override
    public void clearHandlers() {
        clearContainer();
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.clear();
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        MessagingService.removeNotifications(activity);

    }

    private void clearContainer(){
        if (requestingView != null) {
            requestingView.removeAllViews();
            requestingView = null;
        }
    }

}
