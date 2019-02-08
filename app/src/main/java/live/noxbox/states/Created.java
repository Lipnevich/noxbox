package live.noxbox.states;

import android.app.Activity;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;

import com.google.android.gms.maps.GoogleMap;

import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.analitics.LogEvents;
import live.noxbox.model.Profile;
import live.noxbox.tools.DateTimeFormatter;

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

    public Created(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        LogEvents.generateLogEvent(activity, "noxbox_created");
    }

    @Override
    public void draw(final Profile profile) {
        Log.d(TAG + "Created", "timeCreated: " + DateTimeFormatter.time(profile.getCurrent().getTimeCreated()));
        activity.findViewById(R.id.menu).setVisibility(View.VISIBLE);

        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.edit);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.customFloatingView).setOnClickListener(v -> startActivity(activity, ContractActivity.class));

        activity.findViewById(R.id.locationButton).setVisibility(View.VISIBLE);
        moveCopyrightRight(googleMap);
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
