package live.noxbox.state;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.collect.ImmutableMap;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.NoxboxStrategy;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static com.google.common.base.Objects.equal;
import static live.noxbox.state.Firebase.offline;
import static live.noxbox.state.Firebase.online;
import static live.noxbox.state.Firestore.writeNoxbox;
import static live.noxbox.state.Firestore.writeProfile;

public class ProfileStorage {

    private static Profile profile;


    private static Map<String, Task<Profile>> listenProfile = new HashMap<>();
    private static Map<String, Task<Profile>> readProfile = new HashMap<>();

    private static final Task NONE = new Task() {
        @Override
        public void execute(Object noxbox) {
        }
    };

    private static final Task UPDATE = new Task<Noxbox>() {
        @Override
        public void execute(Noxbox noxbox) {
            if (profile != null) {
                writeNoxbox(profile.getCurrent());
            }
        }
    };

    private static final Task REMOVE = new Task<Noxbox>() {
        @Override
        public void execute(Noxbox noxbox) {
            if (profile != null) {
                removeNoxbox(profile.getNoxboxId());
            }
        }
    };

    private static void removeNoxbox(String noxboxId) {
        if (noxboxId != null) {
            offline(profile.getCurrent());
            writeNoxbox(new Noxbox().setId(noxboxId).setTimeRemoved(System.currentTimeMillis()));
            Firestore.listenNoxbox(noxboxId, NONE);
        }
    }

    private static final Task CREATE = new Task<Noxbox>() {
        @Override
        public void execute(Noxbox noxbox) {
            if (profile != null) {
                removeNoxbox(profile.getNoxboxId());
                writeNoxbox(profile.getCurrent());
                profile.setNoxboxId(profile.getCurrent().getId());
                Firestore.listenNoxbox(profile.getNoxboxId(), new Task<Noxbox>() {
                    @Override
                    public void execute(Noxbox current) {
                        profile.setCurrent(current);
                        executeUITasks();
                    }
                });

                fireProfile();
                online(noxbox);
            }
        }
    };

    private static Map<NoxboxStrategy, Task> ON = ImmutableMap.of(
            NoxboxStrategy.update, UPDATE,
            NoxboxStrategy.create, CREATE,
            NoxboxStrategy.remove, REMOVE);

    private static Map<NoxboxStrategy, Task> OFF = ImmutableMap.of(
            NoxboxStrategy.update, NONE,
            NoxboxStrategy.create, NONE,
            NoxboxStrategy.remove, NONE);

    public static void startListening() {
        if (profile == null || (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(profile.getId()))) {
            Firestore.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile newProfile) {
                    if (profile != null) {
                        profile.getCurrent().strategy = OFF;
                    }

                    // since current and viewed are virtual Noxboxes we should transfer them
                    if (profile != null && newProfile != null) {
                        newProfile.setCurrent(profile.getCurrent());
                        newProfile.setViewed(profile.getViewed());
                    }
                    profile = newProfile;

                    if (profile.getNoxboxId() != null) {
                        if (!profile.getNoxboxId().equals(profile.getCurrent().getId())) {
                            profile.getCurrent().setId(profile.getNoxboxId());
                        }
                        Firestore.listenNoxbox(profile.getNoxboxId(), new Task<Noxbox>() {
                            @Override
                            public void execute(Noxbox current) {
                                current.strategy = profile.getCurrent().strategy;

                                profile.setCurrent(current);
                                executeUITasks();
                            }
                        });
                    }

                    fireProfile();

                    if (profile != null) {
                        profile.getCurrent().strategy = ON;
                    }
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

        if (!equal(profile.getCurrent().getId(), profile.getNoxboxId())) {
            profile.setNoxboxId(profile.getCurrent().getId());
        }
        writeProfile(profile);
        Crashlytics.log(Log.DEBUG, "fireProfile()", profile.getName());
        executeUITasks();
    }

    private static void executeUITasks() {
        for (Task<Profile> task : listenProfile.values()) {
            task.execute(profile);
        }

        for (Iterator<Map.Entry<String, Task<Profile>>> task = readProfile.entrySet().iterator(); task.hasNext(); ) {
            Map.Entry<String, Task<Profile>> entry = task.next();
            entry.getValue().execute(profile);
            task.remove();
        }
    }

    public static void stopListen() {
        listenProfile.clear();
        readProfile.clear();
    }

    public static void clear() {
        stopListen();
        if (profile.getCurrent().getId() != null) {
            Firestore.listenNoxbox(profile.getCurrent().getId(), NONE);
            if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.initial) {
                removeNoxbox(profile.getNoxboxId());
            }
        }
        if (profile != null) {
            profile.getCurrent().strategy = OFF;
        }

        Firestore.listenProfile(NONE);
        profile = null;
    }
}
