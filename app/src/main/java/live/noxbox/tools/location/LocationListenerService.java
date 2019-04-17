package live.noxbox.tools.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.Timer;
import java.util.TimerTask;

import live.noxbox.database.GeoRealtime;
import live.noxbox.debug.DebugMessage;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.states.Moving;
import live.noxbox.tools.Task;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.Constants.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Constants.MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.Constants.TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.Events.inForeground;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MarkerCreator.drawMovingMemberMarker;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;

/**
 * Created by Vladislaw Kravchenok on 28.03.2019.
 */
//SERVICE CLASS
public class LocationListenerService extends Service {

    private static LocationManager locationManager;
    private static LocationListener locationListener;
    private Activity activity;
    private static GoogleMap googleMap;
    private static Position memberWhoMovingPosition;
    private static Marker memberWhoMovingMarker;
    private static Task<Profile> updateTimeView;

    private Timer timer;
    private TimerTask timerTask;
    private int counter = 0;
    private Criteria criteria = new Criteria();

    public LocationListenerService() {
        super();
    }

    public LocationListenerService(Activity activity, GoogleMap googleMap, Position memberWhoMovingPosition, Marker memberWhoMovingMarker, Task<Profile> updateTimeView) {
        super();
        this.activity = activity;
        LocationListenerService.googleMap = googleMap;
        LocationListenerService.memberWhoMovingPosition = memberWhoMovingPosition;
        LocationListenerService.memberWhoMovingMarker = memberWhoMovingMarker;
        LocationListenerService.updateTimeView = updateTimeView;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
    }

    @SuppressLint("MissingPermission")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(final Location location) {

                if ((!isNullOrZero(profile().getCurrent().getTimeOwnerVerified()) && !isNullOrZero(profile().getCurrent().getTimePartyVerified()))
                        || !isNullOrZero(profile().getCurrent().getTimeCanceledByOwner())
                        || !isNullOrZero(profile().getCurrent().getTimeCanceledByParty())
                        || !isNullOrZero(profile().getCurrent().getTimeOwnerRejected())
                        || !isNullOrZero(profile().getCurrent().getTimePartyRejected())
                        || profile().getCurrent().getFinished()) {
                    locationManager.removeUpdates(locationListener);
                    stopTimer();
                    stopSelf();
                    return;
                }
                if (inForeground()) {
                    memberWhoMovingPosition = Position.from(location);
                    if (memberWhoMovingMarker == null && googleMap != null) {
                        memberWhoMovingMarker = drawMovingMemberMarker(profile().getCurrent().getProfileWhoComes().getTravelMode(), memberWhoMovingPosition, googleMap, getApplicationContext().getResources());
                    }

                    if (memberWhoMovingPosition != null && googleMap != null) {
                        memberWhoMovingMarker.setPosition(memberWhoMovingPosition.toLatLng());
                        drawPath(getApplicationContext(), googleMap, profile().getCurrent().getPosition(), memberWhoMovingPosition);
                        DebugMessage.popup(getApplicationContext(), "Your location was updated with " + memberWhoMovingPosition.toString());
                    }
                    if (updateTimeView != null) {
                        updateTimeView.execute(profile());
                    } else {
                        Moving.updateTimeView(profile(), getApplicationContext());
                    }
                } else {
                    memberWhoMovingPosition = Position.from(location);
                }

                GeoRealtime.updatePosition(profile().getCurrent().getId(), memberWhoMovingPosition);
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

        if (isLocationPermissionGranted(getApplicationContext())) {
            PackageManager pm = getApplicationContext().getPackageManager();
            boolean hasGps = pm.hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);
            if (hasGps) {
                DebugMessage.popup(getApplicationContext(), "HAS GPS");
                locationManager.requestLocationUpdates(GPS_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener, null);
            } else {
                DebugMessage.popup(getApplicationContext(), "GPS MISSING");
            }
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener, null);
        } else {
            if (activity != null) {
                startLocationPermissionRequest(activity, LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS);

            }
        }
        if (isLocationPermissionGranted(getApplicationContext())) {
            startTimer();
        }
        return START_STICKY;
    }

    public void startTimer() {
        timer = new Timer();

        initializeTimerTask();

        timer.schedule(timerTask, TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS, TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            @SuppressLint("MissingPermission")
            public void run() {
                if (isLocationPermissionGranted(getApplicationContext())
                        && locationListener != null
                        && locationManager != null) {
                    locationManager.requestSingleUpdate(criteria, locationListener, null);
                }
                Log.i("Timer:", "in timer +" + (counter++));
            }
        };
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopTimer();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
