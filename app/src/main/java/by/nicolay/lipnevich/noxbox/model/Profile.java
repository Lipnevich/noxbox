package by.nicolay.lipnevich.noxbox.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.io.Serializable;

public class Profile implements Serializable {

    private String id, name, email, photo, secret;
    private Acceptance acceptance;
    private Position position;
    private String addressToRefund, estimationTime;
    private Long timeDisliked;
    private TravelMode travelMode;
    private AllRates rating;

    private Noxbox current;
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
    public AllRates getRating() {
        return rating;
    }

    @Exclude
    public Profile setRating(AllRates rating) {
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

    public String getAddressToRefund() {
        return addressToRefund;
    }

    public Profile setAddressToRefund(String addressToRefund) {
        this.addressToRefund = addressToRefund;
        return this;
    }

    public String getEstimationTime() {
        return estimationTime;
    }

    public Profile setEstimationTime(String estimationTime) {
        this.estimationTime = estimationTime;
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

    @Exclude
    public Profile publicInfo() {
        return new Profile().setId(id).setName(name).setPhoto(photo).setEmail(email)
                .setPosition(position).setTravelMode(travelMode);
    }

    @Exclude
    public static Profile createFrom(FirebaseUser user) {
        return new Profile()
                .setId(user.getUid())
                .setEmail(user.getEmail())
                .setAcceptance(new Acceptance(user))
                .setName(user.getDisplayName())
                .setTravelMode(TravelMode.driving)
                .setPhoto(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
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
}
