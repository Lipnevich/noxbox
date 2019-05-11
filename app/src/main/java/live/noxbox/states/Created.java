package live.noxbox.states;

import android.view.View;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import live.noxbox.MapActivity;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.states.decorator.StatesDecorator;

import static live.noxbox.Constants.BIG_MARKER_SIZE;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.MapOperator.buildMapPosition;
import static live.noxbox.tools.MapOperator.clearMapMarkerListener;
import static live.noxbox.tools.MarkerCreator.createCustomMarker;
import static live.noxbox.tools.Router.startActivity;

public class Created extends StatesDecorator {

    private StatesDecorator statesDecorator;

    public Created(StatesDecorator statesDecorator) {
        this.statesDecorator = statesDecorator;
    }

    public Created() {
    }

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
        //TODO (vl) объяснить пользователю что большая иконка есть его созданная услуга
        createCustomMarker(profile().getCurrent(), googleMap, activity.getResources(), BIG_MARKER_SIZE);
        //setNoxboxMarkerListener(googleMap, profile, activity);
        buildMapPosition(googleMap, activity.getApplicationContext());

        if (statesDecorator != null) {
            statesDecorator.draw(googleMap, activity);
        }
    }

    @Override
    public void clear() {
        if (statesDecorator != null) {
            statesDecorator.clear();
        }
        clearMapMarkerListener(googleMap);
        activity.findViewById(R.id.menu).setVisibility(View.GONE);
        activity.findViewById(R.id.locationButton).setVisibility(View.GONE);
        ((FloatingActionButton) activity.findViewById(R.id.customFloatingView)).setImageResource(R.drawable.add);
        activity.findViewById(R.id.customFloatingView).setVisibility(View.GONE);
        googleMap.clear();
    }

}
