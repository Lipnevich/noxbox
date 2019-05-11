package live.noxbox.tools.location;

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
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.crashlytics.android.Crashlytics;

import java.util.concurrent.TimeUnit;

import live.noxbox.database.Firestore;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.tools.Task;

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
public class BackgroundLocationListenerWorker extends Worker {
    public BackgroundLocationListenerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final String TAG = "BackgroundLocationListenerWorker";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isMovingFinished;

    private Criteria criteria;
    private String noxboxId;
    private volatile Noxbox current;

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

        isMovingFinished = false;
        noxboxId = profile().getNoxboxId();
        current = profile().getCurrent();

        Firestore.listenNoxbox(noxboxId, new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                current = noxbox;
            }
        }, new Task<Exception>() {
            @Override
            public void execute(Exception exception) {
                Crashlytics.logException(exception);
            }
        });

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
                        || profile().getCurrent().getFinished()
                        || inForeground()) {
                    isMovingFinished = true;
                    locationManager.removeUpdates(locationListener);
                    stopThisWorker();
                    return;
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
            HandlerThread t = new HandlerThread("handlerthreadnetwork");
            t.start();
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener, t.getLooper());
        }
        HandlerThread t = new HandlerThread("singleupdatethread");
        t.start();
        while (true) {
            if (isStopped())
                return Result.retry();

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

    public static void startBackgroundLocationListenerWorker(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(BackgroundLocationListenerWorker.class).setInitialDelay(2, TimeUnit.SECONDS).setConstraints(constraints).addTag(TAG).build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
    }
}
