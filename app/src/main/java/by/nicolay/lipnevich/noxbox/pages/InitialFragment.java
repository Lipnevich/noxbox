package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.AllRates;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.MarkerCreator;
import by.nicolay.lipnevich.noxbox.tools.TimeFormatter;

public class InitialFragment implements Fragment, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, GoogleMap.InfoWindowAdapter {

    private Map<String, Marker> markers = new HashMap<>();
    private GoogleMap googleMap;
    private Activity activity;

    public InitialFragment(GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
        this.googleMap.setInfoWindowAdapter(this);
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
        noxbox.setEstimationTime("0");
        noxbox.setPrice("25");
        noxbox.setPosition(new Position().setLongitude(27.569018).setLatitude(53.871399));
        noxbox.setType(NoxboxType.sportCompanion);
        createMarker(noxbox);

        Noxbox noxbox1 = new Noxbox();
        noxbox1.setPerformer(new Profile()
                .setTravelMode(TravelMode.driving)
                .setRating(new AllRates().setReceived(new Rating().setLikes(89L))));
        noxbox1.getPerformer().setId("1232");
        noxbox1.setId("12312");
        noxbox1.setPayer(Firebase.getProfile());
        noxbox1.setEstimationTime("500");
        noxbox1.setPrice("25");
        noxbox1.setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399));
        noxbox1.setType(NoxboxType.plumber);
        createMarker(noxbox1);

        Noxbox noxbox2 = new Noxbox();
        noxbox2.setPerformer(new Profile()
                .setTravelMode(TravelMode.walking)
                .setRating(new AllRates().setReceived(new Rating().setLikes(95L))));
        noxbox2.getPerformer().setId("1233");
        noxbox2.setId("12313");
        noxbox2.setPayer(Firebase.getProfile());
        noxbox2.setEstimationTime("1600");
        noxbox2.setPrice("25");
        noxbox2.setPosition(new Position().setLongitude(27.609018).setLatitude(53.951399));
        noxbox2.setType(NoxboxType.haircut);
        createMarker(noxbox2);
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

    @Override
    public View getInfoWindow(Marker marker) {
        return null;
    }

    @Override
    public View getInfoContents(Marker marker) {
        View infoLayout = null;
        Noxbox noxbox = null;
        try {
            noxbox = (Noxbox) marker.getTag();
            infoLayout = activity.getLayoutInflater().inflate(R.layout.custom_info_window, null);
            ((ImageView)infoLayout.findViewById(R.id.typeImage)).setBackground(activity.getDrawable(noxbox.getType().getImage()));
            ((TextView)infoLayout.findViewById(R.id.typeText)).setText(noxbox.getType().getName());
            ((TextView)infoLayout.findViewById(R.id.moneyText)).setText(noxbox.getPrice());
            ((TextView)infoLayout.findViewById(R.id.timeText)).setText(TimeFormatter.getTime(noxbox.getEstimationTime()));

        } catch (Exception ex) {
            DebugMessage.popup(activity, "inflate error!");
        }

        return infoLayout;
    }
}
