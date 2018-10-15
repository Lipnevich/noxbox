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
import android.content.res.Resources;
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
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.pages.Accepting;
import live.noxbox.pages.AvailableServices;
import live.noxbox.pages.Created;
import live.noxbox.pages.DebugActivity;
import live.noxbox.pages.Moving;
import live.noxbox.pages.Performing;
import live.noxbox.pages.Requesting;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.ConfirmationMessage;
import live.noxbox.tools.MapController;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.tools.MapController.moveCopyrightLeft;
import static live.noxbox.tools.MapController.setupMap;

public class MapActivity extends DebugActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    protected GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Map<String, GroundOverlay> markers = new HashMap<>();

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
        scaleMarkers();
        visibleCurrentLocation(true);
        setupMap(this, googleMap);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        moveCopyrightLeft(googleMap);
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                scaleMarkers();
            }
        });
        draw();
    }

    protected boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    protected void visibleCurrentLocation(boolean visible) {
        if (isLocationPermissionGranted()) {
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

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
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
                if (profile.getPosition() != null) {
                    MapController.buildMapPosition(googleMap, profile, getApplicationContext());
                }
            }
        });
    }


    @Override
    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();

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
        scaleMarkers();
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

    protected void scaleMarkers() {
        float size = getScaledSize();
        for (GroundOverlay marker : markers.values()) {
            marker.setDimensions(size, size);
        }
    }

    protected void removeOutOfRangeMarkers() {
        Iterator it = markers.values().iterator();
        while (it.hasNext()) {
            GroundOverlay item = (GroundOverlay) it.next();
            if (Position.from(item.getPosition()).toLocation().distanceTo(getCameraPosition().toLocation())
                    > Configuration.RADIUS_IN_METERS) {
                item.remove();
                it.remove();
            }
        }
    }

    private float getScaledSize() {
        if (googleMap != null && googleMap.getCameraPosition() != null) {
            return (float) Math.pow(2, 22 - Math.max(googleMap.getCameraPosition().zoom, 11));
        } else {
            return 0;
        }

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

    public Position getCameraPosition() {
        LatLng latLng = googleMap.getCameraPosition().target;

        return Position.from(latLng);
    }

    protected Noxbox chooseBestOptionPerformer(Profile profile) {
        int minEstimation = Integer.MAX_VALUE;
        Noxbox noxbox = null;
        // TODO (nli) hide performers from black list
        for (Map.Entry<String, GroundOverlay> marker : markers.entrySet()) {
            if (marker.getValue().getTag() == null) {
                continue;
            }
            TravelMode travelMode = TravelMode.valueOf(marker.getValue().getTag().toString());
            int estimation = getEstimationInMinutes(getCameraPosition(),
                    Position.from(marker.getValue().getPosition()), travelMode);
            if (estimation < minEstimation) {
                minEstimation = estimation;
                String performerId = marker.getKey();
                noxbox = new Noxbox().setEstimationTime("" + estimation)
                        .setPosition(getCameraPosition())
                        .setOwner(profile.publicInfo()).setParty(new Profile().setId(performerId)
                                .setTravelMode(travelMode).setPosition(Position.from(marker.getValue().getPosition())));
            }
        }
        return noxbox;
    }


    private State currentState;

    private void draw() {
        ProfileStorage.listenProfile(this.getClass().getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (googleMap == null) return;
                State newState = getFragment(profile);
                if (newState != currentState && currentState != null) {
                    currentState.clear();
                }
                currentState = newState;
                newState.draw(profile);
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