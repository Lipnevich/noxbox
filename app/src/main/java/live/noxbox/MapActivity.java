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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;
import live.noxbox.activities.AuthActivity;
import live.noxbox.database.AppCache;
import live.noxbox.debug.HackerActivity;
import live.noxbox.debug.TimeLogger;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.services.LocationReceiver;
import live.noxbox.states.Accepting;
import live.noxbox.states.AvailableNoxboxes;
import live.noxbox.states.Created;
import live.noxbox.states.Moving;
import live.noxbox.states.Performing;
import live.noxbox.states.Requesting;
import live.noxbox.states.State;
import live.noxbox.tools.ExchangeRate;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.ConfirmationMessage.messageGps;
import static live.noxbox.tools.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.LocationOperator.initLocationProviderClient;
import static live.noxbox.tools.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.LocationOperator.updateLocation;
import static live.noxbox.tools.MapOperator.enterTheMap;
import static live.noxbox.tools.MapOperator.moveCopyrightLeft;
import static live.noxbox.tools.MapOperator.setupMap;

public class MapActivity extends HackerActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    private FusedLocationProviderClient providerClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MapTheme);
        super.onCreate(savedInstanceState);
        initCrashReporting();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || isFirstRun(false)) {
            Router.startActivity(this, AuthActivity.class);
            finish();
            return;
        }
        AppCache.profile().init(user);

        Crashlytics.setUserIdentifier(user.getUid());
        FirebaseMessaging.getInstance().subscribeToTopic(user.getUid()).addOnFailureListener(e -> Crashlytics.log(Log.ERROR, "failToSubscribeOnProfile", user.getUid()));

        providerClient = initLocationProviderClient(getApplicationContext());

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapId);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();

        AppCache.startListening();
        checkBalance(profile(), MapActivity.this);

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
        googleMap = readyMap;

        updateLocation(this, googleMap);

        setupMap(this, googleMap);
        enterTheMap(googleMap, this);

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
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation(profile(), googleMap, this);
                }
                break;
            }
            case LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation(profile(), googleMap, this);
                    executeUITasks();
                }
                break;
            }

        }
        updateLocation(this, googleMap);
    }


    private State currentState;

    private void draw() {
        AppCache.listenProfile(this.getClass().getName(), profile -> {
            if (googleMap == null) return;

            State newState = getFragment(profile);
            if (currentState == null) {
                currentState = newState;
                measuredDraw(newState);
                return;
            }

            if (!newState.getClass().getName().equals(currentState.getClass().getName())) {
                currentState.clear();
                currentState = newState;
                measuredDraw(newState);
                return;
            }

            measuredDraw(currentState);
        });
    }

    private void measuredDraw(State state) {
        TimeLogger timeLogger = new TimeLogger();
        state.draw(googleMap, this);
        timeLogger.makeLog(state.getClass().getSimpleName());
    }


    public State getFragment(final Profile profile) {
        NoxboxState state = NoxboxState.getState(profile.getCurrent(), profile);
        //strong link for weakMap
        State newState;

        switch (state) {
            case initial:
                newState = new AvailableNoxboxes();
                break;
            case created:
                newState = new Created();
                break;
            case requesting:
                newState = new Requesting();
                break;
            case accepting:
                newState = new Accepting();
                break;
            case moving:
                newState = new Moving();
                break;
            case performing:
                newState = new Performing();
                break;
            default:
                throw new IllegalStateException("Unknown state: " + state.name());
        }

        return newState;
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