package live.noxbox.tools;

import android.app.Activity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import live.noxbox.detailed.DetailedActivity;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;

import static live.noxbox.MapActivity.dpToPx;
import static live.noxbox.tools.Router.startActivity;

public class MapController {

    public static void buildMapPosition(GoogleMap googleMap, Profile profile) {

        //created
        if (profile.getCurrent().getTimeCreated() != null
                && profile.getCurrent().getTimeRequested() == null
                && profile.getCurrent().getTimeAccepted() == null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));
            return;
        }

        //requesting and accepting
        if (profile.getCurrent().getTimeRequested() != null
                && profile.getCurrent().getTimeAccepted() == null
                && profile.getCurrent().getTimeCanceledByParty() == null
                && profile.getCurrent().getTimeCanceledByOwner() == null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    new LatLngBounds.Builder()
                            .include(profile.getPosition().toLatLng())
                            .include(profile.getCurrent().getPosition().toLatLng())
                            .build(), dpToPx(68)));
            return;
        }

        //moving
        if (profile.getCurrent().getTimeAccepted() != null
                && profile.getCurrent().getTimeRequested() != null
                && (profile.getCurrent().getTimeOwnerVerified() == null || profile.getCurrent().getTimePartyVerified() == null)) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    new LatLngBounds.Builder()
                            .include(profile.getPosition().toLatLng())
                            .include(profile.getCurrent().getPosition().toLatLng())
                            .build(), dpToPx(68)));
            return;
        }

        //performing
        if (profile.getCurrent().getTimePartyVerified() != null &&
                profile.getCurrent().getTimeOwnerVerified() != null &&
                profile.getCurrent().getTimeCompleted() == null) {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getCurrent().getPosition().toLatLng(), 15));
            return;
        }

        //other
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(profile.getPosition().toLatLng(), 15));
    }


    public static void buildMapMarkerListener(GoogleMap googleMap, final Profile profile, final Activity activity) {

        //created
        if (profile.getCurrent().getTimeCreated() != null
                && profile.getCurrent().getTimeRequested() == null
                && profile.getCurrent().getTimeAccepted() == null) {
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    profile.setViewed((Noxbox) marker.getTag());
                    startActivity(activity, DetailedActivity.class);
                    return true;
                }
            });
            return;
        }


        //requesting and accepting
        if (profile.getCurrent().getTimeRequested() != null
                && profile.getCurrent().getTimeAccepted() == null
                && profile.getCurrent().getTimeCanceledByParty() == null
                && profile.getCurrent().getTimeCanceledByOwner() == null) {
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    profile.setViewed((Noxbox) marker.getTag());
                    startActivity(activity, DetailedActivity.class);
                    return true;
                }
            });
            return;
        }


        //moving
        if (profile.getCurrent().getTimeAccepted() != null
                && profile.getCurrent().getTimeRequested() != null
                && (profile.getCurrent().getTimeOwnerVerified() == null || profile.getCurrent().getTimePartyVerified() == null)) {
            googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    profile.setViewed((Noxbox) marker.getTag());
                    startActivity(activity, DetailedActivity.class);
                    return true;
                }
            });
            return;
        }

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) { return true; }});
    }
}
