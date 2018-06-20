package by.nicolay.lipnevich.noxbox.pages;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Position;

public class InitFragment {

    private Map<String, GroundOverlay> markers = new HashMap<>();
    private GoogleMap googleMap;

    public InitFragment(GoogleMap googleMap) {
        this.googleMap = googleMap;
    }

    public void draw() {
        Noxbox noxbox = new Noxbox().setPosition(new Position().setLatitude(53.2).setLongitude(47.1)).setId();
        createMarker(noxbox);
    }
    public void clear(){
        googleMap.clear();
    }


    public void createMarker(Noxbox noxbox) {
      googleMap.addMarker(new MarkerOptions());
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
}
