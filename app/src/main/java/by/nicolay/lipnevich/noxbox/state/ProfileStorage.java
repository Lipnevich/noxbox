package by.nicolay.lipnevich.noxbox.state;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Wallet;
import by.nicolay.lipnevich.noxbox.model.WorkSchedule;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class ProfileStorage {

    private static Profile profile;

    private static Map<String, Task<Profile>> profileTasks = new HashMap<>();
    private static List<Task<Noxbox>> noxboxTasks = new ArrayList<>();

    public static void listenProfile(String clazz, final Task<Profile> task) {
        profileTasks.put(clazz, task);
        readProfile(task);
    }

    public static void readProfile(Task<Profile> task) {
        if (profile != null) {
            task.execute(profile);
        } else {
            Firebase.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    ProfileStorage.profile = profile;
                    if (profile.getWallet() == null) {
                        profile.setWallet(new Wallet());
                    }
                    if (profile.getCurrent() == null) {
                        profile.setCurrent(noxbox());
                    }
                    fireProfile();
                }
            });
        }
    }

    public static void fireProfile() {
        for (Task<Profile> profileTask : profileTasks.values()) {
            profileTask.execute(profile);
        }
    }

    private static Noxbox noxbox() {
        return new Noxbox()
                .setOwner(profile.publicInfo())
                .setRole(MarketRole.supply)
                .setType(NoxboxType.massage)
                .setPrice("1")
                .setWorkSchedule(new WorkSchedule());
    }

    public static void listenCurrentNoxbox(final Task<Noxbox> task) {
        noxboxTasks.add(task);
        if (profile != null) {
            task.execute(profile.getCurrent());
        } else {
            // TODO (nli) read noxbox instead
            readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    fireNoxbox();
                }
            });
        }
    }

    private static void fireNoxbox() {
        for (Task<Noxbox> noxboxTask : noxboxTasks) {
            noxboxTask.execute(profile.getCurrent());
        }
    }

    public static void stopListen() {
        profileTasks.clear();
        noxboxTasks.clear();
        // TODO (nli) persist profile in firebase and bundle
    }

}
