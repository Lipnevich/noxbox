package live.noxbox.tools.location;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.IntentSender;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import live.noxbox.debug.DebugMessage;

import static live.noxbox.MapActivity.TAG;
import static live.noxbox.tools.location.LocationOperator.initLocationProviderClient;
import static live.noxbox.tools.location.LocationOperator.location;


/**
 * Created by Vladislaw Kravchenok on 18.03.2019.
 */
public class LocationUpdater {

    public static final String KEY_REQUESTING_LOCATION_UPDATES = "request_location_updates_key-0x2";
    public static final int REQUEST_CHECK_LOCATION_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 20000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    private boolean requestingLocationUpdates;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private SettingsClient settingsClient;
    private LocationRequest locationRequest;
    private LocationSettingsRequest locationSettingsRequest;
    private LocationCallback locationCallback;
    private Activity activity;

    public LocationUpdater(Activity activity) {
        this.activity = activity;
        fusedLocationProviderClient = initLocationProviderClient(activity.getApplicationContext());
        settingsClient = LocationServices.getSettingsClient(activity);
        createLocationCallback();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    public void createLocationCallback() {
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                location().set(locationResult.getLastLocation());
                DebugMessage.popup(activity, location().toString(), Toast.LENGTH_LONG);
            }
        };
    }

    public void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
    }

    public void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        locationSettingsRequest = builder.build();
    }

    public void startLocationUpdates() {
        if (!requestingLocationUpdates) requestingLocationUpdates = true;

        settingsClient.checkLocationSettings(locationSettingsRequest)
                .addOnSuccessListener(activity, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        requestingLocationUpdates = true;
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest,
                                locationCallback, Looper.myLooper());

                    }
                }).addOnFailureListener(activity, e -> {
            int statusCode = ((ApiException) e).getStatusCode();
            switch (statusCode) {
                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                    try {
                        ResolvableApiException rae = (ResolvableApiException) e;
                        rae.startResolutionForResult(activity, REQUEST_CHECK_LOCATION_SETTINGS);
                    } catch (IntentSender.SendIntentException sie) {
                        Log.i(TAG, "PendingIntent unable to execute request.");
                    }
                    break;
                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                    String errorMessage = "Location settings are inadequate, and cannot be " +
                            "fixed here. Fix in Settings.";
                    Crashlytics.logException(new LocationException(errorMessage));
                    DebugMessage.popup(activity,errorMessage,Toast.LENGTH_LONG);
                    requestingLocationUpdates = false;
            }

        });
    }

    public void stopLocationUpdates() {
        if (!requestingLocationUpdates) {
            Log.d(TAG, "stopLocationUpdates: updates never requested, no-op.");
            return;
        }
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
                .addOnCompleteListener(activity, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        requestingLocationUpdates = false;
                    }
                });
    }


    public boolean isRequestingLocationUpdates() {
        return requestingLocationUpdates;
    }

    public LocationUpdater setRequestingLocationUpdates(boolean requestingLocationUpdates) {
        this.requestingLocationUpdates = requestingLocationUpdates;
        return this;
    }
}
