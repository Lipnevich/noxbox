package live.noxbox.state;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static live.noxbox.state.Firestore.writeProfile;

public class ProfileStorage {

    private static Profile profile;

    private static Map<String, Task<Profile>> listenProfile = new HashMap<>();
    private static Map<String, Task<Profile>> readProfile = new HashMap<>();
    private static List<Task<Noxbox>> listenNoxbox = new ArrayList<>();
    private static List<Task<Noxbox>> readNoxbox = new ArrayList<>();

    public static void startListening() {
        if (profile == null || (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(profile.getId()))) {
            Firestore.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    ProfileStorage.profile = profile;

                    fireProfile();

                    ProfileStorage.profile.getCurrent().onNoxboxChangeListener = new Task<Noxbox>() {
                        @Override
                        public void execute(Noxbox noxbox) {
                            fireNoxbox();
                        }
                    };
                }
            });
        }
    }

    public static void listenProfile(String clazz, final Task<Profile> task) {
        if (profile != null) {
            task.execute(profile);
        }
        listenProfile.put(clazz, task);
    }

    public static void readProfile(final Task<Profile> task) {
        if (profile == null) {
            readProfile.put(task.hashCode() + "", task);
        } else {
            task.execute(profile);
        }
    }

    public static void fireProfile() {
        if (profile == null) return;

        ProfileStorage.profile.getCurrent().onNoxboxChangeListener = new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
            }
        };

        writeProfile(profile);
        Crashlytics.log(Log.DEBUG, "fireProfile()", profile.getName());
        for (Task<Profile> task : listenProfile.values()) {
            task.execute(profile);
        }

        for (Iterator<Map.Entry<String, Task<Profile>>> task = readProfile.entrySet().iterator(); task.hasNext(); ) {
            Map.Entry<String, Task<Profile>> entry = task.next();
            entry.getValue().execute(profile);
            task.remove();
        }
    }

    public static void listenCurrentNoxbox(final Task<Noxbox> task) {

    }

    private static void fireNoxbox() {
        for (Task<Noxbox> noxboxTask : listenNoxbox) {
            noxboxTask.execute(profile.getCurrent());
        }
        writeNoxbox(profile.getCurrent());
    }

    private static void writeNoxbox(Noxbox current) {

    }


    public static void stopListen() {
        listenProfile.clear();
        readProfile.clear();
        listenNoxbox.clear();
        // TODO (nli) persist profile in firebase and bundle
    }

    public static void clear() {
        stopListen();
    }
}
