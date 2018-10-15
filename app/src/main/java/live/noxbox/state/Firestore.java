package live.noxbox.state;

import android.support.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import live.noxbox.BuildConfig;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static java.lang.reflect.Modifier.isStatic;

public class Firestore {

    private static FirebaseFirestore db() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.setFirestoreSettings(new FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build());
        return db;
    }

    private static DocumentReference profile() {
        return db().collection("profiles")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    // Profiles API
    public static void listenProfile(@NonNull final Task<Profile> task) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;

        profile().addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
        profile().set(objectToMap(profile), SetOptions.merge());
    }

    // in case of null value - does not override value
    public static Map<String, Object> objectToMap(Object object) {
        Map<String, Object> params = new HashMap<>();
        Class clazz = object.getClass();
        for (Field field : clazz.getDeclaredFields()) {
            if (!isStatic(field.getModifiers())) {
                field.setAccessible(true);
                try {
                    Object value = field.get(object);
                    if (value != null) {
                        if (value.getClass().isEnum()) {
                            params.put(field.getName(), value.toString());
                        } else if (value.getClass().getPackage().getName().startsWith(BuildConfig.APPLICATION_ID)) {
                            // write all application objects
                            params.put(field.getName(), objectToMap(value));
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
