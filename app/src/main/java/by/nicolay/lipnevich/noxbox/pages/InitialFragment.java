package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.AllRates;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.MarkerCreator;

public class InitialFragment implements Fragment, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private Map<String, Marker> markers = new HashMap<>();
    private GoogleMap googleMap;
    private Activity activity;

    public InitialFragment(GoogleMap googleMap, Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw() {
        Noxbox noxbox = new Noxbox();
        noxbox.setPerformer(new Profile()
                .setTravelMode(TravelMode.none)
                .setRating(new AllRates().setReceived(new Rating().setLikes(100L))));
        noxbox.getPerformer().setId("1231");
        noxbox.setId("12311");
        noxbox.setPayer(Firebase.getProfile());
        noxbox.setEstimationTime("500");
        noxbox.setPrice("25");
        noxbox.setPosition(new Position().setLongitude(27.569018).setLatitude(53.871399));
        noxbox.setType(NoxboxType.sportCompanion);
        createMarker(noxbox);
        googleMap.setOnMarkerClickListener(this);
    }


    @Override
    public void clear() {
        googleMap.clear();
    }


    public void createMarker(Noxbox noxbox) {
        Profile profile = Firebase.getProfile();
        if (markers.get(noxbox.getId()) == null) {
            markers.put(noxbox.getId(), MarkerCreator.createCustomMarker(noxbox, profile, googleMap, activity));
        }
    }


       /* CircleOptions circleOptions = new CircleOptions()
                    .center(noxbox.getPosition().toLatLng())
                    .radius(50000)
                    .fillColor(Color.BLUE);
            if(false){
                circleOptions.strokeColor(Color.RED);
            }else if(false){
                circleOptions.strokeColor(Color.YELLOW);
            }else{
                circleOptions.strokeColor(Color.GREEN);
            }
            googleMap.addCircle(circleOptions);*/


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


    public void removeMarker(String key) {
        Marker marker = markers.remove(key);
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
