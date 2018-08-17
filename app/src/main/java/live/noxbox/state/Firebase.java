package live.noxbox.state;

import android.support.annotation.NonNull;

import com.firebase.geofire.GeoFire;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import live.noxbox.model.Filters;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.model.Request;
import live.noxbox.model.TravelMode;
import live.noxbox.tools.MessagingService;
import live.noxbox.tools.Task;

import static java.lang.System.currentTimeMillis;
import static java.lang.reflect.Modifier.isStatic;

public class Firebase {

    static {
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }

    private static DatabaseReference profiles() {
        return db().child("profiles");
    }

    private static DatabaseReference events() {
        return db().child("events");
    }

    private static DatabaseReference requests() {
        return db().child("requests");
    }

    private static GeoFire geo;
    public static GeoFire geo() {
        if(geo == null) geo = new GeoFire(db().child("geo"));
        return geo;
    }

    private static DatabaseReference db() {
        return FirebaseDatabase.getInstance().getReference();
    }

    static Profile profile;

    static Profile getProfile() {
        return profile;
    }

    public static void sendRequest(Request request) {
        requests().child(getProfile().getId()).child(request.getType().toString())
                .setValue(objectToMap(request.setId(getProfile().getId())
                        .setPush(MessagingService.generatePush(request))));
    }

    // Noxboxes API
    private static DatabaseReference currentNoxboxReference() {
        return profiles().child(getProfile().getId()).child("profile").child("current");
    }

    public static void updateCurrentNoxbox(@NonNull Noxbox noxbox) {
        currentNoxboxReference().updateChildren(objectToMap(noxbox));
        getProfile().setCurrent(noxbox);
    }

    public static void removeCurrentNoxbox() {
        currentNoxboxReference().removeValue();
        getProfile().setCurrent(null);
    }

    // History API
    public static Noxbox persistHistory() {
        Noxbox current = getProfile().getCurrent();
        if(current == null) return null;

        like();
        return persistHistory(current.setTimeCompleted(System.currentTimeMillis()));
    }

    public static Noxbox persistHistory(Noxbox noxbox) {
        history().child(noxbox.getId()).updateChildren(objectToMap(noxbox));
        return noxbox;
    }

    public static void loadHistory(final Task<Collection<Noxbox>> task) {
        history().limitToLast(15).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<Map<String, Noxbox>> genericTypeIndicator
                            = new GenericTypeIndicator<Map<String, Noxbox>>() {};
                    Map<String, Noxbox> history = dataSnapshot.getValue(genericTypeIndicator);
                    task.execute(history.values());
                }
            }

            @Override public void onCancelled(DatabaseError databaseError) {}
        });
    }

    private static DatabaseReference history() {
        return profiles().child(getProfile().getId()).child("history");
    }

    static private ValueEventListener listener;

    // Profiles API
    static void listenProfile(@NonNull final Task<Profile> task) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        listener = new ValueEventListener() {
            @Override public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profile = snapshot.getValue(Profile.class);
                    refreshNotificationToken();
                    task.execute(profile);
                } else {
                    // TODO (nli) delete it

                    Map<String,Boolean> filterTypesList = new HashMap<>();
                    for(NoxboxType type : NoxboxType.values()){
                        filterTypesList.put(type.name(), true);
                    }
                    profile = new Profile()
                            .setId(firebaseUser.getUid())
                            .setName(firebaseUser.getDisplayName())
                            .setHost(true)
                            .setPhoto(firebaseUser.getPhotoUrl().toString())
                            .setFilters(new Filters(true,true,"0",filterTypesList))
                            .setTravelMode(TravelMode.driving);
                    task.execute(profile);
                }
            }
            @Override public void onCancelled(@NonNull DatabaseError databaseError) {}
        };

        profiles().child(firebaseUser.getUid()).child("profile").addValueEventListener(listener);
    }

    public static void refreshNotificationToken() {
        String notificationToken = FirebaseInstanceId.getInstance().getToken();
        if(notificationToken == null || getProfile() == null) return;
        if(!notificationToken.equals(getProfile().getNotificationKeys().getAndroid())) {
            getProfile().getNotificationKeys().setAndroid(notificationToken);
            profiles().child(getProfile().getId()).child("notificationKeys")
                    .child("android").setValue(notificationToken);
        }
    }

    public static void updateProfile(Profile profile) {
        profiles().child(profile.getId()).child("profile").updateChildren(objectToMap(profile));
    }

    // in case of null value - does not override value
    private static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> params = new HashMap<>();
        Class clazz = object.getClass();
        while(clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(object);
                        if (value != null) {
                            params.put(field.getName(), value);
                        }
                    } catch (IllegalAccessException e) {
                    }
                }
            }
            clazz = clazz.getSuperclass();
        }
        return params;
    }

    public static void like() {
//        getProfile().getRating().setReceivedLikes(getProfile().getRating().getReceivedLikes() + 1);
//        getProfile().getRating().setSentLikes(getProfile().getRating().getSentLikes() + 1);
    }

    public static void dislikeNoxbox(String profileId, Noxbox noxbox) {
        noxbox.getNotMe(profileId).setTimeDisliked(currentTimeMillis());
        // TODO (nli) update history
    }
}
