package live.noxbox.pages;

import android.app.Activity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.R;
import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.model.Profile;
import live.noxbox.state.State;
import live.noxbox.tools.MapController;
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
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setImageResource(R.drawable.edit);
        ((CardView) activity.findViewById(R.id.customFloatingView)).setVisibility(View.VISIBLE);
        ((CardView) activity.findViewById(R.id.customFloatingView)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(activity, ConstructorActivity.class);
            }
        });

        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
            }
        });

        MarkerCreator.createCustomMarker(profile.getCurrent(), googleMap, activity, profile.getTravelMode());

        MapController.buildMapMarkerListener(googleMap, profile, activity);

        MapController.buildMapPosition(googleMap, profile, activity.getApplicationContext());
    }

    @Override
    public void clear() {
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        ((ImageView) activity.findViewById(R.id.customFloatingImage)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        googleMap.clear();

    }
}
