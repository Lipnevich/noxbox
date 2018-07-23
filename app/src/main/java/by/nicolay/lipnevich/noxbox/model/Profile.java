package by.nicolay.lipnevich.noxbox.model;

import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Profile implements Serializable {

    private String id, name, email, photo, secret;
    private Acceptance acceptance;
    private Rating rating;
    private Wallet wallet;
    private NotificationKeys notificationKeys;

    private Noxbox current;
    private Noxbox viewed;

    private Position position;
    private Long timeDisliked;
    private TravelMode travelMode;
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

    @Exclude
    public Rating getRating() {
        return rating;
    }

    @Exclude
    public Profile setRating(Rating rating) {
        this.rating = rating;
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

    public String getEmail() {
        return email;
    }

    public Profile setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
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
        return new Profile().setId(id).setName(name).setPhoto(photo).setEmail(email)
                .setPosition(position).setRating(rating).setTravelMode(travelMode);
    }

    public Noxbox getViewed() {
        return viewed;
    }

    public Profile setViewed(Noxbox viewed) {
        this.viewed = viewed;
        return this;
    }
}
