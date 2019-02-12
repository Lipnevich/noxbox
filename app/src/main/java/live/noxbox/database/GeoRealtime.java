package live.noxbox.database;

import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

import live.noxbox.Constants;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxTime;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Position;
import live.noxbox.model.Rating;
import live.noxbox.model.TravelMode;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.profile;

public class GeoRealtime {

    public final static String delimiter = ";";

    private static DatabaseReference realtime = FirebaseDatabase.getInstance().getReference();
    private static GeoFire geo;
    private static DatabaseReference positions;

    private static GeoFire geo() {
        if (geo == null)
            geo = new GeoFire(realtime.child("geo"));
        return geo;
    }

    private static DatabaseReference position(String noxboxId) {
        if (positions == null)
            positions = realtime.child("positions").child(noxboxId);
        return positions;
    }

    // supplier
    public static void online(Noxbox current) {
        geo().setLocation(createKey(current), current.getPosition().toGeoLocation());
    }

    public static void offline(Noxbox current) {
        if (current.getGeoId() != null) {
            geo().removeLocation(current.getGeoId());
        } else {
            geo().removeLocation(createKey(current));
        }
    }

    public static String createKey(Noxbox currentNoxbox) {
        Rating ownerRating = currentNoxbox.getRole() == MarketRole.supply ?
                currentNoxbox.getOwner().getSuppliesRating().get(currentNoxbox.getType().name()) :
                currentNoxbox.getOwner().getDemandsRating().get(currentNoxbox.getType().name());
        if (ownerRating == null) {
            ownerRating = new Rating();
        }

        return currentNoxbox.getId()
                + delimiter + currentNoxbox.getOwner().getId()
                + delimiter + currentNoxbox.getRole()
                + delimiter + currentNoxbox.getType()
                + delimiter + currentNoxbox.getPrice().replaceAll("\\.", ",")
                + delimiter + currentNoxbox.getOwner().getTravelMode()
                + delimiter + currentNoxbox.getOwner().getHost()
                + delimiter + currentNoxbox.getOwner().getFilters().getAllowNovices()
                + delimiter + ownerRating.getReceivedLikes()
                + delimiter + ownerRating.getReceivedDislikes()
                + delimiter + currentNoxbox.getWorkSchedule().getStartTime().name()
                + delimiter + currentNoxbox.getWorkSchedule().getEndTime().name();
    }

    private static Noxbox parseKey(String key) {
        String[] values = key.split(delimiter);
        int index = 0;
        try {
            if (values.length == 12) {
                Noxbox noxbox = new Noxbox();
                noxbox.setId(values[index++]);
                noxbox.getOwner().setId(values[index++]);
                noxbox.setRole(MarketRole.valueOf(values[index++]));
                noxbox.setType(NoxboxType.valueOf(values[index++]));
                noxbox.setPrice(values[index++]);
                noxbox.getOwner().setTravelMode(TravelMode.valueOf(values[index++]));
                noxbox.getOwner().setHost(Boolean.valueOf(values[index++]));
                noxbox.getOwner().getFilters().setAllowNovices(Boolean.valueOf(values[index++]));


                Rating rating = new Rating().setReceivedLikes(Integer.valueOf(values[index++])).setReceivedDislikes(Integer.valueOf(values[index++]));
                if (noxbox.getRole() == MarketRole.supply) {
                    noxbox.getOwner().getSuppliesRating().put(noxbox.getType().name(), rating);
                } else if (noxbox.getRole() == MarketRole.demand) {
                    noxbox.getOwner().getDemandsRating().put(noxbox.getType().name(), rating);
                }

                noxbox.getWorkSchedule().setStartTime(NoxboxTime.valueOf(values[index++]));
                noxbox.getWorkSchedule().setEndTime(NoxboxTime.valueOf(values[index++]));

                noxbox.setTimeCreated(0L);
                return noxbox;
            }
        } catch (IllegalArgumentException someOneRenamedEnum) {
            Crashlytics.logException(someOneRenamedEnum);
        }
        return null;
    }

    // consumer
    private static GeoQuery geoQuery;
    private static GeoLocation currentLocation = new GeoLocation(0, 0);
    private static final double MIN_STEP = 0.01;

    public static void startListenAvailableNoxboxes(GeoLocation geoLocation, final Map<String, Noxbox> noxboxes) {
        //allow to recreate query once per three seconds
        if (Math.abs(geoLocation.latitude - currentLocation.latitude) < MIN_STEP ||
                Math.abs(geoLocation.longitude - currentLocation.longitude) < MIN_STEP)
            return;
        currentLocation = geoLocation;

        if (geoQuery != null) {
            geoQuery.setCenter(geoLocation);
        } else {
            geoQuery = geo().queryAtLocation(geoLocation, Constants.RADIUS_IN_METERS / 1000);
            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                @Override
                public void onKeyEntered(String key, GeoLocation location) {
                    Noxbox noxbox = parseKey(key);
                    if (noxbox != null && !noxbox.getOwner().getId().equals(profile().getId())) {
                        noxbox.setPosition(Position.from(location));
                        noxbox.getOwner().setPosition(Position.from(location));
                        noxboxes.put(noxbox.getId(), noxbox);
                    }
                }

                @Override
                public void onKeyExited(String key) {
                    Noxbox noxbox = parseKey(key);
                    if (noxbox != null) {
                        noxboxes.remove(noxbox.getId());
                    }
                }

                @Override
                public void onKeyMoved(String key, GeoLocation location) {
                    onKeyEntered(key, location);
                }

                @Override
                public void onGeoQueryReady() {
                }

                @Override
                public void onGeoQueryError(DatabaseError error) {
                }
            });
        }
    }

    public static void stopListenAvailableNoxboxes() {
        currentLocation = new GeoLocation(0, 0);
        if (geoQuery != null) {
            geoQuery.removeAllListeners();
            geoQuery = null;
        }
    }

    private static ValueEventListener positionListener;
    public static void listenPosition(String noxboxId, Task<Position> task) {
        stopListenPosition(noxboxId);
        positionListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()) {
                    Position position = dataSnapshot.getValue(Position.class);
                    task.execute(position);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {}
        };
        position(noxboxId).addValueEventListener(positionListener);
    }

    public static void stopListenPosition(String noxboxId) {
        if(positionListener != null) {
            position(noxboxId).removeEventListener(positionListener);
            positionListener = null;
        }
    }

    public static void updatePosition(String noxboxId, Position position) {
        position(noxboxId).setValue(position);
    }

    public static void removePosition(String noxboxId) {
        position(noxboxId).removeValue();
    }
}
