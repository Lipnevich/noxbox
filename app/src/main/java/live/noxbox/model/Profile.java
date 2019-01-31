package live.noxbox.model;

import android.support.annotation.NonNull;

import com.google.common.base.Objects;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static live.noxbox.Constants.MIN_RATE_IN_PERCENTAGE;

public class Profile implements Serializable {

    // read only
    private String id;

    private Acceptance acceptance = new Acceptance();
    private Map<String, Boolean> darkList = new HashMap<>();
    private Map<String, Rating> suppliesRating = new HashMap<>();
    private Map<String, Rating> demandsRating = new HashMap<>();
    private Map<String, Portfolio> portfolio = new HashMap<>();
    private Wallet wallet;

    // writable
    private String name;
    private String photo;
    private NotificationKeys notificationKeys;
    private String noxboxId;
    @Virtual
    private Noxbox current;
    @Virtual
    private Noxbox viewed;
    private TravelMode travelMode;
    private Boolean host;

    private Position position;
    private Long arriveInSeconds;

    private Filters filters;

    public Filters getFilters() {
        if (filters == null) {
            filters = new Filters();
        }
        return filters;
    }

    public Profile setFilters(Filters filters) {
        this.filters = filters;
        return this;
    }

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
        if(name == null) name = "";
        return name;
    }

    public Profile setName(String name) {
        this.name = name;
        return this;
    }

    public Position getPosition() {
        if(position == null){
            return new Position().setLatitude(0.0).setLongitude(0.0);
        }
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
        if (travelMode == null) {
            travelMode = TravelMode.walking;
        }
        return travelMode;
    }

    public Profile setTravelMode(TravelMode travelMode) {
        this.travelMode = travelMode;
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
        if (wallet == null) {
            wallet = new Wallet();
        }
        return wallet;
    }

    public Profile setWallet(Wallet wallet) {
        this.wallet = wallet;
        return this;
    }

    @NonNull
    public NotificationKeys getNotificationKeys() {
        if (notificationKeys == null) {
            notificationKeys = new NotificationKeys();
        }
        return notificationKeys;
    }

    public Profile setNotificationKeys(NotificationKeys notificationKeys) {
        this.notificationKeys = notificationKeys;
        return this;
    }

    public Profile publicInfo() {
        return new Profile().setId(id)
                .setPosition(position)
                .setSuppliesRating(suppliesRating)
                .setDemandsRating(demandsRating)
                .setTravelMode(travelMode)
                .setHost(host);
    }

    public Profile privateInfo() {
        return publicInfo().setWallet(wallet).setName(name).setPhoto(photo);
    }

    public Noxbox getViewed() {
        if (viewed == null) {
            viewed = new Noxbox();
        }
        return viewed;
    }

    public Profile setViewed(Noxbox viewed) {
        this.viewed = viewed;
        return this;
    }

    public Map<String, Rating> getDemandsRating() {
        if (demandsRating == null) {
            demandsRating = new HashMap<>();
        }
        return demandsRating;
    }

    public Profile setDemandsRating(Map<String, Rating> demandsRating) {
        this.demandsRating = demandsRating;
        return this;
    }

    public Map<String, Rating> getSuppliesRating() {
        if (suppliesRating == null) {
            suppliesRating = new HashMap<>();
        }
        return suppliesRating;
    }

    public Profile setSuppliesRating(Map<String, Rating> suppliesRating) {
        this.suppliesRating = suppliesRating;
        return this;
    }

    public Map<String, Portfolio> getPortfolio() {
        return portfolio;
    }

    public Profile setPortfolio(Map<String, Portfolio> portfolio) {
        this.portfolio = portfolio;
        return this;
    }

    public int ratingToPercentage(MarketRole role, NoxboxType type) {
        int likes = 0;
        int dislikes = 0;
        Rating rating = null;

        if (role == MarketRole.demand) {
            rating = demandsRating.get(type.name());
        } else {
            rating = suppliesRating.get(type.name());
        }

        if(rating == null){
            rating = new Rating();
        }

        likes = rating.getReceivedLikes();
        dislikes = rating.getReceivedDislikes();
        if (dislikes == 0) return 100;
        return (likes * 100 / (likes + dislikes));

    }
    public static int ratingToPercentage(int likes, int dislikes) {
        if (dislikes == 0) return 100;
        return (likes * 100 / (likes + dislikes));

    }

    public int ratingToPercentage() {
        int likes = 0;
        int dislikes = 0;
        for (Rating offer : suppliesRating.values()) {
            likes += offer.getReceivedLikes();
            dislikes += offer.getReceivedDislikes();
        }
        for (Rating demand : demandsRating.values()) {
            likes += demand.getReceivedLikes();
            dislikes += demand.getReceivedDislikes();
        }

        if (likes == 0 && dislikes == 0) return 100;
        if (likes < 10 && dislikes == 1) return MIN_RATE_IN_PERCENTAGE;
        if (likes == 0 && dislikes > 1) return 0;

        return (likes * 100 / (likes + dislikes));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        return Objects.equal(id, profile.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public Boolean getHost() {
        if (host == null) {
            host = false;
        }
        return host;
    }

    public Profile setHost(Boolean host) {
        this.host = host;
        return this;
    }

    public Map<String, Boolean> getDarkList() {
        if (darkList == null) {
            darkList = new HashMap<>();
        }
        return darkList;
    }

    public Profile setDarkList(Map<String, Boolean> darkList) {
        this.darkList = darkList;
        return this;
    }

    public String getNoxboxId() {
        return noxboxId;
    }

    public Profile setNoxboxId(String noxboxId) {
        this.noxboxId = noxboxId;
        return this;
    }

}
