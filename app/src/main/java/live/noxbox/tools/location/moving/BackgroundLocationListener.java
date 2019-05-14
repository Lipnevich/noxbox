package live.noxbox.tools.location.moving;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import live.noxbox.database.GeoRealtime;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static live.noxbox.Constants.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Constants.MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.Events.inForeground;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;

/**
 * Created by Vladislaw Kravchenok on 03.05.2019.
 */
public class BackgroundLocationListener extends MovingWorker {
    public BackgroundLocationListener(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final String TAG = "BackgroundLocationListener";

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        isMovingFinished = false;
        createHandlerThread(getApplicationContext().getPackageName() + "backgroundHandlerThread");
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                if (NoxboxState.getState(profile().getCurrent(), null) != NoxboxState.moving
                        || profile().getCurrent().getFinished()
                        || inForeground()) {
                    isMovingFinished = true;
                    locationManager.removeUpdates(locationListener);
                    cancelWorkerByTag(getApplicationContext(), TAG);
                    return;
                }

                GeoRealtime.updatePosition(noxboxId, Position.from(location));
                if (movingNotification == null) {
                    showMovingNotification(Position.from(location));
                } else {
                    updateMovingNotification(Position.from(location));
                }
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
                TimeUnit.MILLISECONDS.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
    }
}
