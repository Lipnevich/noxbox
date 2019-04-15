package live.noxbox.model;

import java.util.HashMap;
import java.util.Map;

import static live.noxbox.Constants.MIN_RATE_IN_PERCENTAGE;

/**
 * Created by Vladislaw Kravchenok on 12.04.2019.
 */
public class ProfileRatings {
    //key is Profile.id
    private Map<String, Boolean> darkList = new HashMap<>();
    //key is NoxboxType.name
    private Map<String, Rating> suppliesRating = new HashMap<>();
    private Map<String, Rating> demandsRating = new HashMap<>();

    public Map<String, Rating> getDemandsRating() {
        if (demandsRating == null) {
            demandsRating = new HashMap<>();
        }
        return demandsRating;
    }

    public ProfileRatings setDemandsRating(Map<String, Rating> demandsRating) {
        this.demandsRating = demandsRating;
        return this;
    }

    public Map<String, Rating> getSuppliesRating() {
        if (suppliesRating == null) {
            suppliesRating = new HashMap<>();
        }
        return suppliesRating;
    }

    public ProfileRatings setSuppliesRating(Map<String, Rating> suppliesRating) {
        this.suppliesRating = suppliesRating;
        return this;
    }

    public Map<String, Boolean> getDarkList() {
        if (darkList == null) {
            darkList = new HashMap<>();
        }
        return darkList;
    }

    public ProfileRatings setDarkList(Map<String, Boolean> darkList) {
        this.darkList = darkList;
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

        if (rating == null) {
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

    public Integer ratingToPercentage() {
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

}
