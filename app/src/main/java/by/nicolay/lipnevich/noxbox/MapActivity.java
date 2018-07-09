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
package by.nicolay.lipnevich.noxbox;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.constructor.ConstructorNoxboxPage;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.pages.Fragment;
import by.nicolay.lipnevich.noxbox.pages.InitialFragment;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.ConfirmationMessage;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.Configuration.LOCATION_PERMISSION_REQUEST_CODE;
import static by.nicolay.lipnevich.noxbox.tools.PathFinder.getPathPoints;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public class MapActivity extends MenuActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    protected GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Map<String, GroundOverlay> markers = new HashMap<>();
    private Map<String, Polyline> pathes = new HashMap<>();
    private ImageView pathImage;
    private ImageView locationButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapId)).getMapAsync(this);
        pathImage = findViewById(R.id.pathImage);
        connectGoogleApi();
    }


    private void connectGoogleApi() {
        googleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addApi(LocationServices.API).build();
        googleApiClient.connect();
    }

    @Override
    public void onMapReady(GoogleMap readyMap) {
        googleMap = readyMap;
        scaleMarkers();
        visibleCurrentLocation(true);
        // TODO (nli) night and day mode
//        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.map_in_night));
        googleMap.setMaxZoomPreference(18);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        moveGoogleCopyrights();
        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                scaleMarkers();
            }
        });
        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    protected boolean checkLocationPermission() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED;
    }

    protected void visibleCurrentLocation(boolean visible) {
        // TODO (nli) launch gps and ask permissions after button pressed only
        if (checkLocationPermission()) {
            // Permission to access the location is missing.
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);
            locationButton = findViewById(R.id.locationButton);
            locationButton.setVisibility(View.VISIBLE);
            locationButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ActivityCompat.requestPermissions(MapActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                            LOCATION_PERMISSION_REQUEST_CODE);
                }
            });
        } else {
            if (locationButton != null) {
                locationButton.setVisibility(View.INVISIBLE);
            }
            googleMap.setMyLocationEnabled(true);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);
            if (!visible) {
                return;
            }
            // Access to the location has been granted to the app.
            // Get the requestButton view
            View locationButton = ((View) findViewById(new Integer(1)).getParent()).findViewById(new Integer(2));

            // and next place it, for example, on bottom right (as Google Maps app)
            RelativeLayout.LayoutParams layout = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
            // position on right bottom
            layout.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
            layout.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            layout.setMargins(0, 0, 0, dpToPx(8));
            locationButton.setLayoutParams(layout);
        }

        findViewById(R.id.noxboxConstructorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MapActivity.this, ConstructorNoxboxPage.class));
            }
        });
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
                }
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Position position = getCurrentPosition();
        if (position != null && googleMap != null) {
            googleMap.moveCamera(newLatLngZoom(position.toLatLng(), 15));
        }
    }


    @Override
    protected void onPause() {
        googleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (!checkLocationPermission()) {
            if (!isGpsEnabled()) {
                ConfirmationMessage.messageGps(this);
            }
        }
        googleApiClient.connect();
        scaleMarkers();
        super.onResume();
    }

    protected Position getCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (googleApiClient.isConnected()) {
                return Position.from(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
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

    protected void drawPath(final Noxbox noxbox) {
        // TODO (nli) use icon for noxbox type

        AsyncTask<Void, Void, Map.Entry<Integer, List<LatLng>>> asyncTask = new AsyncTask<Void, Void, Map.Entry<Integer, List<LatLng>>>() {
            @Override
            protected Map.Entry<Integer, List<LatLng>> doInBackground(Void... params) {
                return getPathPoints(noxbox.getPerformer().getPosition(), noxbox.getPayer().getPosition(),
                        noxbox.getPerformer().getTravelMode(), getResources().getString(R.string.google_directions_key));
            }

            @Override
            protected void onPostExecute(Map.Entry<Integer, List<LatLng>> responce) {
                if (pathes.containsKey(noxbox.getPerformer().getId())) {
                    pathes.remove(noxbox.getPerformer().getId()).remove();
                }

                List<LatLng> points;
                if (responce != null) {
                    points = responce.getValue();
                    // TODO (nli) show time on the map
                } else {
                    // TODO (nli) draw curve
                    points = new ArrayList<>();
                    points.add(new LatLng(noxbox.getPerformer().getPosition().getLatitude(), noxbox.getPerformer().getPosition().getLongitude()));
                    points.add(new LatLng(noxbox.getPayer().getPosition().getLatitude(), noxbox.getPayer().getPosition().getLongitude()));
                }

                Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                        .color(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                        .width(11)
                        .pattern(Arrays.asList((PatternItem) new Dot()))
                        .addAll(points));
                pathes.put(noxbox.getPerformer().getId(), polyline);
                focus(noxbox.getPerformer().getId(), noxbox.getPayer().getId());
            }
        };
        asyncTask.execute();
    }

    public Position getCameraPosition() {
        LatLng latLng = googleMap.getCameraPosition().target;
        if (latLng == null) {
            throw new RuntimeException("Null camera position");
        }
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

    protected void cleanUpMap() {
        Iterator iterator = markers.values().iterator();
        while (iterator.hasNext()) {
            GroundOverlay item = (GroundOverlay) iterator.next();
            item.remove();
            iterator.remove();
        }

        iterator = pathes.values().iterator();
        while (iterator.hasNext()) {
            Polyline item = (Polyline) iterator.next();
            item.remove();
            iterator.remove();
        }
    }

    public void processNoxbox(Noxbox noxbox) {
        moveGoogleCopyrights();

        visibleCurrentLocation(false);

        pathImage.setVisibility(View.VISIBLE);
        pathImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                focus();
            }
        });
    }

    private void focus(String... array) {
        List<String> ids = Arrays.asList(array);
        if (!pathes.isEmpty() && googleMap != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Polyline polyline : pathes.values()) {
                for (LatLng point : polyline.getPoints()) {
                    builder.include(point);
                }
            }
            for (Map.Entry<String, GroundOverlay> marker : markers.entrySet()) {
                if (ids.isEmpty() || ids.contains(marker.getKey())) {
                    builder.include(marker.getValue().getPosition());
                }
            }
            LatLngBounds bounds = builder.build();
            int padding = dpToPx(36); // padding around start and end points
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    public void prepareForIteration() {
        if (googleMap != null) {
            visibleCurrentLocation(true);
            cleanUpMap();
            moveGoogleCopyrights();
            pathImage.setVisibility(View.INVISIBLE);
        }
    }

    private void moveGoogleCopyrights() {
        if (googleMap != null) {
            googleMap.setPadding(dpToPx(13), dpToPx(64), dpToPx(292), dpToPx(70));
        }
    }

    private Fragment currentFragment;

    private void draw(@NonNull Profile profile) {
        Fragment newFragment = getFragment(profile);
        if (newFragment != currentFragment && currentFragment != null) {
            currentFragment.clear();
        }
        currentFragment = newFragment;
        currentFragment.draw(profile);
    }

    public Fragment getFragment(Profile profile) {
        if (profile.getCurrent() == null) return new InitialFragment(googleMap, this);
//    created,
//    requesting,
//    accepting,
//    moving,
//    watching,
//    performing,
//    enjoying,
//    completed;

        return new InitialFragment(googleMap, this);
    }

}
