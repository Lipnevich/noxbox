package live.noxbox.pages;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.state.State;
import live.noxbox.tools.MarkerCreator;
import live.noxbox.tools.NoxboxExamples;
import live.noxbox.tools.Task;

public class AvailableServices implements State, GoogleMap.OnMarkerClickListener {

    private Map<String, Marker> markers = new HashMap<>();
    private GoogleMap googleMap;
    private GoogleApiClient googleApiClient;
    private Activity activity;
    private List<Noxbox> noxboxes;

    public AvailableServices(GoogleMap googleMap, final GoogleApiClient googleApiClient, final Activity activity) {
        this.googleMap = googleMap;
        this.googleApiClient = googleApiClient;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        activity.findViewById(R.id.noxboxConstructorButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.noxboxConstructorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profile.getCurrent().setPosition(Position.from(googleMap.getCameraPosition().target));
                if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    profile.getCurrent().getOwner().setPosition(Position.from(LocationServices.FusedLocationApi.getLastLocation(googleApiClient)));
                }

                activity.startActivity(new Intent(activity, ConstructorActivity.class));

            }
        });
        activity.findViewById(R.id.debug_noxbox_example).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.debug_noxbox_example).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(noxboxes == null){
                    noxboxes = NoxboxExamples.generateNoxboxes(new Position().setLongitude(27.569018).setLatitude(53.871399), 50, activity);
                }
                for (Noxbox noxbox : noxboxes) {
                    if (profile.getTravelMode() == TravelMode.none && noxbox.getOwner().getTravelMode() == TravelMode.none
                            || profile.getTravelMode() != TravelMode.none && noxbox.getOwner().getTravelMode() != TravelMode.none && !profile.getHost() && !noxbox.getOwner().getHost()) {
                        //do not show this marker
                    } else {
                        createMarker(profile, noxbox);
                    }
                }
                googleMap.setOnMarkerClickListener(AvailableServices.this);
            }
        });




        if (profile.getPosition() != null) {
            CameraPosition cameraPosition
                    = new CameraPosition.Builder()
                    .target(profile.getPosition().toLatLng())
                    .zoom(13)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
        }
    }

    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.noxboxConstructorButton).setVisibility(View.GONE);
    }


    public void createMarker(Profile profile, Noxbox noxbox) {
        if (markers.get(noxbox.getId()) == null) {
            markers.put(noxbox.getId(), MarkerCreator.createCustomMarker(noxbox, googleMap, activity, noxbox.getOwner().getTravelMode()));
        }
    }

    public void removeMarker(String key) {
        Marker marker = markers.remove(key);
        if (marker != null) {
            marker.remove();
        }
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.setViewed((Noxbox) marker.getTag());
                profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                activity.startActivity(new Intent(activity, DetailedActivity.class));
            }
        });
        return false;
    }

}
