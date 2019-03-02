package live.noxbox.activities.detailed;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.Constants;
import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.model.Profile;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.ADDRESS_SEARCH_RADIUS_IN_METERS;
import static live.noxbox.Constants.DEFAULT_ZOOM_LEVEL;
import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.LocationOperator.getLocationPermission;
import static live.noxbox.tools.LocationOperator.initLocationProviderClient;
import static live.noxbox.tools.LocationOperator.updateLocation;
import static live.noxbox.tools.MapOperator.setupMap;

public class CoordinateActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final int COORDINATE = 111;


    private GoogleMap googleMap;
    private AutoCompleteTextView searchLine;
    private PlaceAutocompleteAdapter placeAutocompleteAdapter;
    private GoogleApiClient googleApiClient;

    private FusedLocationProviderClient providerClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate);

        providerClient = initLocationProviderClient(getApplicationContext());

        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
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

        setupMap(this, googleMap);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false);
        getDeviceLocation(profile(), googleMap, CoordinateActivity.this);
        updateLocation(this, googleMap);
        if (profile().getViewed().getPosition() != null) {
            moveCamera(profile().getViewed().getPosition().toLatLng(), DEFAULT_ZOOM_LEVEL);
        } else {
            moveCamera(profile().getCurrent().getPosition().toLatLng(), DEFAULT_ZOOM_LEVEL);
        }

        init(profile());

        draw();
    }

    private void draw() {
        drawToolbar();
        drawLocationButton();
        drawChoosePlaceButton();
    }

    private void drawToolbar() {
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(CoordinateActivity.this));
    }

    private void drawLocationButton() {
        findViewById(R.id.locationButton).setOnClickListener(v -> {
            getLocationPermission(CoordinateActivity.this, Constants.LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS);
            getDeviceLocation(profile(), googleMap, CoordinateActivity.this);
        });
    }

    private void drawChoosePlaceButton() {
        findViewById(R.id.choosePlace).setOnClickListener(v -> {

            providePositionResult();

        });
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
        Router.finishActivity(CoordinateActivity.this);
    }


    public LatLngBounds getAreaByRadiusAroundPoint(LatLng center, double radiusInMeters) {
        double distanceFromCenterToCorner = radiusInMeters * Math.sqrt(2.0);
        LatLng southwestCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 225.0);
        LatLng northeastCorner =
                SphericalUtil.computeOffset(center, distanceFromCenterToCorner, 45.0);
        return new LatLngBounds(southwestCorner, northeastCorner);
    }

    private void init(final Profile profile) {
        googleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        placeAutocompleteAdapter = new PlaceAutocompleteAdapter(
                this,
                googleApiClient,
                getAreaByRadiusAroundPoint(profile.getPosition().toLatLng(), ADDRESS_SEARCH_RADIUS_IN_METERS),
                null);

        searchLine = findViewById(R.id.searchInput);
        searchLine.setOnItemClickListener(mAutocompleteClickListener);
        searchLine.setAdapter(placeAutocompleteAdapter);

        searchLine.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || event.getAction() == KeyEvent.ACTION_DOWN
                    || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                provideGeoLocation();

            }

            return false;
        });
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = placeAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(googleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (in != null) {
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            }
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = places -> {
        if (!places.getStatus().isSuccess()) {
            places.release();
            return;
        }
        moveCamera(places.get(0).getLatLng(), DEFAULT_ZOOM_LEVEL);
        places.release();
    };

    private void provideGeoLocation() {
        String searchString = searchLine.getText().toString();
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressesList = new ArrayList<>();
        try {
            addressesList = geocoder.getFromLocationName(searchString, 1);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        if (addressesList.size() > 0) {
            Address address = addressesList.get(0);

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM_LEVEL);
        }
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
}
