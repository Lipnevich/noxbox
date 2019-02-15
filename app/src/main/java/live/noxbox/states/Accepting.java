package live.noxbox.states;

import android.app.Activity;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.math.BigDecimal;

import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.Constants.REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS;
import static live.noxbox.analitics.BusinessEvent.accept;
import static live.noxbox.analitics.BusinessEvent.timeout;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.BalanceCalculator.enoughBalance;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.MapOperator.drawPath;

public class Accepting implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private CountDownTimer countDownTimer;
    private LinearLayout acceptingView;

    private Position memberWhoMovingPosition;
    private Marker memberWhoMoving;

    public Accepting(final GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());

        AppCache.readProfile(profile -> {
            if (profile.getCurrent().getRole() == MarketRole.demand &&
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
        });

    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Accepting", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        drawPath(activity, googleMap, profile);
        memberWhoMovingPosition = profile.getCurrent().getProfileWhoComes().getPosition();
        if (memberWhoMoving == null) {
            memberWhoMoving = MarkerCreator.createMovingMemberMarker(profile.getCurrent().getProfileWhoComes().getTravelMode(),
                    memberWhoMovingPosition, googleMap, activity.getResources());
        } else {
            memberWhoMoving.setPosition(memberWhoMovingPosition.toLatLng());
        }

        acceptingView = activity.findViewById(R.id.container);
        View child = activity.getLayoutInflater().inflate(R.layout.state_accepting, null);
        acceptingView.addView(child);

        acceptingView.findViewById(R.id.joinButton).setOnClickListener(v -> {
            profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
            profile.getCurrent().getOwner().setPhoto(profile.getPhoto());
            profile.getCurrent().getOwner().setName(profile.getName());
            profile.getCurrent().getOwner().setWallet(profile.getWallet());

            updateNoxbox();

            BusinessActivity.businessEvent(accept);
        });

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
                if (isNullOrZero(profile.getCurrent().getTimeAccepted())
                        && isNullOrZero(profile.getCurrent().getTimeCompleted())
                        && isNullOrZero(profile.getCurrent().getTimeRemoved())
                        && isNullOrZero(profile.getCurrent().getTimeCanceledByOwner())
                        && isNullOrZero(profile.getCurrent().getTimeCanceledByParty())
                        && isNullOrZero(profile.getCurrent().getTimeTimeout())) {

                    BusinessActivity.businessEvent(timeout);
                    profile.getCurrent().setTimeTimeout(System.currentTimeMillis());
                    updateNoxbox();
                }
            }
        }.start();

        googleMap.getUiSettings().setScrollGesturesEnabled(false);
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
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.clear();
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        MessagingService.removeNotifications(activity);
        acceptingView.removeAllViews();
    }

}
