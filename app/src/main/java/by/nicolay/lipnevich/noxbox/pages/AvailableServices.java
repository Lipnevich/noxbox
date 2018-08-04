package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;
import android.content.Intent;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.constructor.ConstructorActivity;
import by.nicolay.lipnevich.noxbox.detailed.DetailedActivity;
import by.nicolay.lipnevich.noxbox.model.Comment;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxTime;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Position;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.model.WorkSchedule;
import by.nicolay.lipnevich.noxbox.state.ProfileStorage;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.MarkerCreator;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class AvailableServices implements State, GoogleMap.OnMarkerClickListener {

    private Map<String, Marker> markers = new HashMap<>();
    private GoogleMap googleMap;
    private Activity activity;

    public AvailableServices(GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(Profile profile) {
        activity.findViewById(R.id.noxboxConstructorButton).setVisibility(View.VISIBLE);
        activity.findViewById(R.id.noxboxConstructorButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.startActivity(new Intent(activity, ConstructorActivity.class));
            }
        });

        final Noxbox noxbox = new Noxbox();
        noxbox.setTimeCreated(System.currentTimeMillis());
        noxbox.setRole(MarketRole.demand);
        Profile owner = new Profile().setId("1231").setTravelMode(TravelMode.none);
        Rating rating = new Rating().setReceivedLikes(2).setReceivedDislikes(1);

        rating.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный напарник!", System.currentTimeMillis(), true));
        rating.getComments().put("1", new Comment("1", "Добротный паренёк!", System.currentTimeMillis(), true));
        rating.getComments().put("2", new Comment("2", "Выносливость бы повысить, слишком быстро выдыхается во время кросса.", System.currentTimeMillis(), false));

        owner.getDemandsRating().put(NoxboxType.sportCompanion.name(), rating);
        noxbox.setOwner(owner);
        noxbox.setId("12311");
        noxbox.setEstimationTime("0");
        noxbox.setPrice("25");
        noxbox.setPosition(new Position().setLongitude(27.569018).setLatitude(53.871399));
        noxbox.setType(NoxboxType.sportCompanion);
        noxbox.setWorkSchedule(new WorkSchedule());
        createMarker(profile, noxbox);

        Noxbox noxbox1 = new Noxbox();
        noxbox1.setTimeCreated(System.currentTimeMillis());
        noxbox1.setRole(MarketRole.demand);
        Profile owner1 = new Profile().setId("1234").setTravelMode(TravelMode.driving);
        Rating rating1 = new Rating().setReceivedLikes(2).setReceivedDislikes(1);
        rating1.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный сантехник!", System.currentTimeMillis(), true));
        rating1.getComments().put("1", new Comment("1", "Добротный сантехник!", System.currentTimeMillis(), true));
        rating1.getComments().put("2", new Comment("2", "Опоздун!!", System.currentTimeMillis(), false));
        owner1.getDemandsRating().put(NoxboxType.plumber.name(), rating1);
        noxbox1.setOwner(owner1);
        noxbox1.setId("12312");
        noxbox1.setEstimationTime("500");
        noxbox1.setPrice("25");
        noxbox1.setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399));
        noxbox1.setType(NoxboxType.plumber);
        noxbox1.setWorkSchedule(new WorkSchedule());

        createMarker(profile, noxbox1);

        Noxbox noxbox2 = new Noxbox();
        noxbox2.setTimeCreated(System.currentTimeMillis());
        noxbox2.setRole(MarketRole.supply);

        Profile owner2 = new Profile().setId("1238").setTravelMode(TravelMode.walking);
        Rating rating2 = new Rating().setReceivedLikes(2).setReceivedDislikes(1);
        rating2.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный музыкант!", System.currentTimeMillis(), true));
        rating2.getComments().put("1", new Comment("1", "Духовная и творческая личность!", System.currentTimeMillis(), true));
        rating2.getComments().put("2", new Comment("2", "Безжалостный музыкант! Всю ночь у костра бренчал, да ещё и песни дьвольские не пел, а рычал!!!", System.currentTimeMillis(), false));
        owner2.getSuppliesRating().put(NoxboxType.musician.name(), rating2);
        noxbox2.setOwner(owner2);

        noxbox2.setId("12313");
        noxbox2.setEstimationTime("1600");
        noxbox2.setPrice("25");
        noxbox2.setPosition(new Position().setLongitude(27.609018).setLatitude(53.951399));
        noxbox2.setType(NoxboxType.musician);
        noxbox2.setWorkSchedule(new WorkSchedule(NoxboxTime._41, NoxboxTime._47));
        createMarker(profile, noxbox2);
        googleMap.setOnMarkerClickListener(this);

        if(profile.getPosition() != null){
            CameraPosition cameraPosition
                    = new CameraPosition.Builder()
                    .target(profile.getPosition().toLatLng())
                    .zoom(15)
                    .build();
            CameraUpdate cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition);
            googleMap.animateCamera(cameraUpdate);
        }

        //TODO тестовый нокс бокс
        profile.setCurrent(noxbox);
    }

    @Override
    public void clear() {
        googleMap.clear();
        activity.findViewById(R.id.noxboxConstructorButton).setVisibility(View.GONE);
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
    public boolean onMarkerClick(final Marker marker) {
        ProfileStorage.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.setViewed((Noxbox) marker.getTag());
                profile.setPosition(Position.from(googleMap.getCameraPosition().target));
                activity.startActivity(new Intent(activity, DetailedActivity.class));
            }
        });
        return false;
    }

}
