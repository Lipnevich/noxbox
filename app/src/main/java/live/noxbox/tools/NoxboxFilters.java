package live.noxbox.tools;

import java.math.BigDecimal;

import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.model.TravelMode;

import static live.noxbox.Constants.NOVICE_LIKES;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.MarketRole.demand;
import static live.noxbox.model.MarketRole.supply;

public class NoxboxFilters {

    public static boolean isFiltered(Profile profile, Noxbox noxbox) {

        Boolean shouldDrawType = profile.getFilters().getTypes().get(noxbox.getType().name());

        //if (BuildConfig.DEBUG) return false;

        if (shouldDrawType != null && !shouldDrawType)
            return true;

        if (noxbox.getRole() == MarketRole.demand && !profile.getFilters().getDemand())
            return true;

        if (noxbox.getRole() == supply && !profile.getFilters().getSupply())
            return true;

        if (noxbox.getOwner().getId().equals(profile().getId()))
            return true;

//        int hour = LocalDateTime.now().getHourOfDay();
//        if (noxbox.getWorkSchedule().getStartTime().getHourOfDay() < hour
//                && noxbox.getWorkSchedule().getEndTime().getHourOfDay() > hour)
//            return true;


        //фильтры по типу передвижения
        if (profile.getTravelMode() == TravelMode.none && noxbox.getOwner().getTravelMode() == TravelMode.none)
            return true;

        Rating ownerRating = noxbox.getRole()
                == demand
                ? noxbox.getOwner().getDemandsRating().get(noxbox.getType().name())
                : noxbox.getOwner().getSuppliesRating().get(noxbox.getType().name());
        if(ownerRating == null) {
            ownerRating = new Rating();
        }
        if (!profile.getFilters().getAllowNovices() && ownerRating.getReceivedLikes() < NOVICE_LIKES)
            return true;

        Rating myRating = noxbox.getRole()
                == supply
                ? profile.getDemandsRating().get(noxbox.getType().name())
                : profile.getSuppliesRating().get(noxbox.getType().name());
        if(myRating == null) {
            myRating = new Rating();
        }

        if (!noxbox.getOwner().getFilters().getAllowNovices() && myRating.getReceivedLikes() < NOVICE_LIKES)
            return true;

        if (profile.getDarkList().get(noxbox.getOwner().getId()) != null)
            return true;

        try {
            if (new BigDecimal(noxbox.getPrice()).intValue() > profile.getFilters().getPrice())
                return true;
        } catch (NumberFormatException e) {
            return true;
        }
        return false;
    }


    //TODO переиспользовать логику для контракта
}
