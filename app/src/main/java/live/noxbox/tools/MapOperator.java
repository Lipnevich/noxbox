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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.activities.detailed.DetailedActivity;
import live.noxbox.cluster.NoxboxMarker;
import live.noxbox.database.AppCache;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;

import static live.noxbox.Constants.MAX_ZOOM_LEVEL;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.tools.Router.startActivity;

public class MapOperator {

    public static void buildMapPosition(final GoogleMap googleMap, final Context context) {
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(final Profile profile) {
                switch (NoxboxState.getState(profile.getCurrent(), profile)) {
                    case requesting:
                    case accepting:
                    case moving:
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(profile.getCurrent().getProfileWhoComes().getPosition().toLatLng());
                        builder.include(profile.getCurrent().getPosition().toLatLng());
                        LatLngBounds latLngBounds = builder.build();
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(
                                latLngBounds,
                                context.getResources().getDisplayMetrics().widthPixels,
                                context.getResources().getDisplayMetrics().heightPixels,
                                dpToPx(68)));
//                        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
//                                latLngBounds,
//                                context.getResources().getDisplayMetrics().widthPixels,
//                                context.getResources().getDisplayMetrics().heightPixels,
//                                dpToPx(68)));
                        break;

                    case created:
                    case performing:
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));
                        break;
                    default:
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(profile.getPosition().toLatLng(), 15));
                }
            }
        });
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
        googleMap.setMaxZoomPreference(MAX_ZOOM_LEVEL);
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

    private static Polyline polyline;
    public static void drawPath(Activity activity, GoogleMap googleMap, Profile profile) {
        LatLng start = profile.getCurrent().getProfileWhoComes().getPosition().toLatLng();
        LatLng end = profile.getCurrent().getPosition().toLatLng();

        double cLat = ((start.latitude + end.latitude) / 2);
        double cLon = ((start.longitude + end.longitude) / 2);

        //add skew and arcHeight to move the midPoint
        if (Math.abs(start.longitude - end.longitude) < 0.0001) {
            cLon -= 0.0195;
        } else {
            cLat += 0.0195;
        }

        // TODO (nli) исправить для малых дистанций
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
                .width(9)
                .color(activity.getResources().getColor(R.color.primary))
                .geodesic(true)
                .addAll(points);
        List<PatternItem> pattern = Arrays.asList(
                new Dot(), new Gap(10));

        if (polyline != null)
            polyline.remove();

        polyline = googleMap.addPolyline(polylineOptions);
        polyline.setPattern(pattern);
        polyline.setGeodesic(true);
    }

}
