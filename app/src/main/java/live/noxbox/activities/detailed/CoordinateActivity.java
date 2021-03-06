package live.noxbox.activities.detailed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;

import java.util.Arrays;
import java.util.List;

import live.noxbox.Constants;
import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.tools.Router;
import live.noxbox.tools.location.LocationOperator;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.MapOperator.setupMap;
import static live.noxbox.tools.location.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.location.LocationOperator.initLocationProviderClient;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;
import static live.noxbox.tools.location.LocationOperator.updateLocation;

public class CoordinateActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final int COORDINATE = 111;

    private ImageView homeButton;
    private ImageView locationButton;
    private Button choosePlace;


    private GoogleMap googleMap;

    private List<Place.Field> placeField = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate);
        initializeUi();

        initLocationProviderClient(getApplicationContext());

        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setPlaceFields(placeField);


        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                moveCamera(place.getLatLng(), 15);
            }

            @Override
            public void onError(Status status) {
            }
        });


        draw();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onMapReady(final GoogleMap readyMap) {
        googleMap = readyMap;
        updateLocation(getApplicationContext(), googleMap);
        setupMap(this, googleMap);
        
        if(LocationOperator.actualMapTarget != null){
            googleMap.moveCamera(LocationOperator.actualMapTarget);
        }


        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        draw();
    }

    private void initializeUi() {
        homeButton = findViewById(R.id.homeButton);
        locationButton = findViewById(R.id.locationButton);
        choosePlace = findViewById(R.id.choosePlace);
    }

    private void draw() {
        drawToolbar();
        drawLocationButton();
        drawChoosePlaceButton();
    }

    private void drawToolbar() {
        homeButton.setOnClickListener(v -> Router.finishActivity(CoordinateActivity.this));
    }

    private void drawLocationButton() {
        locationButton.setOnClickListener(v -> {
            startLocationPermissionRequest(CoordinateActivity.this, Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS);
            getDeviceLocation(profile(), googleMap, CoordinateActivity.this);
        });
    }

    private void drawChoosePlaceButton() {
        choosePlace.setOnClickListener(v -> {
            providePositionResult();
        });
    }

    private void providePositionResult() {
        if (googleMap == null) return;

        final LatLng latLng = googleMap.getCameraPosition().target;
        if (latLng == null) {
            Crashlytics.log(Log.ERROR, this.getClass().getSimpleName(), "Empty camera position on google maps");
            return;
        }
        Intent intent = new Intent();
        intent.putExtra(LAT, latLng.latitude);
        intent.putExtra(LNG, latLng.longitude);
        setResult(RESULT_OK, intent);
        LocationOperator.actualMapTarget = CameraUpdateFactory.newLatLngZoom(latLng, googleMap.getCameraPosition().zoom);
        Router.finishActivity(CoordinateActivity.this);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getDeviceLocation(profile(), googleMap, this);
                    executeUITasks();
                }
                break;
        }
        updateLocation(this, googleMap);
    }
}
