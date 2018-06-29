package by.nicolay.lipnevich.noxbox.model;

import java.io.Serializable;

public class UserAccount implements Serializable {

    private String id;
    private Profile profile;
    private Wallet wallet;
    private AllRates rating;
    private NotificationKeys notificationKeys;

    public String getId() {
        return id;
    }

    public UserAccount setId(String id) {
        this.id = id;
        return this;
    }

    public Profile getProfile() {
        return profile;
    }

    public UserAccount setProfile(Profile profile) {
        this.profile = profile;
        return this;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public UserAccount setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    public AllRates getRating() {
        return rating;
    }

    public UserAccount setRating(AllRates rating) {
        this.rating = rating;
        return this;
    }

    public NotificationKeys getNotificationKeys() {
        return notificationKeys;
    }

    public void setNotificationKeys(NotificationKeys notificationKeys) {
        this.notificationKeys = notificationKeys;
    }


}
