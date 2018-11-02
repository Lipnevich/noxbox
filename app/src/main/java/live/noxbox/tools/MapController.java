package live.noxbox.tools;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import java.util.List;

import live.noxbox.R;
import live.noxbox.contract.ContractActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.state.cluster.NoxboxMarker;

import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.Router.startActivity;

public class MapController {

    public static void buildMapPosition(GoogleMap googleMap, Profile profile, Context context) {
        switch (NoxboxState.getState(profile.getCurrent(), profile)) {
            case requesting:
            case accepting:
            case moving:
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(profile.getPosition().toLatLng());
                builder.include(profile.getCurrent().getPosition().toLatLng());
                LatLngBounds latLngBounds = builder.build();
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        latLngBounds,
                        context.getResources().getDisplayMetrics().widthPixels,
                        context.getResources().getDisplayMetrics().heightPixels,
                        dpToPx(68)));
                break;

            case created:
            case performing:
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));
                break;
            default:
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getPosition().toLatLng(), 15));
        }


    }


    public static void buildMapMarkerListener(GoogleMap googleMap, final Profile profile, final Activity activity) {
        switch (NoxboxState.getState(profile.getCurrent(), profile)) {
            case created:
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        startActivity(activity, ContractActivity.class);
                        return true;
                    }
                });
                break;

            case requesting:
            case accepting:
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        profile.setViewed((Noxbox) marker.getTag());
                        startActivity(activity, DetailedActivity.class);
                        return true;
                    }
                });
                break;

            case moving:
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        profile.setViewed((Noxbox) marker.getTag());
                        startActivity(activity, DetailedActivity.class);
                        return true;
                    }
                });
                break;

            default:
                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        return true;
                    }
                });
        }

    }

    public static void clearMapMarkerListener(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                return true;
            }
        });
    }

    public static void moveCopyrightRight(GoogleMap googleMap) {
        googleMap.setPadding(dpToPx(84), 0, 0, dpToPx(8));
    }

    public static void moveCopyrightLeft(GoogleMap googleMap) {
        googleMap.setPadding(dpToPx(8), 0, 0, dpToPx(8));
    }

    public static void setupMap(Context context, GoogleMap googleMap) {
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_in_night));
        googleMap.setMaxZoomPreference(18);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }


    public static LatLng getCenterBetweenSomeLocations(List<NoxboxMarker> points){
        double totalLatitude = 0;
        double totalLongitude = 0;

        for (NoxboxMarker point : points) {
            totalLatitude += point.getPosition().latitude;
            totalLongitude += point.getPosition().longitude;
        }

        double latitude = totalLatitude / points.size();
        double longitude = totalLongitude / points.size();

        return new LatLng(latitude,longitude);
    }

}
