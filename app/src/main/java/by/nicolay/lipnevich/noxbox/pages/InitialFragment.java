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
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxTime;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.model.WorkSchedule;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
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
    public void draw(Profile profile) {
        Noxbox noxbox = new Noxbox();
        noxbox.setRole(MarketRole.demand);
        noxbox.setOwner(new Profile()
                .setTravelMode(TravelMode.none)
                .setRating(new AllRates().setReceived(new Rating().setLikes(100L))));
        noxbox.getOwner().setId("1231");
        noxbox.setId("12311");
        noxbox.setEstimationTime("0");
        noxbox.setPrice("25");
        noxbox.setPosition(new Position().setLongitude(27.569018).setLatitude(53.871399));
        noxbox.setType(NoxboxType.sportCompanion);
        noxbox.setWorkSchedule(new WorkSchedule());
        createMarker(profile, noxbox);

        Noxbox noxbox1 = new Noxbox();
        noxbox1.setRole(MarketRole.demand);
        noxbox1.setOwner(new Profile()
                .setTravelMode(TravelMode.driving)
                .setRating(new AllRates().setReceived(new Rating().setLikes(89L))));
        noxbox1.getOwner().setId("1232");
        noxbox1.setId("12312");
        noxbox1.setEstimationTime("500");
        noxbox1.setPrice("25");
        noxbox1.setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399));
        noxbox1.setType(NoxboxType.plumber);
        noxbox1.setWorkSchedule(new WorkSchedule());
        createMarker(profile, noxbox1);

        Noxbox noxbox2 = new Noxbox();
        noxbox2.setRole(MarketRole.demand);
        noxbox2.setOwner(new Profile()
                .setTravelMode(TravelMode.walking)
                .setRating(new AllRates().setReceived(new Rating().setLikes(95L))));
        noxbox2.getOwner().setId("1233");
        noxbox2.setId("12313");
        noxbox2.setEstimationTime("1600");
        noxbox2.setPrice("25");
        noxbox2.setPosition(new Position().setLongitude(27.609018).setLatitude(53.951399));
        noxbox2.setType(NoxboxType.haircut);
        noxbox2.setWorkSchedule(new WorkSchedule(NoxboxTime._43,NoxboxTime._49));
        createMarker(profile, noxbox2);
        googleMap.setOnMarkerClickListener(this);
    }

    @Override
    public void clear() {
        googleMap.clear();
    }


    public void createMarker(Profile profile, Noxbox noxbox) {
        if (markers.get(noxbox.getId()) == null) {
            markers.put(noxbox.getId(), MarkerCreator.createCustomMarker(noxbox, profile, googleMap, activity));
        }
    }

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
