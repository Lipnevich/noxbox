package live.noxbox.database;

import android.support.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import live.noxbox.BuildConfig;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.Virtual;
import live.noxbox.tools.Task;

import static java.lang.reflect.Modifier.isStatic;
import static live.noxbox.model.Noxbox.isNullOrZero;

public class Firestore {

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

    public static void listenProfile(@NonNull final Task<Profile> task) {
        if (profileListener != null) profileListener.remove();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        profileListener = profileReference(user.getUid()).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists()) {
                Profile profile = snapshot.toObject(Profile.class);
                task.execute(profile);
            } else {
                Profile profile = new Profile(user);
                writeProfile(profile, o -> task.execute(profile));
            }
        });
    }

    public static void writeProfile(final Profile profile, Task<Profile> onSuccess) {
        profileReference(profile.getId()).set(objectToMap(profile), SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onSuccess.execute(profile);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(e);
                    }
                });
    }

    // Current noxbox
    private static ListenerRegistration noxboxListener;

    public static void listenNoxbox(@NonNull String noxboxId, @NonNull final Task<Noxbox> task) {
        if (noxboxListener != null) noxboxListener.remove();

        noxboxListener = noxboxReference(noxboxId).addSnapshotListener((snapshot, e) -> {
            if (snapshot != null && snapshot.exists() && e == null) {
                Noxbox current = snapshot.toObject(Noxbox.class);
                task.execute(current);
            }
        });
    }

    public static void createOrUpdateNoxbox(Noxbox current, Task<String> onSuccess, Task<Profile> onFailure){
        if (current.getId() == null || current.getId().isEmpty()) {
            String newNoxboxId = db().collection("noxboxes").document().getId();
            current.setId(newNoxboxId);
        }

        if (current.getRole() == MarketRole.supply) {
            current.setPerformerId(current.getOwner().getId());
            current.setPayerId(current.getParty() == null ? "" : current.getParty().getId());
        } else if (current.getRole() == MarketRole.demand) {
            current.setPerformerId(current.getParty() == null ? "" : current.getParty().getId());
            current.setPayerId(current.getOwner().getId());
        }

        writeNoxbox(current,onSuccess,onFailure);

        if (!isNullOrZero(current.getTimeCompleted())) {
            GeoRealtime.offline(current);
        }
    }

    public static void writeNoxbox(Noxbox current, Task<String> onSuccess, Task<Profile> onFailure) {
        current.setFinished(isFinished(current));

        String currentId = current.getId();
        noxboxReference(current.getId()).set(objectToMap(current), SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        onSuccess.execute(currentId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Crashlytics.logException(e);
                        onFailure.execute(null);
                    }
                });
    }

    private static boolean isFinished(Noxbox noxbox) {
        return noxbox.getTimeCanceledByOwner() > 0
                || noxbox.getTimeCanceledByParty() > 0
                || noxbox.getTimeCompleted() > 0
                || noxbox.getTimeRemoved() > 0
                || noxbox.getTimeTimeout() > 0
                ;
    }

    public static void readNoxbox(String noxboxId, final Task<Noxbox> task) {
        noxboxReference(noxboxId).get().addOnCompleteListener(completion -> {
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

}
