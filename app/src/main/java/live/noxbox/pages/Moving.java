package live.noxbox.pages;

import android.app.Activity;
import android.graphics.Color;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import live.noxbox.R;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Profile;
import live.noxbox.pages.confirmation.ConfirmationActivity;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.NavigatorManager;
import live.noxbox.tools.Router;

import static live.noxbox.tools.MapController.moveCopyrightLeft;
import static live.noxbox.tools.MapController.moveCopyrightRight;
import static live.noxbox.tools.Router.startActivity;

public class Moving implements State {

    private GoogleMap googleMap;
    private Activity activity;

    private CountDownTimer countDownTimer;


    public Moving(final GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        MapController.buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Moving", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
        Log.d(TAG + "Moving", "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));

        Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                .add(profile.getPosition().toLatLng(), profile.getCurrent().getPosition().toLatLng())
                .width(5)
                .color(Color.GREEN)
                .geodesic(true));

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        MarkerCreator.createPartyMarker(profile.getCurrent().getParty(), googleMap, activity.getResources());

        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.chat).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.navigation).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);

        if (profile.getCurrent().getOwner().equals(profile)) {
            if (profile.getCurrent().getTimeOwnerVerified() != null) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        } else {
            if (profile.getCurrent().getTimePartyVerified() != null) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        }

        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.eye);


        activity.findViewById(R.id.navigation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavigatorManager.openNavigator(activity, profile);
            }
        });

        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DebugMessage.popup(activity, "way and points");
                MapController.buildMapPosition(googleMap, activity.getApplicationContext());
            }
        });


        activity.findViewById(R.id.customFloatingView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startActivity(activity, ConfirmationActivity.class);
            }
        });

        activity.findViewById(R.id.chat).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(activity, ChatActivity.class);
            }
        });

        countDownTimer = new CountDownTimer(profile.getCurrent().getTimeToMeet(), 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (countDownTimer != null) {
                    //notification.setTime(String.valueOf(profile.getCurrent().getTimeToMeet() - (profile.getCurrent().getTimeToMeet() - millisUntilFinished)));
                    //notification.setProgress((int) (profile.getCurrent().getTimeToMeet() - millisUntilFinished));
                    //NotificationType.updateNotification(activity.getApplicationContext(), notification, MessagingService.builder);
                }
            }

            @Override
            public void onFinish() {
                //MessagingService.removeNotifications(activity.getApplicationContext());
            }
        }.start();


        MapController.buildMapMarkerListener(googleMap, profile, activity);
        moveCopyrightRight(googleMap);
    }

    @Override
    public void clear() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        MapController.clearMapMarkerListener(googleMap);
        NotificationType.removeNotifications(activity.getApplicationContext());
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.chat).setVisibility(View.GONE);
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.add);
        moveCopyrightLeft(googleMap);
        googleMap.clear();

    }


}
