package live.noxbox.state;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

import static com.google.common.base.Objects.equal;
import static live.noxbox.state.Firestore.writeNoxbox;
import static live.noxbox.state.Firestore.writeProfile;

public class ProfileStorage {

    private static Profile profile;

    private static Map<String, Task<Profile>> listenProfile = new HashMap<>();
    private static Map<String, Task<Profile>> readProfile = new HashMap<>();

    public static void startListening() {
        if (profile == null || (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(profile.getId()))) {
            Firestore.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile newProfile) {
                    // since current and viewed are virtual Noxboxes we should transfer them
                    if(profile != null && newProfile != null) {
                        newProfile.setCurrent(profile.getCurrent());
                        newProfile.setViewed(profile.getViewed());
                    }
                    profile = newProfile;

                    if(profile.getNoxboxId() != null) {
                        if(!profile.getNoxboxId().equals(profile.getCurrent().getId())) {
                            profile.getCurrent().clean();
                        }
                        Firestore.listenNoxbox(profile.getNoxboxId(), new Task<Noxbox>() {
                            @Override
                            public void execute(Noxbox current) {
                                if(current != null) {
                                    profile.setCurrent(profile.getCurrent());
                                }
                                executeTasks();
                            }
                        });
                    }

                    fireProfile();

                    ProfileStorage.profile.getCurrent().onNoxboxChangeListener = new Task<Noxbox>() {
                        @Override
                        public void execute(Noxbox noxbox) {
                            writeNoxbox(profile.getCurrent());
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

        if(!equal(profile.getCurrent().getId(), profile.getNoxboxId())) {
            profile.setNoxboxId(profile.getCurrent().getId());
        }
        writeProfile(profile);
        Crashlytics.log(Log.DEBUG, "fireProfile()", profile.getName());
        executeTasks();
    }

    private static void executeTasks() {
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
    }
}
