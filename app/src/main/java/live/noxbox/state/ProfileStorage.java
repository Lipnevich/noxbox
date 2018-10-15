package live.noxbox.state;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.model.WorkSchedule;
import live.noxbox.tools.Task;

import static live.noxbox.state.Firestore.writeProfile;

public class ProfileStorage {

    private static Profile profile;

    private static Map<String, Task<Profile>> listenTasks = new HashMap<>();
    private static Map<String, Task<Profile>> readTasks = new HashMap<>();
    private static List<Task<Noxbox>> noxboxTasks = new ArrayList<>();

    public static void listenProfile(String clazz, final Task<Profile> task) {
        if (profile != null) {
            task.execute(profile);
        }
        listenTasks.put(clazz, task);
    }

    public static void readProfile(final Task<Profile> task) {
        if (profile == null) {
            readTasks.put(task.hashCode() + "", task);
        } else {
            task.execute(profile);
        }
    }

    public static void startListening() {
        if (profile == null || (FirebaseAuth.getInstance().getCurrentUser() != null && !FirebaseAuth.getInstance().getCurrentUser().getUid().equals(profile.getId()))) {
            Firestore.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    ProfileStorage.profile = profile;
                    fireProfile();
                }
            });
        }
    }

    public static void fireProfile() {
        if (profile == null) return;

        writeProfile(profile);
        Crashlytics.log(Log.DEBUG, "fireProfile()", profile.getName());
        for (Task<Profile> task : listenTasks.values()) {
            task.execute(profile);
        }

        for (Iterator<Map.Entry<String, Task<Profile>>> task = readTasks.entrySet().iterator(); task.hasNext(); ) {
            Map.Entry<String, Task<Profile>> entry = task.next();
            entry.getValue().execute(profile);
            task.remove();
        }
    }

    public static Noxbox noxbox() {
        return new Noxbox()
                .setOwner(profile.publicInfo())
                .setRole(MarketRole.supply)
                .setType(NoxboxType.meditation)
                .setPrice("1")
                .setWorkSchedule(new WorkSchedule());
    }

    public static void listenCurrentNoxbox(final Task<Noxbox> task) {

    }

    private static void fireNoxbox() {
        for (Task<Noxbox> noxboxTask : noxboxTasks) {
            noxboxTask.execute(profile.getCurrent());
        }
    }

    public static void stopListen() {
        listenTasks.clear();
        readTasks.clear();
        noxboxTasks.clear();
        // TODO (nli) persist profile in firebase and bundle
    }

}
