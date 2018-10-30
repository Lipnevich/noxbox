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
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;

import live.noxbox.debug.DebugActivity;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.pages.Accepting;
import live.noxbox.pages.AvailableServices;
import live.noxbox.pages.Created;
import live.noxbox.pages.Moving;
import live.noxbox.pages.Performing;
import live.noxbox.pages.Requesting;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.ConfirmationMessage;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.MapController.moveCopyrightLeft;
import static live.noxbox.tools.MapController.setupMap;

public class MapActivity extends DebugActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    protected GoogleMap googleMap;
    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapId)).getMapAsync(this);
        googleApiClient = new GoogleApiClient.Builder(this).addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .build();
        googleApiClient.connect();
        ProfileStorage.startListening();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onMapReady(GoogleMap readyMap) {
        googleMap = readyMap;
        visibleCurrentLocation(true);
        setupMap(this, googleMap);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        moveCopyrightLeft(googleMap);
        draw();
    }

    protected boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    protected void visibleCurrentLocation(boolean visible) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
            findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isLocationPermissionGranted()) {
                        return;
                    }
                    ProfileStorage.readProfile(new Task<Profile>() {
                        @Override
                        public void execute(Profile profile) {
                            profile.setPosition(getCurrentPosition());
                            ProfileStorage.fireProfile();
                        }
                    });
                }
            });

            if (!visible) {
                return;
            }
            View locationButton = ((View) findViewById(new Integer(1)).getParent()).findViewById(new Integer(2));

            RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            layout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layout.setMargins(0, 0, 0, dpToPx(8));
            locationButton.setLayoutParams(layout);
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

    protected boolean isGpsEnabled() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    visibleCurrentLocation(true);
                    ProfileStorage.readProfile(new Task<Profile>() {
                        @Override
                        public void execute(Profile profile) {
                            profile.setPosition(getCurrentPosition());
                            ProfileStorage.fireProfile();
                        }
                    });
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.setPosition(getCurrentPosition());
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
        ProfileStorage.stopListen(this.getClass().getName());
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (currentState != null) currentState.clear();

    }

    @Override
    protected void onResume() {
        if (isLocationPermissionGranted()) {
            if (!isGpsEnabled()) {
                ConfirmationMessage.messageGps(this);
            }
        }
        googleApiClient.connect();
        draw();


        super.onResume();
    }

    protected Position getCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleApiClient.isConnected()) {
                Position position = Position.from(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
                return position;
            } else {
                googleApiClient.connect();
            }
        }
        return null;
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
        ProfileStorage.listenProfile(this.getClass().getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (googleMap == null) return;
                State newState = getFragment(profile);
                if (currentState == null) {
                    currentState = newState;
                    newState.draw(profile);
                    return;
                }

                if (!newState.getClass().getName().equals(currentState.getClass().getName())) {
                    currentState.clear();
                    currentState = newState;
                    newState.draw(profile);
                    return;
                }

                currentState.draw(profile);
            }
        });
    }

    public State getFragment(final Profile profile) {
        NoxboxState state = NoxboxState.getState(profile.getCurrent(), profile);
        switch (state) {
            case initial:
                return new AvailableServices(googleMap, googleApiClient, this);
            case created:
                return new Created(googleMap, this);
            case requesting:
                return new Requesting(googleMap, this);
            case accepting:
                return new Accepting(googleMap, this);
            case moving:
                return new Moving(googleMap, this);
            case performing:
                return new Performing(this, googleMap);
            default:
                throw new IllegalStateException("Unknown state: " + state.name());
        }
    }
}