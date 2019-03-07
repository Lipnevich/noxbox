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
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.Constants.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.analitics.BusinessEvent.timeout;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MapOperator.moveCopyrightLeft;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private Profile profile = AppCache.profile();
    private LinearLayout requestingView;
    private CountDownTimer countDownTimer;
    private Marker memberWhoMoving;

    private boolean initiated;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        if (!initiated) {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
            initiated = true;
        }

        long requestTimePassed = System.currentTimeMillis() - profile.getCurrent().getTimeRequested();
        if (requestTimePassed > REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS) {
            autoDisconnectFromService(profile);
            return;
        }

        HashMap<String, String> data = new HashMap<>();
        data.put("type", NotificationType.requesting.name());
        data.put("time", profile.getCurrent().getTimeRequested() + "");
        NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data).show();

        drawPath(activity, googleMap, profile);
        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        Profile profileWhoComes = profile.getCurrent().getProfileWhoComes();
        if (profileWhoComes == null) return;

        Position memberWhoMovingPosition = profileWhoComes.getPosition();
        memberWhoMoving = MarkerCreator.createMovingMemberMarker(profileWhoComes.getTravelMode(),
                memberWhoMovingPosition, googleMap, activity.getResources());

        requestingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_requesting, null);
        requestingView.addView(child);

        requestingView.findViewById(R.id.cancelButton).setOnClickListener(v -> {
            profile.getCurrent().setTimeCanceledByParty(System.currentTimeMillis());
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
                autoDisconnectFromService(profile);
            }

        }.start();

        MapOperator.buildMapMarkerListener(googleMap, profile, activity);
    }

    private void autoDisconnectFromService(final Profile profile) {
        if (isNullOrZero(profile.getCurrent().getTimeAccepted())
                && !profile.getCurrent().getFinished()) {
            profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
            updateNoxbox();
            BusinessActivity.businessEvent(timeout);
        }
    }

    @Override
    public void clear() {
        moveCopyrightLeft(googleMap);
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.clear();
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        MessagingService.removeNotifications(activity);
        if (requestingView != null) {
            requestingView.removeAllViews();
            requestingView = null;
        }
    }

}
