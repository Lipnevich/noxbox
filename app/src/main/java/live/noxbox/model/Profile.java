package live.noxbox.model;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseUser;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Profile implements Serializable {


    private String id;
    private String referral;

    private Acceptance acceptance;
    @Virtual
    private ProfileRatings ratings = new ProfileRatings();
    private Map<String, Portfolio> portfolio = new HashMap<>();
    private Wallet wallet;

    private String name = "";
    private String photo = "";
    private String noxboxId = "";
    @Virtual
    private Noxbox current;
    @Virtual
    private Noxbox viewed;
    @Virtual
    private Noxbox contract;
    private TravelMode travelMode;
    private Boolean host;

    private Position position;
    private Long arriveInSeconds;

    private Filters filters;

    public Profile() {

    }

    public Profile(FirebaseUser user) {
        setId(Strings.nullToEmpty(user.getUid()))
                .setTravelMode(TravelMode.walking)
                .setPhoto(Strings.nullToEmpty(user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString()))
                .setName(Strings.nullToEmpty(user.getDisplayName()));
    }

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
        if (name == null) name = "";
        return name;
    }

    public Profile setName(String name) {
        this.name = name;
        return this;
    }

    public Position getPosition() {
        if (position == null) {
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
        if (acceptance == null) {
            acceptance = new Acceptance();
        }
        return acceptance;
    }

    public Profile setAcceptance(Acceptance acceptance) {
        this.acceptance = acceptance;
        return this;
    }

    public Noxbox getCurrent() {
        if (current == null) {
            current = new Noxbox();
        }
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

    public Profile publicInfo(MarketRole role, NoxboxType type) {
        Map<String, Rating> ratings = new HashMap<>();

        Rating rating = role == MarketRole.supply ? getRatings().getSuppliesRating().get(type.name()) : getRatings().getDemandsRating().get(type.name());
        if (rating == null) {
            rating = new Rating();
        }
        ratings.put(type.name(), rating);


        Map<String, Portfolio> servicePortfolio = new HashMap<>();
        if (role == MarketRole.supply && portfolio.get(type.name()) != null) {
            servicePortfolio.put(type.name(), portfolio.get(type.name()));
        }
        Profile profile = new Profile().setId(getId())
                .setPortfolio(servicePortfolio)
                .setPosition(getPosition())
                .setTravelMode(getTravelMode())
                .setHost(getHost());
        profile.getRatings().setSuppliesRating(role == MarketRole.supply ? ratings : null);
        profile.getRatings().setDemandsRating(role == MarketRole.demand ? ratings : null);
        return profile;
    }

    public Profile addPrivateInfo(Profile profile) {
        return this.setWallet(profile.getWallet()).setName(profile.getName()).setPhoto(profile.getPhoto()).setReferral(profile.getReferral());
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


    public Map<String, Portfolio> getPortfolio() {
        return portfolio;
    }

    public Profile setPortfolio(Map<String, Portfolio> portfolio) {
        this.portfolio = portfolio;
        return this;
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


    public String getNoxboxId() {
        if (noxboxId == null) {
            noxboxId = "";
        }
        return noxboxId;
    }

    public Profile setNoxboxId(String noxboxId) {
        this.noxboxId = noxboxId;
        return this;
    }

    public Noxbox getContract() {
        if (contract == null) {
            contract = new Noxbox();
        }
        return contract;
    }

    public Profile setContract(Noxbox contract) {
        this.contract = contract;
        return this;
    }

    public Profile init(FirebaseUser user) {
        if (user == null || !Strings.isNullOrEmpty(getId())) return null;
        setId(Strings.nullToEmpty(user.getUid()))
                .setPhoto(Strings.nullToEmpty(user.getPhotoUrl() == null ? "" : user.getPhotoUrl().toString()))
                .setName(Strings.nullToEmpty(user.getDisplayName()));

        return this;
    }

    public Profile copy(Profile from) {
        if (from == null) return null;
        id = from.id;
        acceptance = from.acceptance;
        portfolio = from.portfolio;
        wallet = from.wallet;
        name = from.name;
        photo = from.photo;
        noxboxId = from.noxboxId;
        travelMode = from.travelMode;
        host = from.host;
        position = from.position;
        arriveInSeconds = from.arriveInSeconds;
        filters = from.filters;
        return this;
    }

    @Override
    public String toString() {
        return "id=" + getId() + ",noxboxId=" + getNoxboxId();
    }

    public String getReferral() {
        if (referral == null) {
            referral = "";
        }
        return referral;
    }

    public Profile setReferral(String referral) {
        this.referral = referral;
        return this;
    }

    public ProfileRatings getRatings() {
        if (ratings == null) {
            ratings = new ProfileRatings();
        }
        return ratings;
    }

    public Profile setRatings(ProfileRatings ratings) {
        this.ratings = ratings;
        return this;
    }
}
