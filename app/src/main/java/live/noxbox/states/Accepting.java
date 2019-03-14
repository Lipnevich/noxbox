package live.noxbox.states;

import android.app.Activity;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.math.BigDecimal;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.Constants.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.analitics.BusinessActivity.businessEvent;
import static live.noxbox.analitics.BusinessEvent.accept;
import static live.noxbox.analitics.BusinessEvent.timeout;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.BalanceCalculator.enoughBalance;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.MapOperator.drawPath;

public class Accepting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private Profile profile = profile();
    private CountDownTimer countDownTimer;
    private LinearLayout acceptingView;

    private Position memberWhoMovingPosition;
    private Marker memberWhoMoving;
    private boolean initiated;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;

        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        if (!initiated) {
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());

            if (profile.getCurrent().getRole() == MarketRole.supply &&
                    !enoughBalance(profile.getCurrent(), profile.getCurrent().getParty())) {
                checkBalance(profile.getCurrent().getParty(), activity, balance -> {

                    profile.getCurrent().setTimeCanceledByOwner(System.currentTimeMillis());
                    if (balance.compareTo(BigDecimal.ZERO) == 0) {
                        profile.getCurrent().setCancellationReasonMessage("Zero balance " + balance + " on payer account");
                    } else {
                        profile.getCurrent().setCancellationReasonMessage("Low balance " + balance + " on payer account");
                    }
                    updateNoxbox();
                });
            }
            initiated = true;
        }

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        drawPath(activity, googleMap, profile);
        Profile profileWhoComes = profile.getCurrent().getProfileWhoComes();
        if (profileWhoComes == null) return;

        memberWhoMovingPosition = profileWhoComes.getPosition();
        memberWhoMoving = MarkerCreator.createMovingMemberMarker(profileWhoComes.getTravelMode(),
                memberWhoMovingPosition, googleMap, activity.getResources());

        acceptingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_accepting, null);
        acceptingView.addView(child);

        acceptingView.findViewById(R.id.joinMapButton).setOnClickListener(v -> acceptCurrent());

        long requestTimePassed = System.currentTimeMillis() - profile.getCurrent().getTimeRequested();
        if (requestTimePassed > REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS) {
            autoDisconnectFromService(profile);
            return;
        }

        countDownTimer = new CountDownTimer(REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS - requestTimePassed, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                TextView countdownTimeView = acceptingView.findViewById(R.id.countdownTime);
                if (countdownTimeView != null) {
                    countdownTimeView.setText(String.valueOf(millisUntilFinished / 1000));
                }
            }

            @Override
            public void onFinish() {
                timeoutCurrent();
            }
        }.start();

        MapOperator.buildMapMarkerListener(googleMap, profile, activity);
    }

    public static void timeoutCurrent() {
        if (isNullOrZero(profile().getCurrent().getTimeAccepted())
                && isNullOrZero(profile().getCurrent().getTimeCompleted())
                && isNullOrZero(profile().getCurrent().getTimeRemoved())
                && isNullOrZero(profile().getCurrent().getTimeCanceledByOwner())
                && isNullOrZero(profile().getCurrent().getTimeCanceledByParty())
                && isNullOrZero(profile().getCurrent().getTimeTimeout())
                && !profile().getCurrent().getFinished()) {

            GeoRealtime.offline(profile().getCurrent());
            businessEvent(timeout);
            profile().getCurrent().setTimeTimeout(System.currentTimeMillis());
            updateNoxbox();
        }
    }

    public static void acceptCurrent() {
        profile().getCurrent().setTimeAccepted(System.currentTimeMillis());
        profile().getCurrent().getOwner().setPhoto(profile().getPhoto());
        profile().getCurrent().getOwner().setName(profile().getName());
        profile().getCurrent().getOwner().setWallet(profile().getWallet());
        updateNoxbox();

        businessEvent(accept);
    }

    private void autoDisconnectFromService(final Profile profile) {
        if (isNullOrZero(profile.getCurrent().getTimeAccepted())
                && !profile.getCurrent().getFinished()) {
            profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
            updateNoxbox();
            businessEvent(timeout);
        }
    }

    @Override
    public void clear() {
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.clear();
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        MessagingService.removeNotifications(activity);
        if(acceptingView != null){
            acceptingView.removeAllViews();
            acceptingView = null;
        }
    }

}
