package live.noxbox.pages;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.HashMap;

import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.notifications.util.MessagingService;
import live.noxbox.pages.confirmation.ConfirmationActivity;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.NavigatorManager;
import live.noxbox.tools.Router;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static live.noxbox.Configuration.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Configuration.MINIMUM_TIME_INTERVAL_BETWEEN_RECEIVE_IN_SECONDS;
import static live.noxbox.model.MarketRole.demand;
import static live.noxbox.model.MarketRole.supply;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.MapController.moveCopyrightLeft;
import static live.noxbox.tools.MapController.moveCopyrightRight;
import static live.noxbox.tools.Router.startActivity;

public class Moving implements State {

    private GoogleMap googleMap;
    private Activity activity;

    private CountDownTimer countDownTimer;

    private LocationManager locationManager;
    private LocationListener locationListener;


    public Moving(final GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        MapController.buildMapPosition(googleMap, activity.getApplicationContext());

    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Moving", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
        Log.d(TAG + "Moving", "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));

        //TODO (vl) если текущий пользователь движется - начать слушать его GPS и обновлять noxbox
        //TODO (vl) иначе включить слушатель профиля обнавляющий пуш
        if (defineLocationListener(profile)) {
            registerLocationListener(profile);
        } else {
            HashMap<String, String> data = new HashMap<>();
            data.put("type", NotificationType.moving.name());
            data.put("timeToMeet", profile.getCurrent().getTimeToMeet().toString());

            NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data);
        }


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
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        MessagingService.removeNotifications(activity);
    }

    private boolean defineLocationListener(Profile profile) {
        if (profile.equals(profile.getCurrent().getOwner())) {
            if (profile.getCurrent().getOwner().getTravelMode() != none) {
                if (profile.getCurrent().getRole() == supply) {
                    return true;
                } else {
                    return profile.getCurrent().getParty().getTravelMode() == none;
                }
            }
        } else {
            if (profile.getCurrent().getParty().getTravelMode() != none) {
                if (profile.getCurrent().getRole() == demand) {
                    return true;
                } else {
                    return profile.getCurrent().getOwner().getTravelMode() == none;
                }
            }
        }
        return false;
    }

    private void registerLocationListener(final Profile profile) {
        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                updatePosition(profile, location);
                AppCache.updateNoxbox();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;

        locationManager.requestLocationUpdates(GPS_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_RECEIVE_IN_SECONDS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener);
        //locationManager.requestLocationUpdates(NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_RECEIVE_IN_SECONDS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener);
    }


    private void updatePosition(Profile profile, Location location) {
        if (profile.equals(profile.getCurrent().getOwner())) {
            profile.getCurrent().getOwner().setPosition(new Position(location.getLatitude(), location.getLongitude()));
        } else {
            profile.getCurrent().getParty().setPosition(new Position(location.getLatitude(), location.getLongitude()));
        }
    }
}
