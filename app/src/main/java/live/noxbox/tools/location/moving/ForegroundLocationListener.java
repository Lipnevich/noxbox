package live.noxbox.tools.location.moving;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.google.android.gms.maps.GoogleMap;

import java.util.concurrent.TimeUnit;

import live.noxbox.database.GeoRealtime;
import live.noxbox.debug.DebugMessage;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static live.noxbox.Constants.DEFAULT_MARKER_SIZE;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.Constants.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Constants.MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.Constants.TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.services.MessagingService.inForeground;
import static live.noxbox.states.Moving.memberWhoMovingMarker;
import static live.noxbox.states.Moving.memberWhoMovingPosition;
import static live.noxbox.states.Moving.updateTimeView;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;
import static live.noxbox.tools.MarkerCreator.drawMovingMemberMarker;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;

/**
 * Created by Vladislaw Kravchenok on 03.05.2019.
 */
public class ForegroundLocationListener extends MovingWorker {
    public ForegroundLocationListener(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final String TAG = "ForegroundLocationListener";


    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        createHandlerThread(getApplicationContext().getPackageName() + "foregroundHandlerThread");
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onLocationChanged(final Location location) {
                if (NoxboxState.getState(profile().getCurrent(), profile()) != NoxboxState.moving
                        || profile().getCurrent().getFinished()
                        || !inForeground()) {
                    isMovingFinished = true;
                    locationManager.removeUpdates(locationListener);
                    cancelWorkerByTag(getApplicationContext(), TAG);
                    return;
                }

                memberWhoMovingPosition = Position.from(location);

                if (activity != null) {
                    activity.runOnUiThread(() -> {
                        googleMap.clear();
                        memberWhoMovingMarker = drawMovingMemberMarker(profile().getCurrent().getProfileWhoComes().getTravelMode(), memberWhoMovingPosition, googleMap, getApplicationContext().getResources());
                        memberWhoMovingMarker.setPosition(memberWhoMovingPosition.toLatLng());
                        drawPath(getApplicationContext(), googleMap, profile().getCurrent().getPosition(), memberWhoMovingPosition);
                        createCustomMarker(profile().getCurrent(), googleMap, activity.getResources(), DEFAULT_MARKER_SIZE);
                        DebugMessage.popup(getApplicationContext(), "Your location was updated with " + memberWhoMovingPosition.toString());

                        updateTimeView(profile(), getApplicationContext());
                    });

                }

                GeoRealtime.updatePosition(profile().getCurrent().getId(), Position.from(location));
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
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener, looper);
        } else {
            if (activity != null) {
                startLocationPermissionRequest(activity, LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS);
            }
        }
        while (true) {
            if (isStopped() || isMovingFinished) {
                return Result.success();
            }


            if (isLocationPermissionGranted(getApplicationContext())
                    && locationListener != null
                    && locationManager != null
                    && !isMovingFinished) {
                locationManager.requestSingleUpdate(criteria, locationListener, looper);
            }

            try {
                TimeUnit.MILLISECONDS.sleep(TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }

    public static void provideContextLinks(Activity activity, GoogleMap googleMap) {
        ForegroundLocationListener.activity = activity;
        ForegroundLocationListener.googleMap = googleMap;
    }

    public static void removeContextLinks() {
        activity = null;
        googleMap = null;
    }


}
