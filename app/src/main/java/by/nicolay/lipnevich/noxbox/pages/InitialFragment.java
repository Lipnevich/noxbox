package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;
import android.content.Intent;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.detailed.DetailedNoxboxPage;
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
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.MarkerCreator;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class InitialFragment implements Fragment, GoogleMap.OnMarkerClickListener {

    private Map<String, Marker> markers = new HashMap<>();
    private GoogleMap googleMap;
    private Activity activity;

    public InitialFragment(GoogleMap googleMap, final Activity activity) {
        this.googleMap = googleMap;
        this.activity = activity;
    }

    @Override
    public void draw(Profile profile) {
        Noxbox noxbox = new Noxbox();
        noxbox.setRole(MarketRole.demand);
        noxbox.setOwner(new Profile()
                .setTravelMode(TravelMode.none)
                .setRating(new Rating().setReceivedLikes(100)));
        noxbox.getOwner().setId("1231");
        noxbox.setId("12311");
        noxbox.setEstimationTime("0");
        noxbox.setPrice("25");
        noxbox.setPosition(new Position().setLongitude(27.569018).setLatitude(53.871399));
        noxbox.setType(NoxboxType.sportCompanion);
        noxbox.setWorkSchedule(new WorkSchedule());
        noxbox.getOwner().getRating().getComments().put("0",new Comment("0","Очень занятный молодой человек, и годный напарник!",System.currentTimeMillis(),true));
        noxbox.getOwner().getRating().getComments().put("1",new Comment("1","Добротный паренёк!",System.currentTimeMillis(),true));
        noxbox.getOwner().getRating().getComments().put("2",new Comment("2","Выносливость бы повысить, слишком быстро выдыхается во время кросса.",System.currentTimeMillis(),false));
        createMarker(profile, noxbox);

        Noxbox noxbox1 = new Noxbox();
        noxbox1.setRole(MarketRole.demand);
        noxbox1.setOwner(new Profile()
                .setTravelMode(TravelMode.driving)
                .setRating(new Rating().setReceivedLikes(100).setReceivedDislikes(3)));
        noxbox1.getOwner().setId("1232");
        noxbox1.setId("12312");
        noxbox1.setEstimationTime("500");
        noxbox1.setPrice("25");
        noxbox1.setPosition(new Position().setLongitude(27.609018).setLatitude(53.901399));
        noxbox1.setType(NoxboxType.plumber);
        noxbox1.setWorkSchedule(new WorkSchedule());
        noxbox1.getOwner().getRating().getComments().put("0",new Comment("0","Очень занятный молодой человек, и годный сантехник!",System.currentTimeMillis(),true));
        noxbox1.getOwner().getRating().getComments().put("1",new Comment("1","Добротный сантехник!",System.currentTimeMillis(),true));
        noxbox1.getOwner().getRating().getComments().put("2",new Comment("2","Опоздун!!",System.currentTimeMillis(),false));
        createMarker(profile, noxbox1);

        Noxbox noxbox2 = new Noxbox();
        noxbox2.setRole(MarketRole.demand);
        noxbox2.setOwner(new Profile()
                .setTravelMode(TravelMode.walking)
                .setRating(new Rating().setReceivedLikes(100).setReceivedDislikes(9)));
        noxbox2.getOwner().setId("1233");
        noxbox2.setId("12313");
        noxbox2.setEstimationTime("1600");
        noxbox2.setPrice("25");
        noxbox2.setPosition(new Position().setLongitude(27.609018).setLatitude(53.951399));
        noxbox2.setType(NoxboxType.haircut);
        noxbox2.setWorkSchedule(new WorkSchedule(NoxboxTime._43,NoxboxTime._49));
        noxbox2.getOwner().getRating().getComments().put("0",new Comment("0","Очень занятный молодой человек, и годный парикмахер!",System.currentTimeMillis(),true));
        noxbox2.getOwner().getRating().getComments().put("1",new Comment("1","Добротный парикмахер!",System.currentTimeMillis(),true));
        noxbox2.getOwner().getRating().getComments().put("2",new Comment("2","Слишком высокий! пришлось стоять во время стрижки...",System.currentTimeMillis(),false));
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
    public boolean onMarkerClick(final Marker marker) {
        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.setViewed((Noxbox)marker.getTag());
                activity.startActivity(new Intent(activity, DetailedNoxboxPage.class));
            }
        });
        return false;
    }
}
