/*
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package live.noxbox;

import android.app.Activity;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;
import java.util.WeakHashMap;

import io.fabric.sdk.android.Fabric;
import live.noxbox.database.AppCache;
import live.noxbox.debug.DebugActivity;
import live.noxbox.debug.TimeLogger;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.pages.Accepting;
import live.noxbox.pages.AuthActivity;
import live.noxbox.pages.AvailableServices;
import live.noxbox.pages.Created;
import live.noxbox.pages.LocationReceiver;
import live.noxbox.pages.Moving;
import live.noxbox.pages.Performing;
import live.noxbox.pages.Requesting;
import live.noxbox.state.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.tools.ConfirmationMessage.messageGps;
import static live.noxbox.tools.MapController.moveCopyrightLeft;
import static live.noxbox.tools.MapController.setupMap;

public class MapActivity extends DebugActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    protected static final String TAG = "MapActivity";

    protected GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest locationRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCrashReporting();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Router.startActivity(this, AuthActivity.class);
            finish();
            return;
        }
        Crashlytics.setUserIdentifier(user.getUid());
        FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapId);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();
        AppCache.startListening();
    }

    private Marker currentLocationMarker;
    private LocationCallback locationCallback;

    @Override
    public void onMapReady(GoogleMap readyMap) {
        super.onMapReady(readyMap);
        googleMap = readyMap;

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5 * 1000);

        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                locationCallback = new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        List<Location> locationList = locationResult.getLocations();
                        if (locationList.size() > 0) {
                            Location location = locationList.get(locationList.size() - 1);
                            if (currentLocationMarker != null) {
                                currentLocationMarker.remove();
                            }

                            final LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                            MarkerOptions markerOptions = new MarkerOptions();
                            markerOptions.position(latLng);
                            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
                            currentLocationMarker = googleMap.addMarker(markerOptions);

                            profile.setPosition(new Position(location.getLatitude(), location.getLongitude()));
                            //AppCache.fireProfile();
                        }
                    }
                };
            }
        });
        visibleCurrentLocation();
        setupMap(this, googleMap);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        moveCopyrightLeft(googleMap);
        draw();

    }

    private LocationReceiver locationReceiver;

    @Override
    protected void onResume() {
        if (isLocationPermissionGranted(this)) {
            if (!isGpsEnabled()) {
                messageGps(this);
            }
        }
        locationReceiver = new LocationReceiver(this);
        registerReceiver(locationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        googleApiClient.connect();
        draw();

        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mFusedLocationClient != null && locationCallback != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
        unregisterReceiver(locationReceiver);
        googleApiClient.disconnect();
        AppCache.stopListen(this.getClass().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentState != null) currentState.clear();

    }


    private void initCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }


    public static boolean isLocationPermissionGranted(final Activity activity) {
        return ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    protected void visibleCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            startListenLocationUpdate();
        } else {
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            });
        }
    }

    private void startListenLocationUpdate() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            if (mFusedLocationClient != null && locationRequest != null && locationCallback != null) {
                mFusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
            }
        }
    }

    protected boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    visibleCurrentLocation();
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startListenLocationUpdate();
    }


    protected int getEstimationInMinutes(Position from, Position to, TravelMode travelMode) {
        float distanceInMeters = from.toLocation()
                .distanceTo(to.toLocation());
        int estimationInMinutes = (int) (distanceInMeters / travelMode.getSpeedInMetersPerMinute());
        return estimationInMinutes > 0 ? estimationInMinutes : 1;
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    public static Position getCameraPosition(GoogleMap googleMap) {
        if (googleMap == null) return null;
        LatLng latLng = googleMap.getCameraPosition().target;

        return Position.from(latLng);
    }

    private State currentState;

    private void draw() {
        AppCache.listenProfile(this.getClass().getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (googleMap == null) return;
                State newState = getFragment(profile);
                if (currentState == null) {
                    currentState = newState;
                    measuredDraw(newState, profile);
                    newState.draw(profile);
                    return;
                }

                if (!newState.getClass().getName().equals(currentState.getClass().getName())) {
                    currentState.clear();
                    currentState = newState;
                    measuredDraw(newState, profile);
                    return;
                }

                measuredDraw(currentState, profile);
            }
        });
    }

    private void measuredDraw(State state, Profile profile) {
        TimeLogger timeLogger = new TimeLogger();
        state.draw(profile);
        timeLogger.makeLog(state.getClass().getSimpleName());
    }

    private WeakHashMap<NoxboxState, State> states = new WeakHashMap<>();

    public State getFragment(final Profile profile) {
        NoxboxState state = NoxboxState.getState(profile.getCurrent(), profile);
        if (state == NoxboxState.initial) {
            AppCache.stopListenNoxbox(profile.getCurrent().getId());
        } else {
            AppCache.startListenNoxbox(profile.getCurrent().getId());
        }
        //strong link for weakMap
        State newState = states.get(state);
        if (newState != null)
            return newState;


        switch (state) {
            case initial:
                newState = new AvailableServices(googleMap, googleApiClient, this);
                break;
            case created:
                newState = new Created(googleMap, this);
                break;
            case requesting:
                newState = new Requesting(googleMap, this);
                break;
            case accepting:
                newState = new Accepting(googleMap, this);
                break;
            case moving:
                newState = new Moving(googleMap, this);
                break;
            case performing:
                newState = new Performing(this, googleMap);
                break;
            default:
                throw new IllegalStateException("Unknown state: " + state.name());
        }
        states.put(state, newState);

        if (BuildConfig.DEBUG && currentState != null)
            Log.d(State.TAG + TAG, "previousState: " + currentState.getClass().getName());
        Log.d(State.TAG + TAG, "newState: " + newState.getClass().getName());
        if (profile.getCurrent() != null) {
            Log.d(State.TAG + TAG, "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
            Log.d(State.TAG + TAG, "timeRequested: " + DateTimeFormatter.time(profile.getCurrent().getTimeRequested()));
            Log.d(State.TAG + TAG, "timeAccepted: " + DateTimeFormatter.time(profile.getCurrent().getTimeAccepted()));
            Log.d(State.TAG + TAG, "timeStartPerforming: " + DateTimeFormatter.time(profile.getCurrent().getTimeStartPerforming()));
            Log.d(State.TAG + TAG, "timeCompleted: " + DateTimeFormatter.time(profile.getCurrent().getTimeCompleted()));
        } else {
            Log.d(State.TAG + TAG, "current noxbox: " + "null");
        }


        return newState;
    }

    @Override
    public void onBackPressed() {
        // ignore it
    }
}