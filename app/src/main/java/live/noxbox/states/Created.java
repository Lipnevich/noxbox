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

import static live.noxbox.tools.MapOperator.buildMapPosition;
import static live.noxbox.tools.MapOperator.clearMapMarkerListener;
import static live.noxbox.tools.MapOperator.setNoxboxMarkerListener;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;
import static live.noxbox.tools.Router.startActivity;

public class Created implements State {

    private GoogleMap googleMap;
    private Activity activity;
    private Profile profile = AppCache.profile();

    @Override
    public void draw(GoogleMap googleMap, MapActivity activity) {
        this.googleMap = googleMap;
        this.activity = activity;

        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.edit);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> startActivity(activity, ContractActivity.class));

        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.locationButton).setOnClickListener(v -> buildMapPosition(googleMap, activity.getApplicationContext()));

        createCustomMarker(profile.getCurrent(), googleMap, activity.getResources());
        setNoxboxMarkerListener(googleMap, profile, activity);
        buildMapPosition(googleMap, activity.getApplicationContext());
    }

    @Override
    public void clear() {
        clearMapMarkerListener(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        googleMap.clear();

    }

}
