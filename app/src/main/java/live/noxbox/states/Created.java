package live.noxbox.states;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;

import static live.noxbox.database.GeoRealtime.online;
import static live.noxbox.tools.MapOperator.buildMapMarkerListener;
import static live.noxbox.tools.MapOperator.buildMapPosition;
import static live.noxbox.tools.MapOperator.clearMapMarkerListener;
import static live.noxbox.tools.MapOperator.moveCopyrightLeft;
import static live.noxbox.tools.MapOperator.moveCopyrightRight;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;
import static live.noxbox.tools.Router.startActivity;

public class Created implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private Profile profile = AppCache.profile();
    private boolean initiated;

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        if (!initiated) {
            online(profile.getCurrent());
            initiated = true;
        }

        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.edit);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> startActivity(activity, ContractActivity.class));

        moveCopyrightRight(googleMap);
        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(v -> buildMapPosition(googleMap, activity.getApplicationContext()));

        createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        buildMapMarkerListener(googleMap, profile, activity);
        buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void clear() {
        clearMapMarkerListener(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        moveCopyrightLeft(googleMap);
        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        googleMap.clear();

    }

}
