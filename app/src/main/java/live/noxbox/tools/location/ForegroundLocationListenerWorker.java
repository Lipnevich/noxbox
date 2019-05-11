package live.noxbox.tools.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.gms.maps.GoogleMap;

import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import live.noxbox.database.GeoRealtime;
import live.noxbox.debug.DebugMessage;
import live.noxbox.model.Position;
import live.noxbox.states.Moving;

import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static live.noxbox.Constants.DEFAULT_MARKER_SIZE;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.Constants.MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS;
import static live.noxbox.Constants.MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.Constants.TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.states.Moving.memberWhoMovingMarker;
import static live.noxbox.states.Moving.memberWhoMovingPosition;
import static live.noxbox.tools.MapOperator.drawPath;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;
import static live.noxbox.tools.MarkerCreator.drawMovingMemberMarker;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;

/**
 * Created by Vladislaw Kravchenok on 03.05.2019.
 */
public class ForegroundLocationListenerWorker extends Worker {
    public ForegroundLocationListenerWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final String TAG = "ForegroundWorker";

    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean isMovingFinished;

    private Criteria criteria;

    private static Activity activity;
    private static GoogleMap googleMap;

    @SuppressLint("MissingPermission")
    @NonNull
    @Override
    public Result doWork() {
        //Looper.prepare();
        criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);

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
                    isMovingFinished = true;
                    locationManager.removeUpdates(locationListener);
                    stopThisWorker();
                    return;
                }

                memberWhoMovingPosition = Position.from(location);

                if (activity != null) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            googleMap.clear();
                            memberWhoMovingMarker = drawMovingMemberMarker(profile().getCurrent().getProfileWhoComes().getTravelMode(), memberWhoMovingPosition, googleMap, getApplicationContext().getResources());
                            memberWhoMovingMarker.setPosition(memberWhoMovingPosition.toLatLng());
                            drawPath(getApplicationContext(), googleMap, profile().getCurrent().getPosition(), memberWhoMovingPosition);
                            createCustomMarker(profile().getCurrent(), googleMap, activity.getResources(), DEFAULT_MARKER_SIZE);
                            DebugMessage.popup(getApplicationContext(), "Your location was updated with " + memberWhoMovingPosition.toString());


                            Moving.updateTimeView(profile(), getApplicationContext());
                        }
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
            HandlerThread t = new HandlerThread("handlerthreadnetwork");
            t.start();
            locationManager.requestLocationUpdates(NETWORK_PROVIDER, MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS, MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS, locationListener, t.getLooper());
        } else {
            if (activity != null) {
                startLocationPermissionRequest(activity, LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS);
            }
        }
        HandlerThread t = new HandlerThread("singleupdatethread");
        t.start();
        while (true) {
            if (isStopped())
                return Result.failure();

            if (isLocationPermissionGranted(getApplicationContext())
                    && locationListener != null
                    && locationManager != null
                    && !isMovingFinished) {
                locationManager.requestSingleUpdate(criteria, locationListener, t.getLooper());
            }

            try {
                TimeUnit.MILLISECONDS.sleep(TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS);
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

    public static void provideMapComponentsForUpdating(Activity activity, GoogleMap googleMap) {
        ForegroundLocationListenerWorker.activity = activity;
        ForegroundLocationListenerWorker.googleMap = googleMap;
    }

    public static void stopForegroundLocationListenerWorker(Context context) {
        WorkManager.getInstance(context).cancelAllWorkByTag(TAG);
    }

    public static void startForegroundLocationListenerWorker(Context context) {
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ForegroundLocationListenerWorker.class).setInitialDelay(2, TimeUnit.SECONDS).setConstraints(constraints).addTag(TAG).build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
    }
}
