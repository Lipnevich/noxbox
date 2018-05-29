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

import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
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

import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.payer.massage.R;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Timer;
import by.nicolay.lipnevich.noxbox.tools.TravelMode;

import static by.nicolay.lipnevich.noxbox.tools.Firebase.tryGetNoxboxInProgress;
import static by.nicolay.lipnevich.noxbox.tools.PathFinder.getPathPoints;
import static com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom;

public abstract class MapActivity extends ProfileActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 911;

    protected GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Map<String, GroundOverlay> markers = new HashMap<>();
    private Map<String, Polyline> pathes = new HashMap<>();
    private ImageView pathImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        ((MapFragment) getFragmentManager().findFragmentById(R.id.mapId))
                .getMapAsync(this);
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
    }

    protected void visibleCurrentLocation(boolean visible) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            if(googleMap != null) {
                googleMap.setMyLocationEnabled(visible);
            }

            if(!visible) {
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
    }

    public static int dpToPx(int dp)
    {
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
        if(position != null && googleMap != null && tryGetNoxboxInProgress() == null) {
            googleMap.moveCamera(newLatLngZoom(position.toLatLng(), 15));
        }
        scaleMarkers();
    }

    @Override
    protected void onStop() {
        googleApiClient.disconnect();
        super.onStop();
    }

    @Override
    protected void onResume() {
        googleApiClient.connect();
        scaleMarkers();
        super.onResume();
    }

    protected Position getCurrentPosition() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if(googleApiClient.isConnected()) {
                return Position.from(LocationServices.FusedLocationApi.getLastLocation(googleApiClient));
            } else {
                googleApiClient.connect();
            }
        }
        return null;
    }

    protected void scaleMarkers() {
        float size = getScaledSize();
        for(GroundOverlay marker : markers.values()) {
            marker.setDimensions(size, size);
        }
    }

    protected void removeOutOfRangeMarkers() {
        Iterator it = markers.values().iterator();
        while (it.hasNext()) {
            GroundOverlay item = (GroundOverlay) it.next();
            if (Position.from(item.getPosition()).toLocation().distanceTo(getCameraPosition().toLocation())
                    > getResources().getInteger(R.integer.radius_in_kilometers) * 1000) {
                item.remove();
                it.remove();
            }
        }
    }

    private float getScaledSize() {
        if(googleMap != null && googleMap.getCameraPosition() != null) {
            return (float) Math.pow(2, 22 - Math.max(googleMap.getCameraPosition().zoom, 11));
        } else {
            return 128;
        }

    }

    protected int getEstimationInMinutes(Position from, Position to, TravelMode travelMode) {
        float distanceInMeters = from.toLocation()
                .distanceTo(to.toLocation());
        int estimationInMinutes = (int) (distanceInMeters / travelMode.getSpeedInMetersPerMinute());
        return estimationInMinutes > 0 ? estimationInMinutes : 1;
    }

    @Override public void onConnectionSuspended(int i) {}

    protected void drawPathToNoxbox(Profile performer, final Noxbox noxbox) {
        drawPath(noxbox.getId(), performer, new Profile().setId(noxbox.getId()).setPosition(noxbox.getPosition()));
    }

    protected void drawPathsToAllPerformers(final Noxbox noxbox) {
        if(googleMap == null) {
            new Timer() {
                @Override
                protected void timeout() {
                    drawPathsToAllPerformers(noxbox);
                }
            }.start(1);
            return;
        }

        for (Profile performer : noxbox.getPerformers().values()) {
            drawPath(noxbox.getId(), performer, new Profile().setId(noxbox.getId()).setPosition(noxbox.getPosition()));
        }
    }

    protected void drawPath(final String noxboxId, final Profile performer, final Profile payer) {
        if(performer.getPosition() == null && noxboxId != null && tryGetNoxboxInProgress() != null) {
            performer.setPosition(tryGetNoxboxInProgress()
                    .getPerformers().get(performer.getId()).getPosition());
        }

        draw(performer, getPerformerDrawable());
        draw(payer, getPayerDrawable());

        AsyncTask<Void, Void, Map.Entry<Integer, List<LatLng>>> asyncTask = new AsyncTask<Void, Void, Map.Entry<Integer, List<LatLng>>>() {
            @Override
            protected Map.Entry<Integer, List<LatLng>> doInBackground(Void... params) {
                return getPathPoints(performer.getPosition(), payer.getPosition(),
                        performer.getTravelMode(), getResources().getString(R.string.google_directions_key));
            }

            @Override
            protected void onPostExecute(Map.Entry<Integer, List<LatLng>> responce) {
                if(pathes.containsKey(performer.getId())) {
                    pathes.remove(performer.getId()).remove();
                }

                List<LatLng> points;
                if(responce != null) {
                    points = responce.getValue();
                    // TODO (nli) show time on the map
                    if(tryGetNoxboxInProgress() != null && responce.getKey() != null) {
                        Firebase.updateCurrentNoxbox(tryGetNoxboxInProgress()
                                .setEstimationTime(responce.getKey().toString()));
                    }
                } else {
                    // TODO (nli) draw curve
                    points = new ArrayList<>();
                    points.add(new LatLng(performer.getPosition().getLatitude(), performer.getPosition().getLongitude()));
                    points.add(new LatLng(payer.getPosition().getLatitude(), payer.getPosition().getLongitude()));
                }

                Polyline polyline = googleMap.addPolyline(new PolylineOptions()
                        .color(ContextCompat.getColor(getApplicationContext(), R.color.primary))
                        .width(11)
                        .pattern(noxboxId == null ? Arrays.asList((PatternItem) new Dot()) : null)
                        .addAll(points));
                pathes.put(performer.getId(), polyline);
                focus(performer.getId(), payer.getId());
            }
        };
        asyncTask.execute();
    }

    public Position getCameraPosition() {
        LatLng latLng = googleMap.getCameraPosition().target;
        if(latLng == null) {
            throw new RuntimeException("Null camera position");
        }
        return Position.from(latLng);
    }

    protected void draw(Profile profile, int drawable) {
        if(profile.getPosition() != null) {
            createMarker(profile.getId(), profile.getPosition().toLatLng(), drawable);
        } else {
            Crashlytics.log(Log.WARN, "emptyPosition", "Empty position for profile "
                    + profile.publicInfo());
        }
    }

    public GroundOverlay createMarker(String key, LatLng latLng, int resource) {
        GroundOverlay marker = markers.get(key);
        if (marker == null) {
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(resource))
                    .position(latLng, 48, 48)
                    .anchor(0.5f, 1)
                    .zIndex(10);
            marker = googleMap.addGroundOverlay(newarkMap);
            marker.setDimensions(getScaledSize(), getScaledSize());
            markers.put(key, marker);
        }
        marker.setPosition(latLng);
        return marker;
    }

    public void removeMarker(String key) {
        GroundOverlay marker = markers.remove(key);
        if (marker != null) {
            marker.remove();
        }
    }

    protected Profile chooseBestOptionPerformer() {
        int minEstimation = Integer.MAX_VALUE;
        Profile performer = null;
        // TODO (nli) hide performers from black list
        for (Map.Entry<String, GroundOverlay> marker : markers.entrySet()) {
            if(marker.getValue().getTag() == null) {
                continue;
            }
            TravelMode travelMode = TravelMode.valueOf(marker.getValue().getTag().toString());
            int estimation = getEstimationInMinutes(getCameraPosition(),
                    Position.from(marker.getValue().getPosition()), travelMode);
            if (estimation < minEstimation) {
                minEstimation = estimation;
                String performerId = marker.getKey();
                performer = new Profile().setId(performerId).setEstimationTime("" + estimation)
                        .setTravelMode(travelMode).setPosition(Position.from(marker.getValue().getPosition()));
            }
        }
        return performer;
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

    @Override
    protected void processNoxbox(Noxbox noxbox) {
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

    private void focus(String ... array) {
        List<String> ids = Arrays.asList(array);
        if(!pathes.isEmpty() && googleMap != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (Polyline polyline : pathes.values()) {
                for (LatLng point : polyline.getPoints()) {
                    builder.include(point);
                }
            }
            for(Map.Entry<String, GroundOverlay> marker : markers.entrySet()) {
                if(ids.isEmpty() || ids.contains(marker.getKey())) {
                    builder.include(marker.getValue().getPosition());
                }
            }
            LatLngBounds bounds = builder.build();
            int padding = dpToPx(36); // padding around start and end points
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
        }
    }

    @Override
    protected void prepareForIteration() {
        if(googleMap != null) {
            visibleCurrentLocation(true);
            cleanUpMap();
            moveGoogleCopyrights();
            pathImage.setVisibility(View.INVISIBLE);
        }
    }

    private void moveGoogleCopyrights() {
        if(googleMap != null) {
            googleMap.setPadding(dpToPx(13), dpToPx(64), dpToPx(7), dpToPx(70));
        }

    }


}
