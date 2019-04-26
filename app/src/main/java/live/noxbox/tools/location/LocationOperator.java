package live.noxbox.tools.location;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnSuccessListener;

import live.noxbox.debug.DebugMessage;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.profile;

public class LocationOperator {

    private static FusedLocationProviderClient fusedLocationProviderClient;
    private static Location lastLocation;

    public static CameraUpdate actualMapTarget;

    public static Location location() {
        if (lastLocation == null) {
            lastLocation = profile().getPosition().toLocation();
        }
        return lastLocation;
    }

    public static boolean isLocationPermissionGranted(Context context) {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void startLocationPermissionRequest(Activity activity, int requestCode) {
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    requestCode);
        }
    }

    public static void updateLocation(Context context, GoogleMap googleMap) {
        if (googleMap == null)
            return;

        try {
            if (isLocationPermissionGranted(context)) {
                googleMap.setMyLocationEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
            }
        } catch (SecurityException e) {
            Crashlytics.logException(e);
        }
    }

    public static void getDeviceLocation(Profile profile, GoogleMap googleMap, Activity activity) {
        getDeviceLocation(position -> {
            profile.setPosition(position);
            MapOperator.buildMapPosition(googleMap, activity.getApplicationContext());
        }, activity);
    }

    public static void getDeviceLocation(Task<Position> onSuccess, Activity activity) {
        if (fusedLocationProviderClient == null) return;
        try {
            if (isLocationPermissionGranted(activity.getApplicationContext())) {
                fusedLocationProviderClient.getLastLocation().addOnSuccessListener(activity, (OnSuccessListener<? super Location>) location -> {
                    if (location != null) {
                        lastLocation = location;
                        onSuccess.execute(Position.from(lastLocation));
                    } else if (lastLocation != null) {
                        onSuccess.execute(Position.from(lastLocation));
                    } else {
                        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
                        Criteria criteria = new Criteria();
                        criteria.setAccuracy(Criteria.ACCURACY_FINE);
                        locationManager.requestSingleUpdate(criteria, new LocationListener() {
                            @Override
                            public void onLocationChanged(Location location) {
                                if (location != null) {
                                    lastLocation = location;

                                    onSuccess.execute(Position.from(lastLocation));
                                } else {
                                    DebugMessage.popup(activity, "Unknown user location");
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
                        }, null);
                    }
                });
            }

        } catch (SecurityException e) {
            Crashlytics.logException(e);
        }
    }

    public static FusedLocationProviderClient initLocationProviderClient(Context context) {
        if (fusedLocationProviderClient == null) {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        }
        return fusedLocationProviderClient;
    }
}
