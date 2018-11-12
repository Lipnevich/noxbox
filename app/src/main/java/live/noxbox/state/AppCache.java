package live.noxbox.state;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static com.google.common.base.Objects.equal;
import static live.noxbox.state.Firestore.writeNoxbox;
import static live.noxbox.state.Firestore.writeProfile;
import static live.noxbox.state.GeoRealtime.offline;
import static live.noxbox.state.GeoRealtime.online;

public class AppCache {

    private static Profile profile;
    public static Map<String, Noxbox> markers = new ConcurrentHashMap<>();

    private static Map<String, Task<Profile>> profileListeners = new HashMap<>();
    private static Map<String, Task<Profile>> profileReaders = new HashMap<>();

    private static final Task NONE = new Task() {
        @Override
        public void execute(Object noxbox) {
        }
    };

    public static void startListening() {
        if (profile == null
                || (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(profile.getId()))) {
            Firestore.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile newProfile) {
                    // since current and viewed are virtual Noxboxes we should transfer them
                    if (profile != null) {
                        newProfile.setCurrent(profile.getCurrent());
                        newProfile.setViewed(profile.getViewed());
                    }
                    profile = newProfile;

                    if (profile != null && profile.getNoxboxId() != null) {
                        startListenNoxbox(profile.getNoxboxId());
                    } else {
                        executeUITasks();
                    }
                }
            });
        }
    }

    public static void stopListen(String className) {
        profileListeners.remove(className);
    }

    public static void listenProfile(String clazz, final Task<Profile> task) {
        if (isProfileReady()) {
            task.execute(profile);
        }
        profileListeners.put(clazz, task);
    }

    public static void readProfile(final Task<Profile> task) {
        if (isProfileReady()) {
            task.execute(profile);
        } else {
            profileReaders.put(task.hashCode() + "", task);
        }
    }

    private static boolean isProfileReady() {
        return profile != null && (profile.getNoxboxId() == null || profile.getCurrent() != null);
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
        LogProperties.update(profile, profileListeners);

        for (Task<Profile> task : profileListeners.values()) {
            task.execute(profile);
        }

        for (Iterator<Map.Entry<String, Task<Profile>>> task = profileReaders.entrySet().iterator(); task.hasNext(); ) {
            Map.Entry<String, Task<Profile>> entry = task.next();
            entry.getValue().execute(profile);
            task.remove();
        }
    }

    public static void clearTasks() {
        profileListeners.clear();
        profileReaders.clear();
    }

    public static void logout() {
        clearTasks();
        if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.created) {
            removeNoxbox();
        }
        for(String noxboxId : ids) {
            stopListenNoxbox(noxboxId);
        }
        Firestore.listenProfile(NONE);
        profile = null;
    }

    private static Set<String> ids = new HashSet<>();

    public static void startListenNoxbox(String noxboxId) {
        if (noxboxId == null || ids.contains(noxboxId)) return;
        ids.add(noxboxId);
        FirebaseMessaging.getInstance().subscribeToTopic(noxboxId);
        Firestore.listenNoxbox(noxboxId, new Task<Noxbox>() {
            @Override
            public void execute(Noxbox noxbox) {
                if(noxbox.getId().equals(profile.getNoxboxId())) {
                    profile.setCurrent(noxbox);
                } else {
                    profile.setViewed(noxbox);
                }
                executeUITasks();
            }
        });
    }

    public static void stopListenNoxbox(String noxboxId) {
        if (noxboxId == null || !ids.remove(noxboxId)) return;
        Firestore.listenNoxbox(noxboxId, NONE);
    }


    public static void noxboxCreated() {
        if (profile == null) return;

        // remove previous noxbox
        removeNoxbox();
        profile.getCurrent().setTimeCreated(System.currentTimeMillis());
        profile.getCurrent().setOwner(profile.publicInfo());
        if (profile.getCurrent().getRole() == MarketRole.supply) {
            profile.getCurrent().getOwner().setPortfolio(profile.getPortfolio());
        } else {
            profile.getCurrent().getOwner().setPortfolio(null);
        }
        writeNoxbox(profile.getCurrent());
        profile.setNoxboxId(profile.getCurrent().getId());

        writeProfile(profile);
        online(profile.getCurrent());
    }

    public static void removeNoxbox() {
        if (profile == null) return;
        String noxboxId = profile.getNoxboxId();
        if (noxboxId != null) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(noxboxId);
            offline(profile.getCurrent());
            writeNoxbox(new Noxbox().setId(profile.getNoxboxId()).setTimeRemoved(System.currentTimeMillis()));
            stopListenNoxbox(noxboxId);
            profile.setNoxboxId(null);
            profile.getCurrent().clean();
        }
    }

    public static void updateNoxbox() {
        if (profile == null) return;
        writeNoxbox(profile.getCurrent());
        executeUITasks();
    }
}
