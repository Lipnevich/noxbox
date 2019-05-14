package live.noxbox.tools.location.moving;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

import live.noxbox.database.GeoRealtime;
import live.noxbox.model.Position;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static live.noxbox.Constants.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Constants.MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.Noxbox.isNullOrZero;
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

        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(final Location location) {
                if ((!isNullOrZero(profile().getCurrent().getTimeOwnerVerified()) && !isNullOrZero(profile().getCurrent().getTimePartyVerified()))
                        || !isNullOrZero(profile().getCurrent().getTimeCanceledByOwner())
                        || !isNullOrZero(profile().getCurrent().getTimeCanceledByParty())
                        || !isNullOrZero(profile().getCurrent().getTimeOwnerRejected())
                        || !isNullOrZero(profile().getCurrent().getTimePartyRejected())
                        || profile().getCurrent().getFinished()
                        || inForeground()) {
                    isMovingFinished = true;
                    locationManager.removeUpdates(locationListener);
                    stopThisWorker();
                    return;
                }

                GeoRealtime.updatePosition(noxboxId, Position.from(location));
                showMovingNotification(Position.from(location));
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
            HandlerThread t = new HandlerThread("handlerthreadnetwork");
            t.start();
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener, t.getLooper());
        }
        HandlerThread t = new HandlerThread("singleupdatethread");
        t.start();
        while (true) {
            if (isStopped()) {
                if (inForeground()) {
                    return Result.failure();
                }
                return Result.retry();
            }

            if (isLocationPermissionGranted(getApplicationContext())
                    && locationListener != null
                    && locationManager != null
                    && !isMovingFinished) {
                locationManager.requestSingleUpdate(criteria, locationListener, t.getLooper());
            }

            try {
                TimeUnit.MILLISECONDS.sleep(11000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (isMovingFinished)
                return Result.success();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();
        Log.i(TAG, "onStopped()");

    }

    private void stopThisWorker() {
        WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(TAG);
    }
}
