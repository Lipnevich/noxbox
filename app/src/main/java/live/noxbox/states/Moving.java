package live.noxbox.states;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.activities.ChatActivity;
import live.noxbox.activities.ConfirmationActivity;
import live.noxbox.database.GeoRealtime;
import live.noxbox.debug.DebugMessage;
import live.noxbox.model.Message;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.NavigatorManager;
import live.noxbox.tools.Router;

import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND;
import static android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE;
import static android.location.LocationManager.GPS_PROVIDER;
import static live.noxbox.Constants.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Constants.MINIMUM_TIME_INTERVAL_BETWEEN_GPS_ACCESS_IN_SECONDS;
import static live.noxbox.database.AppCache.readProfile;
import static live.noxbox.database.GeoRealtime.stopListenPosition;
import static live.noxbox.model.MarketRole.demand;
import static live.noxbox.model.MarketRole.supply;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;
import static live.noxbox.tools.LocationPermitOperator.isLocationPermissionGranted;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MapOperator.moveCopyrightLeft;
import static live.noxbox.tools.MapOperator.moveCopyrightRight;
import static live.noxbox.tools.Router.startActivity;

public class Moving implements State {

    private GoogleMap googleMap;
    private Activity activity;

    private static LocationManager locationManager;
    private static LocationListener locationListener;

    private static LinearLayout movingView;
    private static View childMovingView;
    private static TextView timeView;
    private static Position memberWhoMovingPosition;

    private Marker memberWhoMoving;

    private TextView totalUnreadView;

    public Moving(final GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());

