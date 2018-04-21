package by.nicolay.lipnevich.noxbox.model;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.Exclude;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.tools.TravelMode;

public class Profile implements Serializable {

    private String id, name, email, photo, secret;
    private Position position;
    private String addressToRefund;
    private Long timeDisliked;
    private TravelMode travelMode;

    private AllRates rating;

    // performer
    private Map<String, Noxbox> noxboxesForPerformer = new HashMap<>();
    private Long arriveInSeconds;

    // payer
    private Map<String, Noxbox> noxboxesForPayer = new HashMap<>();

    // communication during iteration
    private List<String> messages = new ArrayList<>();

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

    public Map<String, Noxbox> getNoxboxesForPerformer() {
        return noxboxesForPerformer;
    }

    public Profile setNoxboxesForPerformer(Map<String, Noxbox> noxboxesForPerformer) {
        this.noxboxesForPerformer = noxboxesForPerformer;
        return this;
    }

    public Long getArriveInSeconds() {
        return arriveInSeconds;
    }

    public Profile setArriveInSeconds(Long arriveInSeconds) {
        this.arriveInSeconds = arriveInSeconds;
        return this;
    }

    public Map<String, Noxbox> getNoxboxesForPayer() {
        return noxboxesForPayer;
    }

    public Profile setNoxboxesForPayer(Map<String, Noxbox> noxboxesForPayer) {
        this.noxboxesForPayer = noxboxesForPayer;
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

    public List<String> getMessages() {
        return messages;
    }

    public Profile setMessages(List<String> messages) {
        this.messages = messages;
        return this;
    }

    public String getAddressToRefund() {
        return addressToRefund;
    }

    public Profile setAddressToRefund(String addressToRefund) {
        this.addressToRefund = addressToRefund;
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
                .setName(user.getDisplayName())
                .setTravelMode(TravelMode.driving)
                .setPhoto(user.getPhotoUrl() != null ? user.getPhotoUrl().toString() : null);
    }

}
