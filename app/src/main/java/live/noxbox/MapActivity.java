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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import java.util.Collections;

import io.fabric.sdk.android.Fabric;
import live.noxbox.activities.AuthActivity;
import live.noxbox.activities.DemonstrationActivity;
import live.noxbox.database.AppCache;
import live.noxbox.debug.TimeLogger;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.services.LocationReceiver;
import live.noxbox.services.NetworkReceiver;
import live.noxbox.states.Accepting;
import live.noxbox.states.AvailableNoxboxes;
import live.noxbox.states.Created;
import live.noxbox.states.Moving;
import live.noxbox.states.Performing;
import live.noxbox.states.Requesting;
import live.noxbox.states.State;
import live.noxbox.tools.ExchangeRate;
import live.noxbox.tools.Router;
import live.noxbox.tools.exceptions.LocationException;
import live.noxbox.tools.location.LocationOperator;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Strings.isNullOrEmpty;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.database.AppCache.availableNoxboxes;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.fireProfile;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.services.ConnectivityManager.checkGpsEnabled;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.MapOperator.animateCameraToCurrentCountry;
import static live.noxbox.tools.MapOperator.enterTheMap;
import static live.noxbox.tools.MapOperator.setupMap;
import static live.noxbox.tools.ReferrerCatcher.clearReferrer;
import static live.noxbox.tools.ReferrerCatcher.referrer;
import static live.noxbox.tools.location.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.location.LocationOperator.initLocationProviderClient;
import static live.noxbox.tools.location.LocationOperator.updateLocation;
import static live.noxbox.tools.location.LocationUpdater.KEY_REQUESTING_LOCATION_UPDATES;
import static live.noxbox.tools.location.LocationUpdater.REQUEST_CHECK_LOCATION_SETTINGS;

public class MapActivity extends DemonstrationActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {
    private static FirebaseRemoteConfig mFirebaseRemoteConfig;

    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    public static final String TAG = MapActivity.class.getSimpleName();

    private LocationReceiver locationReceiver;