        readProfile(profile -> memberWhoMovingPosition = profile.getCurrent().getProfileWhoComes().getPosition());
    }

    private void drawUnreadMessagesIndicator(Map<String, Message> messages, Long readTime) {
        Integer totalUnread = 0;

        totalUnreadView = activity.findViewById(R.id.totalUnread);

        Iterator iterator = messages.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry entry = (Map.Entry) iterator.next();
            Message message = (Message) entry.getValue();

            if (message.getTime() > readTime) {
                totalUnread++;
                totalUnreadView.setVisibility(View.VISIBLE);
                if (totalUnread <= 9) {
                    totalUnreadView.setText(totalUnread.toString());
                } else {
                    totalUnreadView.setText("9+");
                }
            }
        }

        if (totalUnread == 0) {
            totalUnreadView.setVisibility(View.GONE);
        }
    }

    @Override
    public void draw(final Profile profile) {
        // TODO (vl) glide upload in daemon other person photo and store in cache

        Log.d(TAG + "Moving", "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
        Log.d(TAG + "Moving", "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));

        movingView = activity.findViewById(R.id.container);
        movingView.removeAllViews();
        childMovingView = activity.getLayoutInflater().inflate(R.layout.state_moving, null);
        movingView.addView(childMovingView);
        timeView = childMovingView.findViewById(R.id.timeView);
        updateTimeView(profile, activity);

        if (profile.getCurrent().getOwner().equals(profile)) {
            drawUnreadMessagesIndicator(profile.getCurrent().getChat().getPartyMessages(), profile.getCurrent().getChat().getOwnerReadTime());
        } else {
            drawUnreadMessagesIndicator(profile.getCurrent().getChat().getOwnerMessages(), profile.getCurrent().getChat().getPartyReadTime());
        }

        if (defineProfileLocationListener(profile)) {
            Intent intent = new Intent(activity, LocationListenerService.class);
            activity.startService(intent);
        } else {
            GeoRealtime.listenPosition(profile.getCurrent().getId(), position -> {
                memberWhoMovingPosition = position;
                draw(profile);
            });
            HashMap<String, String> data = new HashMap<>();
            data.put("type", NotificationType.moving.name());
            NotificationFactory.buildNotification(activity.getApplicationContext(), profile, data);
        }

        drawPath(activity, googleMap, profile);

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        if (memberWhoMoving == null) {
            memberWhoMoving = MarkerCreator.createMovingMemberMarker(profile.getCurrent().getProfileWhoComes().getTravelMode(),
                    memberWhoMovingPosition, googleMap, activity.getResources());
        } else {
            memberWhoMoving.setPosition(memberWhoMovingPosition.toLatLng());
        }

        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.chat).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.navigation).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);

        if (profile.getCurrent().getOwner().equals(profile)) {
            if (!isNullOrZero(profile.getCurrent().getTimeOwnerVerified())) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        } else {
            if (!isNullOrZero(profile.getCurrent().getTimePartyVerified())) {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
            } else {
                activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
            }
        }

        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.eye);

        activity.findViewById(R.id.navigation).setOnClickListener(v -> NavigatorManager.openNavigator(activity, profile));

        activity.findViewById(R.id.locationButton).setOnClickListener(v -> {
            DebugMessage.popup(activity, "way and points");
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
        });


        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> Router.startActivity(activity, ConfirmationActivity.class));

        activity.findViewById(R.id.chat).setOnClickListener(v -> startActivity(activity, ChatActivity.class));

        MapOperator.buildMapMarkerListener(googleMap, profile, activity);
        moveCopyrightRight(googleMap);
    }

    @Override
    public void clear() {
        readProfile(profile -> stopListenPosition(profile.getCurrent().getId()));
        movingView.removeAllViews();
        MapOperator.clearMapMarkerListener(googleMap);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.chat).setVisibility(View.GONE);
        activity.findViewById(R.id.navigation).setVisibility(View.GONE);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        if (totalUnreadView != null) {
            totalUnreadView.setVisibility(View.GONE);
        }
        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.add);
        moveCopyrightLeft(googleMap);

        googleMap.clear();
        memberWhoMoving = null;
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
            locationManager = null;
        }
        MessagingService.removeNotifications(activity);
    }

    private boolean defineProfileLocationListener(Profile profile) {
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

//    private void registerLocationListener() {
//        if (locationManager != null && locationListener != null) return;
//
//        locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
//        locationListener = new LocationListener() {
//            @Override
//            public void onLocationChanged(final Location location) {
//                AppCache.readProfile(profile -> {
//                    Log.d(State.TAG + " Moving", location.toString());
//                    DebugMessage.popup(activity, location.getLatitude() + " : " + location.getLongitude());
//                    updatePosition(profile, location);
//                    updateTimeView(profile);
//                    AppCache.updateNoxbox();
//                });
//
//            }
//
//            @Override
//            public void onStatusChanged(String provider, int status, Bundle extras) {
//            }
//
//            @Override
//            public void onProviderEnabled(String provider) {
//            }
//
//            @Override
//            public void onProviderDisabled(String provider) {
//            }
//        };
//
//        if (!isLocationPermissionGranted(activity))
//            return;
//        locationManager.requestLocationUpdates(GPS_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_GPS_ACCESS_IN_SECONDS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener);
//    }

    private static void updateTimeView(Profile profile, Context context) {
        if (movingView != null && childMovingView != null && timeView != null) {
            int progressInMinutes = ((int) getTimeInMinutesBetweenUsers(profile.getCurrent().getPosition(), memberWhoMovingPosition, profile.getCurrent().getProfileWhoComes().getTravelMode()));
            timeView.setText(context.getResources().getString(R.string.movement, "" + progressInMinutes));
        }
    }

    //SERVICE CLASS
    public static class LocationListenerService extends Service {

        private LocationManager locationManager;
        private LocationListener locationListener;

        public LocationListenerService() {
            super();
        }

        @Override
        public void onCreate() {
            super.onCreate();
        }

        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {


            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationListener = new LocationListener() {
                @SuppressLint("MissingPermission")
                @Override
                public void onLocationChanged(final Location location) {
                    readProfile(profile -> {
                        if ((!isNullOrZero(profile.getCurrent().getTimeOwnerVerified()) && !isNullOrZero(profile.getCurrent().getTimePartyVerified()))
                                || !isNullOrZero(profile.getCurrent().getTimeCanceledByOwner())
                                || !isNullOrZero(profile.getCurrent().getTimeCanceledByParty())) {
                            locationManager.removeUpdates(locationListener);
                            stopSelf();
                            return;
                        }
                        if (inForeground()) {
                            DebugMessage.popup(getApplicationContext(), location.getLatitude() + " : " + location.getLongitude());
                            memberWhoMovingPosition = Position.from(location);
                            updateTimeView(profile, getApplicationContext());
                        } else {
                            memberWhoMovingPosition = Position.from(location);
                        }
                        Log.d(State.TAG + " Moving", location.toString());

                        GeoRealtime.updatePosition(profile.getCurrent().getId(), memberWhoMovingPosition);
                    });

                    if (!isLocationPermissionGranted(getApplicationContext()))
                        return;


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
            locationManager.requestLocationUpdates(GPS_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_GPS_ACCESS_IN_SECONDS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener);

            return super.onStartCommand(intent, flags, startId);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        private boolean inForeground() {
            ActivityManager.RunningAppProcessInfo appProcessInfo = new ActivityManager.RunningAppProcessInfo();
            ActivityManager.getMyMemoryState(appProcessInfo);
            return (appProcessInfo.importance == IMPORTANCE_FOREGROUND
                    || appProcessInfo.importance == IMPORTANCE_VISIBLE);
        }
    }
}
