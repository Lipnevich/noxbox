package live.noxbox.pages;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.state.State;
import live.noxbox.tools.MarkerCreator;

import static live.noxbox.tools.Router.startActivity;

public class Created implements State {

    private GoogleMap googleMap;
    private Activity activity;

    public Created(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(final Profile profile) {
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));

        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setImageResource(R.drawable.edit);
        ((CardView) activity.findViewById(R.id.customFloatingView)).setVisibility(View.VISIBLE);
        ((CardView) activity.findViewById(R.id.customFloatingView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(activity, ConstructorActivity.class);
            }
        });
        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));
            }
        });
        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity, profile.getTravelMode());
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                profile.setViewed((Noxbox) marker.getTag());
                startActivity(activity, DetailedActivity.class);
                return true;
            }
        });

    }

    @Override
    public void clear() {
        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        googleMap.clear();

    }
}
