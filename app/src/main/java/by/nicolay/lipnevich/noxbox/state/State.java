package by.nicolay.lipnevich.noxbox.state;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.NoxboxTime;
import by.nicolay.lipnevich.noxbox.model.NoxboxType;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.TimePeriod;
import by.nicolay.lipnevich.noxbox.tools.Firebase;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class State {

    private static Profile profile;
    private static Noxbox noxbox;

    private static List<Task<Profile>> profileTasks = new ArrayList<>();
    private static List<Task<Noxbox>> noxboxTasks = new ArrayList<>();

    public static void listenProfile(Task<Profile> task) {
        profileTasks.add(task);
        if (profile != null) {
            task.execute(profile.setRating(profile.getRating()));
        } else {
            Firebase.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile object) {
                    profile = object;
                }
            });
        }
    }

    public static Profile getProfile() {
        if (profile == null) {
            profile = Profile.createFrom(FirebaseAuth.getInstance().getCurrentUser());
        }
        return profile;
    }

    public static void setProfile(Profile profile) {
        State.profile = profile;
        for (Task<Profile> profileTask : profileTasks) {
            profileTask.execute(profile);
        }
    }

    public static Noxbox getCurrentNoxbox() {
        if(noxbox == null){
            noxbox = noxbox();
        }
        return noxbox;
    }

    private static Noxbox noxbox() {
        return new Noxbox().setOwner(getProfile().publicInfo()).setRole(MarketRole.supply).setType(NoxboxType.massage).setPrice("1").setNoxboxTime(new NoxboxTime(TimePeriod.daily));
    }

    public static void listenCurrentNoxbox(Task<Noxbox> task) {
        noxboxTasks.add(task);
        task.execute(getCurrentNoxbox());
    }

    public static void setCurrentNoxbox(Noxbox noxbox) {
        State.noxbox = noxbox;
        for (Task<Noxbox> noxboxTask : noxboxTasks) {
            noxboxTask.execute(noxbox);
        }
    }

    public static void stopListen() {
        profileTasks.clear();
        noxboxTasks.clear();
    }

}
