package live.noxbox.tools;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import live.noxbox.model.Noxbox;
import live.noxbox.model.TravelMode;

public class MarkerCreator {
    public static Marker createPositionMarker(TravelMode travelMode, LatLng position, GoogleMap googleMap) {
        return googleMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(travelMode.getImage()))
                .anchor(0.5f, 1f));
    }

    public static Marker createCustomMarker(Noxbox noxbox, GoogleMap googleMap) {
        MarkerOptions markerOptions = new MarkerOptions()
                .position(noxbox.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromResource(noxbox.getType().getImage()))
                .anchor(0.5f, 1f);

        Marker marker = googleMap.addMarker(markerOptions);

        marker.setTag(noxbox);

        return marker;
    }

    public static MarkerOptions createCustomMarker(Noxbox noxbox) {
        return new MarkerOptions()
                .position(noxbox.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromResource(noxbox.getType().getImage()))
                .anchor(0.5f, 1f);
    }

}
