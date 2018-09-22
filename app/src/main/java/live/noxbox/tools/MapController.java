package live.noxbox.tools;

import android.app.Activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import live.noxbox.constructor.ConstructorActivity;
import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;

import static live.noxbox.MapActivity.dpToPx;
import static live.noxbox.tools.Router.startActivity;

public class MapController {

    public static void buildMapPosition(GoogleMap googleMap, Profile profile) {
        switch (NoxboxState.getState(profile.getCurrent(), profile)) {

            case requesting:
            case accepting:
            case moving:
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                        new LatLngBounds.Builder()
                                .include(profile.getPosition().toLatLng())
                                .include(profile.getCurrent().getPosition().toLatLng())
                                .build(), dpToPx(68)));
                break;

            case created:
            case performing:
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));
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
                        startActivity(activity, ConstructorActivity.class);
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
}
