package live.noxbox.activities.detailed;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.Configuration.ADDRESS_SEARCH_RADIUS_IN_METERS;
import static live.noxbox.tools.MapOperator.setupMap;

public class CoordinateActivity extends BaseActivity implements OnMapReadyCallback, GoogleApiClient.OnConnectionFailedListener {
    public static final String LAT = "lat";
    public static final String LNG = "lng";
    public static final int COORDINATE = 111;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    private GoogleMap googleMap;
    private AutoCompleteTextView searchLine;
    private PlaceAutocompleteAdapter mPlaceAutocompleteAdapter;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coordinate);
        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMapAsync(this);
    }


    private void geoLocate() {
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

            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), 15);
        }
    }


    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        setupMap(this, googleMap);
        this.googleMap.getUiSettings().setMyLocationButtonEnabled(true);
        this.googleMap.setMyLocationEnabled(true);
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (profile.getViewed().getPosition() != null) {
                    moveCamera(profile.getViewed().getPosition().toLatLng(), 15);
                } else {
                    moveCamera(profile.getCurrent().getPosition().toLatLng(), 15);
                }
                init(profile);
            }
        });


        findViewById(R.id.choosePlace).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                final LatLng latLng = googleMap.getCameraPosition().target;
                if (latLng == null) {
                    Crashlytics.log(Log.ERROR, this.getClass().getSimpleName(), "Empty camera position on google maps");
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(LAT, latLng.latitude);
                intent.putExtra(LNG, latLng.longitude);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
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
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();

        mPlaceAutocompleteAdapter = new PlaceAutocompleteAdapter(
                this,
                mGoogleApiClient,
                getAreaByRadiusAroundPoint(profile.getPosition().toLatLng(), ADDRESS_SEARCH_RADIUS_IN_METERS),
                null);

        searchLine = findViewById(R.id.searchInput);
        searchLine.setOnItemClickListener(mAutocompleteClickListener);
        searchLine.setAdapter(mPlaceAutocompleteAdapter);

        searchLine.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || event.getAction() == KeyEvent.ACTION_DOWN
                        || event.getAction() == KeyEvent.KEYCODE_ENTER) {
                    geoLocate();

                }

                return false;
            }
        });
    }

    private AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final AutocompletePrediction item = mPlaceAutocompleteAdapter.getItem(position);
            final String placeId = item.getPlaceId();
            PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId);
            placeResult.setResultCallback(mUpdatePlaceDetailsCallback);
            InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (in != null) {
                in.hideSoftInputFromWindow(view.getApplicationWindowToken(), 0);
            }
        }
    };

    private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
        @Override
        public void onResult(@NonNull PlaceBuffer places) {
            if (!places.getStatus().isSuccess()) {
                places.release();
                return;
            }
            moveCamera(places.get(0).getLatLng(), 15);
            places.release();
        }
    };

    private void moveCamera(LatLng latLng, float zoom) {
        CameraPosition cameraPosition
                = new CameraPosition.Builder()
                .target(latLng)
                .zoom(zoom)
                .build();
        CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
        googleMap.moveCamera(cameraUpdate);
        hideSoftKeyboard();
    }

    private void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
