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

import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.WeakHashMap;

import io.fabric.sdk.android.Fabric;
import live.noxbox.activities.AuthActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.Firestore;
import live.noxbox.debug.DebugActivity;
import live.noxbox.debug.TimeLogger;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.services.LocationReceiver;
import live.noxbox.states.Accepting;
import live.noxbox.states.AvailableNoxboxes;
import live.noxbox.states.Created;
import live.noxbox.states.Moving;
import live.noxbox.states.Performing;
import live.noxbox.states.Requesting;
import live.noxbox.states.State;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.ExchangeRate;
import live.noxbox.tools.MapOperator;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.ConfirmationMessage.messageGps;
import static live.noxbox.tools.LocationPermitOperator.isLocationPermissionGranted;
import static live.noxbox.tools.LocationPermitOperator.locationPermissionGranted;
import static live.noxbox.tools.LogEvents.generateLogEvent;
import static live.noxbox.tools.MapOperator.moveCopyrightLeft;
import static live.noxbox.tools.MapOperator.setupMap;

public class MapActivity extends DebugActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {
    protected static final String TAG = "MapActivity";

    protected GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initCrashReporting();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Router.startActivity(this, AuthActivity.class);
            finish();
            return;
        }
        Crashlytics.setUserIdentifier(user.getUid());
        FirebaseMessaging.getInstance().subscribeToTopic(user.getUid());

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapId);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();

        AppCache.startListening();
        AppCache.readProfile(profile -> checkBalance(profile, MapActivity.this));

        ExchangeRate.wavesToUSD(rate -> AppCache.wavesToUsd = rate);



    }

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
        unregisterReceiver(locationReceiver);
        googleApiClient.disconnect();

        AppCache.stopListen(this.getClass().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentState != null) currentState.clear();
    }


    @Override
    public void onMapReady(GoogleMap readyMap) {
        super.onMapReady(readyMap);
        googleMap = readyMap;

        AppCache.readProfile(profile -> {
            updateLocationUI();
            getDeviceLocation(profile);
        });
        //visibleCurrentLocation();
        setupMap(this, googleMap);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        moveCopyrightLeft(googleMap);
        draw();

    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private LocationReceiver locationReceiver;

    protected boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        locationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true;
                    AppCache.readProfile(this::getDeviceLocation);

                    generateLogEvent(this, "location_permission_granted");
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (googleMap == null)
            return;

        try {
            if (locationPermissionGranted) {
                googleMap.setMyLocationEnabled(true);
            } else {
                googleMap.setMyLocationEnabled(false);
                lastKnownLocation = null;
            }
        } catch (SecurityException e) {
            Crashlytics.logException(e);
        }
    }



    public void getDeviceLocation(Profile profile) {
        try {
            if (locationPermissionGranted && isLocationPermissionGranted(getApplicationContext())) {
                com.google.android.gms.tasks.Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, (OnCompleteListener) task -> {
                    if (task.isSuccessful()) {
                        lastKnownLocation = (Location) task.getResult();

                        if (lastKnownLocation != null) {
                            Position position = Position.from(lastKnownLocation);
                            profile.setPosition(Position.from(new LatLng(position.getLatitude(),
                                    position.getLongitude())));
                            MapOperator.buildMapPosition(googleMap, getApplicationContext());
                            Firestore.writeProfile(profile, objectProfile -> { });
                        }
                    } else {
                        Crashlytics.logException(task.getException());
                    }
                });
            }
        } catch (SecurityException e) {
            Crashlytics.logException(e);
        }
    }


    private State currentState;

    private void draw() {
        AppCache.listenProfile(this.getClass().getName(), profile -> {
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
            states.clear();
        } else {
            AppCache.startListenNoxbox(profile.getCurrent().getId());
        }
        //strong link for weakMap
        State newState = states.get(state);
        if (newState != null)
            return newState;


        switch (state) {
            case initial:
                newState = new AvailableNoxboxes(googleMap, googleApiClient, this);
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

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    private void initCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }
}