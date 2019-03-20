package live.noxbox.database;

import android.support.annotation.NonNull;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import live.noxbox.BuildConfig;
import live.noxbox.Constants;
import live.noxbox.model.Comment;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.model.Virtual;
import live.noxbox.tools.Task;

import static java.lang.reflect.Modifier.isStatic;
import static live.noxbox.database.AppCache.fireProfile;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.Noxbox.isNullOrZero;

public class Firestore {

    public static long reads = 0;
    public static long writes = 0;

    private static FirebaseFirestore db() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().build());
        return db;
    }

    private static DocumentReference profileReference(String userId) {
        return db().collection("profiles")
                .document(userId);
    }

    private static DocumentReference noxboxReference(String noxboxId) {
        return db().collection("noxboxes").document(noxboxId);
    }

    // Current human_profile
    private static ListenerRegistration profileListener;

    public static void stopListenProfile() {
        if (profileListener != null) {
            profileListener.remove();
            profileListener = null;
        }
    }

    public static void listenProfile(@NonNull final Task<Profile> task) {
        stopListenProfile();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        profileListener = profileReference(user.getUid()).addSnapshotListener((snapshot, e) -> {
            reads++;
            if (snapshot != null && snapshot.exists()) {
                Profile profile = snapshot.toObject(Profile.class);
                task.execute(profile);
            } else {
                Profile profile = new Profile(user);
                task.execute(profile);
            }
        });
    }

    public static void writeProfile(final Profile profile, Task<Profile> onSuccess) {
        if (profile.getId() == null) return;
        profileReference(profile.getId()).set(objectToMap(profile), SetOptions.merge())
                .addOnCompleteListener(onComplete -> {
                    System.out.println(onComplete);
                })
                .addOnSuccessListener(aVoid -> {
                    writes++;
                    onSuccess.execute(profile);
                })
                .addOnFailureListener(o -> {
                    if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                        return;
                    }

                    if (o instanceof FirebaseFirestoreException) {
                        if (((FirebaseFirestoreException) o).getCode() == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                            Crashlytics.log(Log.ERROR, "ProfileWriteDenied", profile.toString());
                        }
                    }
                    Crashlytics.logException(o);
                });
    }

    // Current noxbox
    private static ListenerRegistration noxboxListener;

    public static void stopListenNoxbox() {
        if (noxboxListener != null) {
            noxboxListener.remove();
        }
    }

    public static void listenNoxbox(@NonNull String noxboxId,
                                    @NonNull final Task<Noxbox> success,
                                    final Task<Exception> failure) {
        stopListenNoxbox();

        noxboxListener = noxboxReference(noxboxId).addSnapshotListener((snapshot, e) -> {
            reads++;
            if (e != null) {
                Crashlytics.logException(e);
                failure.execute(e);
                return;
            }

            if (snapshot != null && snapshot.exists()) {
                Noxbox current = snapshot.toObject(Noxbox.class);
                success.execute(current);
            }
        });
    }

    public static String getNewNoxboxId() {
        return db().collection("noxboxes").document().getId();
    }

    public static void updateNoxbox(Noxbox current, Task<String> onSuccess, Task<Exception> onFailure) {
        if (current.getRole() == MarketRole.supply) {
            current.setPerformerId(current.getOwner().getId());
            current.setPayerId(current.getParty() == null ? "" : current.getParty().getId());
        } else if (current.getRole() == MarketRole.demand) {
            current.setPerformerId(current.getParty() == null ? "" : current.getParty().getId());
            current.setPayerId(current.getOwner().getId());
        }

        writeNoxbox(current, onSuccess, onFailure);

        if (!isNullOrZero(current.getTimeCompleted())) {
            GeoRealtime.offline(current);
        }
    }

    public static void writeNoxbox(Noxbox noxbox, Task<String> onSuccess, Task<Exception> onFailure) {
        noxbox.setFinished(isFinished(noxbox));

        String currentId = noxbox.getId();
        noxboxReference(currentId).set(objectToMap(noxbox), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    writes++;
                    onSuccess.execute(currentId);
                })
                .addOnFailureListener(e -> {
                    Crashlytics.log(Log.ERROR, "NoxboxWriteDenied", noxbox.toString());
                    Crashlytics.logException(e);
                    onFailure.execute(e);
                });
    }

    public static boolean isFinished(Noxbox noxbox) {
        return noxbox.getTimeCanceledByOwner() > 0
                || noxbox.getTimeCanceledByParty() > 0
                || noxbox.getTimeOwnerRejected() > 0
                || noxbox.getTimePartyRejected() > 0
                || noxbox.getTimeCompleted() > 0
                || noxbox.getTimeRemoved() > 0
                || noxbox.getTimeTimeout() > 0;
    }

    public static void readNoxbox(String noxboxId, final Task<Noxbox> task) {
        noxboxReference(noxboxId).get().addOnCompleteListener(completion -> {
            reads++;
            if (!completion.isSuccessful()) return;
            DocumentSnapshot snapshot = completion.getResult();
            if (snapshot != null && snapshot.exists()) {
                Noxbox noxbox = snapshot.toObject(Noxbox.class);
                task.execute(noxbox);
            }
        });
    }

    public static void readHistory(long start, int count, MarketRole role, final Task<Collection<Noxbox>> task) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        noxboxes(start, count, role, user.getUid())
                .get().addOnCompleteListener(result -> {
            reads++;
            Collection<Noxbox> noxboxes = new ArrayList<>();
            if (result.isSuccessful() && result.getResult() != null) {
                for (QueryDocumentSnapshot document : result.getResult()) {
                    noxboxes.add(document.toObject(Noxbox.class));
                }
            }
            task.execute(noxboxes);
        });
    }

    private static Query noxboxes(long start, int count, MarketRole role, String userId) {
        return db().collection("noxboxes")
                .whereEqualTo(role.getOwnerFieldId(), userId)
                .whereGreaterThan("timeCompleted", 0)
                .orderBy("timeCompleted", Query.Direction.DESCENDING)
                .startAt(start)
                .limit(count);
    }

    // in case of null value - does not override value
    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> params = new HashMap<>();
        Class clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!isStatic(field.getModifiers()) && field.getAnnotation(Virtual.class) == null) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    if (value != null) {
                        if (value.getClass().isEnum()) {
                            params.put(field.getName(), value.toString());
                        } else if (value.getClass().getPackage().getName().startsWith(BuildConfig.APPLICATION_ID)) {
                            // write all application objects
                            params.put(field.getName(), objectToMap(value));
                        } else if (value instanceof Map) {
                            Map mapValue = (Map) value;
                            if (!mapValue.isEmpty()
                                    && !mapValue.values().iterator().next().getClass().getPackage().getName().startsWith(BuildConfig.APPLICATION_ID)) {
                                params.put(field.getName(), mapValue);
                            } else {
                                Map<String, Object> map = new HashMap<>();
                                for (Object entry : ((Map) value).entrySet()) {
                                    Map.Entry newEntry = (Map.Entry) entry;
                                    params.put(field.getName(), map);
                                    map.put(newEntry.getKey().toString(), objectToMap(newEntry.getValue()));
                                }
                            }
                        } else {
                            params.put(field.getName(), value);
                        }
                    }
                } catch (IllegalAccessException ignored) {
                }
            }
        }
        return params;
    }

    public static void ratingsUpdate() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        if(profile().getRatingUpdateTime() + Constants.RATINGS_UPDATE_MIN_FREQUENCY >
                System.currentTimeMillis()) {
            return;
        }

        ratingsUpdate(MarketRole.demand);
        ratingsUpdate(MarketRole.supply);
    }


    private static void ratingsUpdate(MarketRole role) {
        ratings(role, profile().getId(), profile().getRatingUpdateTime())
                .get().addOnCompleteListener(result -> {
            reads++;
            Collection<Noxbox> noxboxes = new ArrayList<>();
            if (result.isSuccessful() && result.getResult() != null) {
                for (QueryDocumentSnapshot document : result.getResult()) {
                    noxboxes.add(document.toObject(Noxbox.class));
                }
            } else {
                return;
            }
            Set<String> profiles = new HashSet<>();
            for(Noxbox noxbox : noxboxes) {
                boolean isUniqueProfile = profiles.add(noxbox.getNotMe(profile().getId()).getId());
                if(!isUniqueProfile) {
                    continue;
                }
                Rating rating = (role == MarketRole.demand
                        ? profile().getDemandsRating() : profile().getSuppliesRating())
                        .get(noxbox.getType());
                boolean isOwner = noxbox.getOwner().equals(profile());
                boolean isRatingUpdated = !noxbox.getTimeCompleted().equals(noxbox.getTimeRatingUpdated());

                if(isOwner) {
                    if(noxbox.getTimeCanceledByOwner() > 0) {
                        rating.setCanceled(rating.getCanceled() + 1);
                        continue;
                    }
                    if(noxbox.getTimeTimeout() > 0) {
                        rating.setNotResponded(rating.getNotResponded() + 1);
                        continue;
                    }
                    if(noxbox.getTimePartyRejected() > 0) {
                        rating.setNotVerified(rating.getNotVerified() + 1);
                        continue;
                    }
                    if(noxbox.getTimeRatingUpdated() == 0) {
                        continue;
                    }

                    if(noxbox.getTimeOwnerDisliked() > 0) {
                        rating.setSentDislikes(rating.getSentDislikes() + 1);
                        if(isRatingUpdated) {
                            rating.setSentLikes(rating.getSentLikes() - 1);
                        }
                    } else {
                        rating.setSentLikes(rating.getSentLikes() + 1);
                        if(isRatingUpdated) {
                            rating.setSentDislikes(rating.getSentDislikes() - 1);
                        }
                    }

                    Comment comment = new Comment().setTime(noxbox.getTimeCompleted());
                    if(noxbox.getTimePartyDisliked() > 0) {
                        comment.setLike(false);
                        rating.setReceivedDislikes(rating.getReceivedDislikes() + 1);
                        if(isRatingUpdated) {
                            rating.setReceivedLikes(rating.getReceivedLikes() - 1);
                        }
                    } else {
                        comment.setLike(true);
                        rating.setReceivedLikes(rating.getReceivedLikes() + 1);
                        if(isRatingUpdated) {
                            rating.setReceivedDislikes(rating.getReceivedDislikes() - 1);
                        }
                    }

                    if(!Strings.isNullOrEmpty(noxbox.getPartyComment())) {
                        comment.setText(noxbox.getPartyComment());
                        rating.getComments().put(noxbox.getParty().getId(), comment);
                    }
                } else {
                    if(noxbox.getTimeCanceledByParty() > 0) {
                        rating.setCanceled(rating.getCanceled() + 1);
                        continue;
                    }
                    if(noxbox.getTimeOwnerRejected() > 0) {
                        rating.setNotVerified(rating.getNotVerified() + 1);
                        continue;
                    }
                    if(noxbox.getTimeRatingUpdated() == 0) {
                        continue;
                    }

                    if(noxbox.getTimeOwnerDisliked() > 0) {
                        rating.setReceivedDislikes(rating.getReceivedDislikes() + 1);
                        if(isRatingUpdated) {
                            rating.setReceivedLikes(rating.getReceivedLikes() - 1);
                        }
                    } else {
                        rating.setReceivedLikes(rating.getReceivedLikes() + 1);
                        if(isRatingUpdated) {
                            rating.setReceivedDislikes(rating.getReceivedDislikes() - 1);
                        }
                    }

                    Comment comment = new Comment().setTime(noxbox.getTimeCompleted());
                    if(noxbox.getTimePartyDisliked() > 0) {
                        comment.setLike(false);
                        rating.setSentDislikes(rating.getSentDislikes() + 1);
                        if(isRatingUpdated) {
                            rating.setSentLikes(rating.getSentLikes() - 1);
                        }
                    } else {
                        comment.setLike(true);
                        rating.setSentLikes(rating.getSentLikes() + 1);
                        if(isRatingUpdated) {
                            rating.setSentDislikes(rating.getSentDislikes() - 1);
                        }
                    }

                    if(!Strings.isNullOrEmpty(noxbox.getOwnerComment())) {
                        comment.setText(noxbox.getOwnerComment());
                        rating.getComments().put(noxbox.getOwner().getId(), comment);
                    }
                }
            }
            profile().setRatingUpdateTime(System.currentTimeMillis());
            fireProfile();
        });


    }

    private static Query ratings(MarketRole role, String userId, long lastUpdate) {
        return db().collection("noxboxes")
                .whereEqualTo(role.getOwnerFieldId(), userId)
                .whereEqualTo("finished", true)
                .whereGreaterThan("timeRequested", 0)
                .whereGreaterThan("timeRatingUpdated", lastUpdate)
                .orderBy("timeRequested", Query.Direction.DESCENDING)
                .limit(100);
    }
}