    private Boolean requestLocationUpdatesBundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.MapTheme);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        super.onCreate(savedInstanceState);
        initCrashReporting();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null || isFirstRun(false)) {
            Router.startActivity(this, AuthActivity.class);
            finish();
            return;
        }

        fetchConfig();

        AppCache.profile().init(user);

        Crashlytics.setUserIdentifier(user.getUid());
        FirebaseMessaging.getInstance().subscribeToTopic(user.getUid()).addOnFailureListener(e -> Crashlytics.log(Log.ERROR, "failToSubscribeOnProfile", user.getUid()));

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapId);
        mapFragment.getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();

        updateValuesFromBundle(savedInstanceState);
        new Thread(() -> checkBalance(profile(), MapActivity.this)).start();
        initLocationProviderClient(getApplicationContext());
        ExchangeRate.wavesToUSD(rate -> AppCache.wavesToUsd = rate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkGpsEnabled(this);
        if (NetworkReceiver.isOnline(this)) {
            AppCache.startListening();
        }

        locationReceiver = new LocationReceiver(this);
        registerReceiver(locationReceiver, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
        googleApiClient.connect();
        draw();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationReceiver);
        googleApiClient.disconnect();

        if (googleMap != null) {
            LocationOperator.actualMapTarget = CameraUpdateFactory.newLatLngZoom(googleMap.getCameraPosition().target, googleMap.getCameraPosition().zoom);
            googleMap.clear();
        }
        if (availableNoxboxes != null) {
            availableNoxboxes.clear();
        }
        AppCache.stopListen(this.getClass().getName());
        if (currentState != null) {
            currentState.clearHandlers();
            currentState = null;
        }
    }


    @Override
    public void onMapReady(GoogleMap readyMap) {
        googleMap = readyMap;

        updateLocation(this, googleMap);

        setupMap(this, googleMap);


        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        draw();
    }

    @Override
    public void onConfigurationChanged(android.content.res.Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (currentState == null) return;

        if (currentState instanceof AvailableNoxboxes) {
            showAvailableDemonstration();
        }
        if (currentState instanceof Moving) {
            showMovingDemonstration();
        }
        //TODO (vl) to remove operator if after there're being to add all customs view's
        if (currentState instanceof Performing) {
            draw();
        }
    }

    private void fetchConfig() {
        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings = new FirebaseRemoteConfigSettings.Builder()
                .setDeveloperModeEnabled(BuildConfig.DEBUG)
                .setMinimumFetchIntervalInSeconds(5)
                .build();
        mFirebaseRemoteConfig.setConfigSettings(configSettings);
        mFirebaseRemoteConfig.setDefaults(Collections.singletonMap("minAllowedVersion", BuildConfig.VERSION_CODE));
        mFirebaseRemoteConfig.fetchAndActivate().addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                long minAllowedVersion = mFirebaseRemoteConfig.getLong("minAllowedVersion");
                if (minAllowedVersion > BuildConfig.VERSION_CODE) {
                    showRequestUpdate();
                }
            }
        });


    }

    public void onSaveInstanceState(Bundle savedInstanceState) {
        if (currentState != null && currentState instanceof AvailableNoxboxes) {
            AvailableNoxboxes availableNoxboxesState = (AvailableNoxboxes) currentState;
            availableNoxboxesState.onSaveRequestingLocationUpdatesState(savedInstanceState);
        }
        super.onSaveInstanceState(savedInstanceState);
    }


    private void updateValuesFromBundle(Bundle savedInstanceState) {
        if (savedInstanceState != null && savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
            requestLocationUpdatesBundle = ((Boolean) savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES)) != null
                    ? savedInstanceState.getBoolean(KEY_REQUESTING_LOCATION_UPDATES)
                    : null;
            if (currentState != null && currentState instanceof AvailableNoxboxes) {
                AvailableNoxboxes availableNoxboxesState = (AvailableNoxboxes) currentState;
                availableNoxboxesState.updateRequestingLocationUpdatesFromBundle(requestLocationUpdatesBundle);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    draw();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHECK_LOCATION_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        Crashlytics.logException(new LocationException("User agreed to make required location settings changes."));
                        break;
                    case Activity.RESULT_CANCELED:
                        Crashlytics.logException(new LocationException("User chose not to make required location settings changes."));
                        Bundle bundle = new Bundle();
                        bundle.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
                        updateValuesFromBundle(bundle);
                        break;
                }
                break;
        }
    }


    private State currentState;

    private void draw() {
        AppCache.listenProfile(this.getClass().getName(), profile -> {
            if (googleMap == null) return;

            animateCameraToCurrentCountry(profile, googleMap);

            enterTheMap(googleMap, this);

            if (!isNullOrEmpty(referrer) && !equal(profile().getReferral(), referrer)
                    && !equal(profile().getId(), referrer)) {
                profile().setReferral(referrer);
                clearReferrer();
                fireProfile();
            }

            State newState = getFragment(profile);
            if (newState instanceof AvailableNoxboxes && requestLocationUpdatesBundle != null) {
                ((AvailableNoxboxes) newState).updateRequestingLocationUpdatesFromBundle(requestLocationUpdatesBundle);
            }

            if (newState instanceof Moving) {
                showMovingDemonstration();
            } else if (newState instanceof AvailableNoxboxes) {
                showAvailableDemonstration();
            }

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

            if (profile.getCurrent() != null && NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.created
                    && currentState instanceof AvailableNoxboxes
                    && newState instanceof AvailableNoxboxes
                    && ((AvailableNoxboxes) currentState).getDecorator() == null
                    && ((AvailableNoxboxes) newState).getDecorator() != null) {
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
                newState = new AvailableNoxboxes(new Created());
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

    public void hideUi() {
        findViewById(R.id.pointerImage).setVisibility(View.GONE);
        findViewById(R.id.menu).setVisibility(View.GONE);
        findViewById(R.id.filter).setVisibility(View.GONE);
        findViewById(R.id.chat).setVisibility(View.GONE);
        findViewById(R.id.totalUnread).setVisibility(View.GONE);
        findViewById(R.id.navigation).setVisibility(View.GONE);
        findViewById(R.id.switcherLayout).setVisibility(View.GONE);
        findViewById(R.id.locationButton).setVisibility(View.GONE);
        findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        findViewById(R.id.container).setVisibility(View.GONE);
    }

}