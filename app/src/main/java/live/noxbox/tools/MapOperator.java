package live.noxbox.tools;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.activities.detailed.DetailedActivity;
import live.noxbox.cluster.NoxboxMarker;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;

import static java.util.Arrays.asList;
import static live.noxbox.Constants.DEFAULT_ZOOM_LEVEL;
import static live.noxbox.Constants.MAX_ZOOM_LEVEL;
import static live.noxbox.Constants.MIN_ZOOM_LEVEL;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.DayPartDeterminer.isItDayNow;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.Router.startActivity;
import static live.noxbox.tools.location.LocationOperator.getDeviceLocation;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;

public class MapOperator {

    public static void buildMapPosition(final GoogleMap googleMap, final Context context) {
        if (googleMap == null) return;
        switch (NoxboxState.getState(profile().getCurrent(), profile())) {
            case requesting:
            case accepting:
            case moving:
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(profile().getCurrent().getProfileWhoComes().getPosition().toLatLng());
                builder.include(profile().getCurrent().getPosition().toLatLng());
                LatLngBounds latLngBounds = builder.build();
                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                        latLngBounds,
                        context.getResources().getDisplayMetrics().widthPixels,
                        context.getResources().getDisplayMetrics().heightPixels,
                        dpToPx(68)));
                break;

            case created:
            case performing:
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile().getCurrent().getPosition().toLatLng(),
                        googleMap.getCameraPosition().zoom < DEFAULT_ZOOM_LEVEL ? DEFAULT_ZOOM_LEVEL : googleMap.getCameraPosition().zoom));
                break;
            default:
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile().getPosition().toLatLng(),
                        googleMap.getCameraPosition().zoom < DEFAULT_ZOOM_LEVEL ? DEFAULT_ZOOM_LEVEL : googleMap.getCameraPosition().zoom));
        }
    }

    private volatile static boolean wasMapPositionUpdated = false;
    public static void enterTheMap(GoogleMap googleMap, Activity activity) {
        if(wasMapPositionUpdated) return;

        if (isLocationPermissionGranted(activity.getApplicationContext())) {
            getDeviceLocation(profile(), googleMap, activity);
        } else {
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile().getPosition().toLatLng(), MIN_ZOOM_LEVEL));
        }

        wasMapPositionUpdated = true;
    }


    public static void setNoxboxMarkerListener(GoogleMap googleMap, final Profile profile, final Activity activity) {
        switch (NoxboxState.getState(profile.getCurrent(), profile)) {
            case created:
                googleMap.setOnMarkerClickListener(marker -> {
                    startActivity(activity, ContractActivity.class);
                    return true;
                });
                break;

            case requesting:
            case accepting:
                googleMap.setOnMarkerClickListener(marker -> {
                    profile.setViewed(profile().getCurrent());
                    startActivity(activity, DetailedActivity.class);
                    return true;
                });
                break;

            case moving:
                googleMap.setOnMarkerClickListener(marker -> {
                    profile.setViewed(profile().getCurrent());
                    startActivity(activity, DetailedActivity.class);
                    return true;
                });
                break;

            default:
                googleMap.setOnMarkerClickListener(marker -> true);
        }

    }

    public static void clearMapMarkerListener(GoogleMap googleMap) {
        googleMap.setOnMarkerClickListener(marker -> true);
    }

    public static void setupMap(Context context, GoogleMap googleMap) {

        if (isItDayNow()) {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map));
        } else {
            googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_in_night));
        }
        googleMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);
        googleMap.getUiSettings().setRotateGesturesEnabled(false);
        googleMap.getUiSettings().setTiltGesturesEnabled(false);
        googleMap.getUiSettings().setMapToolbarEnabled(false);
    }


    public static LatLng getCenterBetweenSomeLocations(List<NoxboxMarker> points) {
        double totalLatitude = 0;
        double totalLongitude = 0;

        for (NoxboxMarker point : points) {
            totalLatitude += point.getPosition().latitude;
            totalLongitude += point.getPosition().longitude;
        }

        double latitude = totalLatitude / points.size();
        double longitude = totalLongitude / points.size();

        return new LatLng(latitude, longitude);
    }

    private static Polyline polyline;

    public static void drawPath(Context context, GoogleMap googleMap, Profile profile) {
        LatLng start = profile.getCurrent().getProfileWhoComes().getPosition().toLatLng();
        LatLng end = profile.getCurrent().getPosition().toLatLng();

        if (polyline != null) {
            polyline.remove();
        }

        drawPath(context, googleMap, start, end);
    }

    //method for moving State only
    public static void drawPath(Context context, GoogleMap googleMap, Position currentNoxbox, Position profileWhoMoving) {
        LatLng start = profileWhoMoving.toLatLng();
        LatLng end = currentNoxbox.toLatLng();

        if (polyline != null) {
            polyline.remove();
        }

        drawPath(context, googleMap, start, end);
    }

    public static void drawPath(Context context, GoogleMap googleMap, LatLng start, LatLng end) {
        double cLat = ((start.latitude + end.latitude) / 2);
        double cLon = ((start.longitude + end.longitude) / 2);

        //add skew and arcHeight to move the midPoint
        if (Math.abs(start.longitude - end.longitude) < Math.abs(start.latitude - end.latitude)) {
            cLon -= start.longitude - end.longitude;
        } else {
            cLat += start.latitude - end.latitude;
        }

        ArrayList<LatLng> points = new ArrayList<LatLng>();
        double tDelta = 1.0 / 50;
        for (double t = 0; t <= 1.0; t += tDelta) {
            double oneMinusT = (1.0 - t);
            double t2 = Math.pow(t, 2);
            double lon = oneMinusT * oneMinusT * start.longitude
                    + 2 * oneMinusT * t * cLon
                    + t2 * end.longitude;
            double lat = oneMinusT * oneMinusT * start.latitude
                    + 2 * oneMinusT * t * cLat
                    + t2 * end.latitude;
            points.add(new LatLng(lat, lon));
        }

        PolylineOptions polylineOptions = new PolylineOptions()
                .width(24)
                .color(context.getResources().getColor(R.color.primary))
                .geodesic(true)
                .pattern(asList(new Dot(), new Gap(10)))
                .addAll(points);

        polyline = googleMap.addPolyline(polylineOptions);
    }

    public static Position getCameraPosition(GoogleMap googleMap) {
        if (googleMap == null) return null;
        LatLng latLng = googleMap.getCameraPosition().target;

        return Position.from(latLng);
    }

}
