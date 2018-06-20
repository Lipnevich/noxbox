package by.nicolay.lipnevich.noxbox.pages;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Position;

public class InitialFragment implements Fragment, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private Map<String, GroundOverlay> markers = new HashMap<>();
    private GoogleMap googleMap;

    public InitialFragment(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    @Override
    public void draw() {
        Noxbox noxbox = new Noxbox();
        noxbox.setPosition(new Position().setLongitude(27.34).setLatitude(53.52));
        noxbox.setType(NoxboxType.sportCompanion);
        createMarker(noxbox);
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void clear() {
        googleMap.clear();
    }


    public void createMarker(Noxbox noxbox) {
        if (markers.get(noxbox.getId()) == null) {
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(noxbox.getType().name())
                    .snippet("Description for noxbox type")
                    .position(noxbox.getPosition().toLatLng())
                    .icon(BitmapDescriptorFactory.fromResource(noxbox.getType().getImage()))
                    .anchor(0.5f, 1f));
            marker.setTag(noxbox);
            // markers.put(noxbox.getId(), ...);
        }


       /* GroundOverlay marker = markers.get(key);
        if (marker == null) {
            GroundOverlayOptions newarkMap = new GroundOverlayOptions()
                    .image(BitmapDescriptorFactory.fromResource(resource))
                    .position(latLng, 48, 48)
                    .anchor(0.5f, 1)
                    .zIndex(10);
            marker = googleMap.addGroundOverlay(newarkMap);
            marker.setDimensions(getScaledSize(), getScaledSize());
            markers.put(key, marker);
        }
        marker.setPosition(latLng);
        return marker;*/
    }

    public void removeMarker(String key) {
        GroundOverlay marker = markers.remove(key);
        if (marker != null) {
            marker.remove();
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Noxbox noxbox = (Noxbox) marker.getTag();
        //popup(this, noxbox.getType().name());
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Noxbox noxbox = (Noxbox) marker.getTag();
        //popup(this, noxbox.getType().name());
    }
}
