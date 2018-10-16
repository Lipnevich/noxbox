package live.noxbox.state;

import com.firebase.geofire.GeoFire;
import com.google.firebase.database.FirebaseDatabase;

import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Rating;
import live.noxbox.model.Request;
import live.noxbox.model.TravelMode;

public class Firebase {

    public final static String delimiter = ";";

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private static GeoFire geo;

    private static GeoFire geo() {
        if (geo == null) geo = new GeoFire(FirebaseDatabase.getInstance().getReference().child("geo"));
        return geo;
    }

    public static void online(Noxbox current) {
        geo().setLocation(createKey(current), current.getPosition().toGeoLocation());
    }

    public static void offline(Noxbox current) {
        if (current.getGeoId() != null) {
            geo().removeLocation(current.getGeoId());
        }
        geo().removeLocation(createKey(current));
    }

    public static String createKey(Noxbox currentNoxbox) {
        Rating ownerRating = currentNoxbox.getRole() == MarketRole.supply ?
                currentNoxbox.getOwner().getSuppliesRating().get(currentNoxbox.getType().name()) :
                currentNoxbox.getOwner().getDemandsRating().get(currentNoxbox.getType().name());
        if(ownerRating == null) {
            ownerRating = new Rating();
        }

        return currentNoxbox.getId()
                + delimiter + currentNoxbox.getRole()
                + delimiter + currentNoxbox.getType()
                + delimiter + currentNoxbox.getPrice()
                + delimiter + currentNoxbox.getOwner().getTravelMode()
                + delimiter + ownerRating.getReceivedLikes()
                + delimiter + ownerRating.getReceivedDislikes()
                ;
    }

    private static Noxbox parseKey(String key) {
        String [] values = key.split(delimiter);
        Noxbox noxbox = new Noxbox();
        if(values.length >= 7) {
            noxbox.setId(values[0]);
            noxbox.setRole(MarketRole.valueOf(values[1]));
            noxbox.setType(NoxboxType.valueOf(values[2]));
            noxbox.setPrice(values[3]);
            noxbox.getOwner().setTravelMode(TravelMode.valueOf(values[4]));

            Rating rating = new Rating().setReceivedLikes(Integer.valueOf(values[5])).setReceivedDislikes(Integer.valueOf(values[6]));
            if(noxbox.getRole() == MarketRole.supply) {
                noxbox.getOwner().getSuppliesRating().put(noxbox.getType().name(), rating);
            } else if (noxbox.getRole() == MarketRole.demand) {
                noxbox.getOwner().getDemandsRating().put(noxbox.getType().name(), rating);
            }
        }
        return noxbox;
    }

    public static void sendRequest(Request request) {
//        requests().child(getProfile().getId()).child(request.getType().toString())
//                .setValue(objectToMap(request.setId(getProfile().getId())
//                        .setPush(MessagingService.generatePush(request))));
    }
}
