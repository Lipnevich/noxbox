package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.MarkerCreator;

import static by.nicolay.lipnevich.noxbox.state.ProfileStorage.fireProfile;

public class Requesting implements State {

    private GoogleMap googleMap;
    private Activity activity;

    public Requesting(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        MarkerCreator.createCustomMarker(profile.getCurrent(), profile, googleMap, activity);
        MarkerCreator.addPulsatingEffect(googleMap,profile,activity);
        activity.findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.findViewById(R.id.cancelButton).setVisibility(View.GONE);
                profile.getCurrent().setTimeRequested(null);
                fireProfile();
            }
        });
        moveCamera(profile.getCurrent().getPosition().toLatLng(),17);
        googleMap.getUiSettings().setScrollGesturesEnabled(false);
    }

    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.cancelButton).setVisibility(View.GONE);
        googleMap.getUiSettings().setScrollGesturesEnabled(true);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        CameraPosition cameraPosition
                = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.animateCamera(cameraUpdate);
    }

}
