package live.noxbox.database;

import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import live.noxbox.BuildConfig;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.Virtual;
import live.noxbox.tools.Task;

import static java.lang.reflect.Modifier.isStatic;

public class Firestore {

    private static FirebaseFirestore db() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().build());
        return db;
    }

    private static DocumentReference profileReference() {
        return db().collection("profiles")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    private static DocumentReference noxboxReference(String noxboxId) {
        return db().collection("noxboxes").document(noxboxId);
    }

    // Current profile
    private static ListenerRegistration profileListener;
    public static void listenProfile(@NonNull final Task<Profile> task) {
        if(profileListener != null) profileListener.remove();

        profileListener = profileReference().addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot != null && snapshot.exists()) {
                    Profile profile = snapshot.toObject(Profile.class);
                    task.execute(profile);
                }
            }
        });
    }

    public static void writeProfile(final Profile profile) {
        profileReference().set(objectToMap(profile), SetOptions.merge());
    }

    // Current noxbox
    private static ListenerRegistration noxboxListener;
    public static void listenNoxbox(@NonNull String noxboxId, @NonNull final Task<Noxbox> task) {
        if(noxboxListener != null) noxboxListener.remove();

        noxboxListener = noxboxReference(noxboxId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
                if (snapshot != null && snapshot.exists()) {
                    Noxbox current = snapshot.toObject(Noxbox.class);
                    task.execute(current);
                }
            }
        });
    }

    public static void writeNoxbox(final Noxbox current) {
        if (current.getId() == null) {
            String newNoxboxId = db().collection("noxboxes").document().getId();
            current.setId(newNoxboxId);
        }
        // ids for history queries
        if(current.getRole() == MarketRole.supply) {
            current.setPerformerId(current.getOwner().getId());
            current.setPayerId(current.getParty() == null ? null : current.getParty().getId());
        } else if(current.getRole() == MarketRole.demand) {
            current.setPerformerId(current.getParty() == null ? null : current.getParty().getId());
            current.setPayerId(current.getOwner().getId());
        }

        noxboxReference(current.getId()).set(objectToMap(current), SetOptions.merge());

        if(current.getTimeCompleted() != null){
            GeoRealtime.offline(current);
        }
    }

    public static void readNoxbox(String noxboxId, final Task<Noxbox> task){
        noxboxReference(noxboxId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> completion) {
                if(!completion.isSuccessful()) return;
                DocumentSnapshot snapshot = completion.getResult();
                if (snapshot != null && snapshot.exists()) {
                    Noxbox noxbox = snapshot.toObject(Noxbox.class);
                    task.execute(noxbox);
                }
            }
        });
    }

    public static void readHistory(long start, int count, MarketRole role, final Task<Collection<Noxbox>> task) {
        noxboxes(start, count, role)
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> result) {
                Collection<Noxbox> noxboxes = new ArrayList<>();
                if (result.isSuccessful() && result.getResult() != null) {
                    for (QueryDocumentSnapshot document : result.getResult()) {
                        noxboxes.add(document.toObject(Noxbox.class));
                    }
                }
                task.execute(noxboxes);
            }
        });
    }

    private static Query noxboxes(long start, int count, MarketRole role) {
        return db().collection("noxboxes")
                .whereEqualTo(role.getOwnerFieldId(), FirebaseAuth.getInstance().getCurrentUser().getUid())
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
