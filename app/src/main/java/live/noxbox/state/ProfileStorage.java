package live.noxbox.state;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static com.google.common.base.Objects.equal;
import static live.noxbox.state.Firestore.writeNoxbox;
import static live.noxbox.state.Firestore.writeProfile;
import static live.noxbox.state.GeoRealtime.offline;
import static live.noxbox.state.GeoRealtime.online;

public class ProfileStorage {

    private static Profile profile;
    private static Map<String, Task<Profile>> profileListeners = new HashMap<>();
    private static Map<String, Task<Profile>> profileReaders = new HashMap<>();

    private static final Task NONE = new Task() {
        @Override
        public void execute(Object noxbox) {
        }
    };

    public static void startListening() {
        if (profile == null || (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(profile.getId()))) {
            Firestore.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile newProfile) {
                    // since current and viewed are virtual Noxboxes we should transfer them
                    if (profile != null && newProfile != null) {
                        newProfile.setCurrent(profile.getCurrent());
                        newProfile.setViewed(profile.getViewed());
                    }
                    profile = newProfile;

                    if (profile != null && profile.getNoxboxId() != null) {
                        if (!profile.getNoxboxId().equals(profile.getCurrent().getId())) {
                            profile.getCurrent().setId(profile.getNoxboxId());
                        }
                        startListenNoxbox();
                    }

                    executeUITasks();
                }
            });
        }
    }

    public static void stopListen(String className) {
        profileListeners.remove(className);
    }

    public static void listenProfile(String clazz, final Task<Profile> task) {
        if (profile != null) {
            task.execute(profile);
        }
        profileListeners.put(clazz, task);
    }

    public static void readProfile(final Task<Profile> task) {
        if (profile == null) {
            profileReaders.put(task.hashCode() + "", task);
        } else {
            task.execute(profile);
        }
    }

    public static void fireProfile() {
        if (profile == null) return;

        if (!equal(profile.getCurrent().getId(), profile.getNoxboxId())) {
            profile.setNoxboxId(profile.getCurrent().getId());
        }
        writeProfile(profile);
        Crashlytics.log(Log.DEBUG, "fireProfile()", profile.getName());
        executeUITasks();
    }

    private static void executeUITasks() {
        for (Task<Profile> task : profileListeners.values()) {
            task.execute(profile);
        }

        for (Iterator<Map.Entry<String, Task<Profile>>> task = profileReaders.entrySet().iterator(); task.hasNext(); ) {
            Map.Entry<String, Task<Profile>> entry = task.next();
            entry.getValue().execute(profile);
            task.remove();
        }
    }

    public static void clearListeners() {
        profileListeners.clear();
        profileReaders.clear();
    }

    public static void clear() {
        clearListeners();
        if (profile.getCurrent().getId() != null) {
            Firestore.listenNoxbox(profile.getCurrent().getId(), NONE);
            if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.initial) {
                removeNoxbox();
            }
        }

        Firestore.listenProfile(NONE);
        profile = null;
    }

    public static void startListenNoxbox() {
        if (profile == null || profile.getNoxboxId() == null) return;
        Firestore.listenNoxbox(profile.getNoxboxId(), new Task<Noxbox>() {
            @Override
            public void execute(Noxbox current) {
                profile.setCurrent(current);
                executeUITasks();
            }
        });
    }

    public static void stopListenNoxbox() {
        if (profile == null || profile.getNoxboxId() == null) return;
        Firestore.listenNoxbox(profile.getNoxboxId(), NONE);
    }


    public static void noxboxCreated() {
        if (profile == null) return;

        profile.getCurrent().setTimeCreated(System.currentTimeMillis());
        removeNoxbox();
        writeNoxbox(profile.getCurrent());
        profile.setNoxboxId(profile.getCurrent().getId());

        startListenNoxbox();

        fireProfile();
        online(profile.getCurrent());
    }

    public static void removeNoxbox() {
        if (profile == null) return;
        String noxboxId = profile.getNoxboxId();
        if (noxboxId != null) {
            offline(profile.getCurrent());
            writeNoxbox(new Noxbox().setId(profile.getNoxboxId()).setTimeRemoved(System.currentTimeMillis()));
            stopListenNoxbox();
        }
    }

    public static void updateNoxbox() {
        if (profile == null) return;
        writeNoxbox(profile.getCurrent());
        executeUITasks();
    }
}
