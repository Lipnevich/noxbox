package by.nicolay.lipnevich.noxbox.state;

import by.nicolay.lipnevich.noxbox.model.*;
import by.nicolay.lipnevich.noxbox.tools.Task;

import java.util.ArrayList;
import java.util.List;

public class State {

    private static Profile profile;

    private static List<Task<Profile>> profileTasks = new ArrayList<>();
    private static List<Task<Noxbox>> noxboxTasks = new ArrayList<>();

    public static void listenProfile(final Task<Profile> task) {
        profileTasks.add(task);
        if (profile != null) {
            task.execute(profile);
        } else {
            Firebase.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    State.profile = profile;
                    if (profile.getWallet() == null) {
                        profile.setWallet(new Wallet());
                    }
                    if (profile.getRating() == null) {
                        profile.setRating(new AllRates());
                    }
                    if (profile.getCurrent() == null) {
                        profile.setCurrent(noxbox());
                    }
                    fireProfile();
                }
            });
        }
    }

    private static void fireProfile() {
        for (Task<Profile> profileTask : profileTasks) {
            profileTask.execute(profile);
        }
    }

    private static Noxbox noxbox() {
        return new Noxbox().setOwner(profile.publicInfo()).setRole(MarketRole.supply).setType(NoxboxType.massage).setPrice("1").setNoxboxTime(new NoxboxTime(TimePeriod.daily));
    }

    public static void listenCurrentNoxbox(final Task<Noxbox> task) {
        noxboxTasks.add(task);
        if(profile != null) {
            task.execute(profile.getCurrent());
        } else {
            // TODO (nli) read noxbox instead
            listenProfile(new Task<Profile>() {
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
