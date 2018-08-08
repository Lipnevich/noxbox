package live.noxbox.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static live.noxbox.Configuration.MIN_RATE_IN_PERCENTAGE;

public class Profile implements Serializable {

    // read only
    private String id;
    private Map<String, Rating> suppliesRating = new HashMap<>();
    private Map<String, Rating> demandsRating = new HashMap<>();
    private Acceptance acceptance;
    private Wallet wallet;
    private Long timeDisliked;

    // writable
    private String name;
    private String photo;
    private NotificationKeys notificationKeys;
    private Noxbox current;
    private TravelMode travelMode;

    private Position position;
    private Noxbox viewed;
    private Long arriveInSeconds;


    public String getId() {
        return id;
    }

    public Profile setId(String id) {
        this.id = id;
        return this;
    }

    public String getPhoto() {
        return photo;
    }

    public Profile setPhoto(String photo) {
        this.photo = photo;
        return this;
    }

    public String getName() {
        return name;
    }

    public Profile setName(String name) {
        this.name = name;
        return this;
    }

    public Position getPosition() {
        return position;
    }

    public Profile setPosition(Position position) {
        this.position = position;
        return this;
    }

    public Long getArriveInSeconds() {
        return arriveInSeconds;
    }

    public Profile setArriveInSeconds(Long arriveInSeconds) {
        this.arriveInSeconds = arriveInSeconds;
        return this;
    }

    public TravelMode getTravelMode() {
        return travelMode;
    }

    public Profile setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
        return this;
    }

    public Long getTimeDisliked() {
        return timeDisliked;
    }

    public Profile setTimeDisliked(Long timeDisliked) {
        this.timeDisliked = timeDisliked;
        return this;
    }

    public Acceptance getAcceptance() {
        return acceptance;
    }

    public Profile setAcceptance(Acceptance acceptance) {
        this.acceptance = acceptance;
        return this;
    }

    public Noxbox getCurrent() {
        return current;
    }

    public Profile setCurrent(Noxbox current) {
        this.current = current;
        return this;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public Profile setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public NotificationKeys getNotificationKeys() {
        if(notificationKeys == null) {
            notificationKeys = new NotificationKeys();
        }
        return notificationKeys;
    }

    public Profile setNotificationKeys(NotificationKeys notificationKeys) {
        this.notificationKeys = notificationKeys;
        return this;
    }

    @Exclude
    public Profile publicInfo() {
        return new Profile().setId(id)
                .setPosition(position).setSuppliesRating(suppliesRating).setDemandsRating(demandsRating).setTravelMode(travelMode);
    }

    @Exclude
    public Profile notPublicInfo(){
        return new Profile().setId(id).setName(name).setPhoto(photo)
                .setPosition(position).setSuppliesRating(suppliesRating).setDemandsRating(demandsRating).setTravelMode(travelMode);
    }

    public Noxbox getViewed() {
        return viewed;
    }

    public Profile setViewed(Noxbox viewed) {
        this.viewed = viewed;
        return this;
    }

    public Map<String, Rating> getDemandsRating() {
        return demandsRating;
    }

    public Profile setDemandsRating(Map<String, Rating> demandsRating) {
        this.demandsRating = demandsRating;
        return this;
    }

    public Map<String, Rating> getSuppliesRating() {
        return suppliesRating;
    }

    public Profile setSuppliesRating(Map<String, Rating> suppliesRating) {
        this.suppliesRating = suppliesRating;
        return this;
    }



    @Exclude
    public int ratingToPercentage() {
        int likes = 0;
        int dislikes = 0;
        for(Rating offer : suppliesRating.values()) {
            likes += offer.getReceivedLikes();
            dislikes += offer.getReceivedDislikes();
        }
        for(Rating demand : demandsRating.values()) {
            likes += demand.getReceivedLikes();
            dislikes += demand.getReceivedDislikes();
        }

        if (likes == 0 && dislikes == 0) return 100;
        if (likes < 10 && dislikes == 1) return MIN_RATE_IN_PERCENTAGE;
        if (likes == 0 && dislikes > 1) return 0;

        return (likes / (likes + dislikes)) * 100;
    }
}
