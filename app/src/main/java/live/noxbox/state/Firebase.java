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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import live.noxbox.model.Comment;
import live.noxbox.model.Filters;
import live.noxbox.model.ImageType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Portfolio;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
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
        if (geo == null) geo = new GeoFire(db().child("geo"));
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
        if (current == null) return null;

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
                            = new GenericTypeIndicator<Map<String, Noxbox>>() {
                    };
                    Map<String, Noxbox> history = dataSnapshot.getValue(genericTypeIndicator);
                    task.execute(history.values());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
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
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    profile = snapshot.getValue(Profile.class);
                    refreshNotificationToken();
                    task.execute(profile);
                } else {
                    // TODO (nli) delete it
                    Rating rating = new Rating();
                    rating.setReceivedLikes(ThreadLocalRandom.current().nextInt(900, 1000));
                    rating.setReceivedDislikes(ThreadLocalRandom.current().nextInt(rating.getReceivedLikes() / 10));
                    rating.getComments().put("0", new Comment("0", "Очень занятный молодой человек, и годный напарник!", System.currentTimeMillis(), true));
                    rating.getComments().put("1", new Comment("1", "Добротный паренёк!", System.currentTimeMillis(), true));
                    rating.getComments().put("2", new Comment("2", "Выносливость бы повысить, слишком быстро выдыхается во время кросса.", System.currentTimeMillis(), false));

                    Map<String, Rating> ratingList = new HashMap<>();
                    Map<String, Boolean> filterTypesList = new HashMap<>();
                    for (NoxboxType type : NoxboxType.values()) {
                        filterTypesList.put(type.name(), true);
                        ratingList.put(type.name(),rating);
                    }


                    List<String> certificatesList = new ArrayList<>();
                    certificatesList.add("https://i.pinimg.com/736x/1d/ba/a1/1dbaa1fb5b2f64e54010cf6aae72b8b1.jpg");
                    certificatesList.add("http://4u-professional.com/assets/images/sert/gel-lak.jpg");
                    certificatesList.add("https://www.hallyuuk.com/wp-content/uploads/2018/06/reiki-master-certificate-template-inspirational-reiki-certificate-templates-idealstalist-of-reiki-master-certificate-template.jpg");
                    certificatesList.add("http://www.childminder.ng/blog_pics/1479134810.jpg");

                    List<String> workSampleList = new ArrayList<>();
                    workSampleList.add("http://coolmanicure.com/media/k2/items/cache/stilnyy_manikur_so_strazami_XL.jpg");
                    workSampleList.add("http://rosdesign.com/design_materials3/img_materials3/kopf/kopf1.jpg");
                    workSampleList.add("http://vmirevolos.ru/wp-content/uploads/2015/12/61.jpg");




                    Map<String, List<String>> images = new HashMap<>();
                    images.put(ImageType.samples.name(), new ArrayList<String>(workSampleList));
                    images.put(ImageType.certificates.name(), new ArrayList<String>(certificatesList));


                    Map<String, Portfolio> portfolioMap = new HashMap<>();
                    portfolioMap.put(NoxboxType.haircut.name(), new Portfolio(new HashMap<String, List<String>>(images)));
                    portfolioMap.put(NoxboxType.manicure.name(), new Portfolio(new HashMap<String, List<String>>(images)));

                    profile = new Profile()
                            .setId(firebaseUser.getUid())
                            .setName(firebaseUser.getDisplayName())
                            .setHost(true)
                            .setPhoto(firebaseUser.getPhotoUrl().toString())
                            .setDemandsRating(ratingList)
                            .setSuppliesRating(ratingList)
                            .setFilters(new Filters(true, true, "0", filterTypesList))
                            .setTravelMode(TravelMode.driving)
                            .setPortfolio(portfolioMap);

                    task.execute(profile);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        };

        profiles().child(firebaseUser.getUid()).child("profile").addValueEventListener(listener);
    }

    public static void refreshNotificationToken() {
        String notificationToken = FirebaseInstanceId.getInstance().getToken();
        if (notificationToken == null || getProfile() == null) return;
        if (!notificationToken.equals(getProfile().getNotificationKeys().getAndroid())) {
            getProfile().getNotificationKeys().setAndroid(notificationToken);
            profiles().child(getProfile().getId()).child("notificationKeys")
                    .child("android").setValue(notificationToken);
        }
    }

    public static void updateProfile(Profile profile) {
        profiles().child(profile.getId()).child("profile").updateChildren(objectToMap(profile));
    }

    // in case of null value - does not override value
    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> params = new HashMap<>();
        Class clazz = object.getClass();
        while (clazz != Object.class) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!isStatic(field.getModifiers())) {
                    field.setAccessible(true);
                    try {
                        Object value = field.get(object);
                        if (value != null) {
                            if(value instanceof Map) {

                                Map<String, Object> stringMap = new HashMap<>();
                                Map map = (Map)value;
                                Iterator<Map.Entry> iterator = map.entrySet().iterator();
                                while (iterator.hasNext()) {
                                    Map.Entry entry = iterator.next();
                                    stringMap.put(entry.getKey().toString(), entry.getValue());
                                }
                                params.put(field.getName(), stringMap);
                            } else {
                                params.put(field.getName(), value);
                            }
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
