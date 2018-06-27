package by.nicolay.lipnevich.noxbox.state;

import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.UserAccount;
import by.nicolay.lipnevich.noxbox.tools.Task;

import java.util.ArrayList;
import java.util.List;

public class State {

    private static UserAccount userAccount;

    private static List<Task<Profile>> profileTasks = new ArrayList<>();

    public static void listenProfile(Task<Profile> task) {
        if(userAccount != null && userAccount.getProfile() != null) {
            task.execute(userAccount.getProfile().setRating(userAccount.getRating()));
        }
        profileTasks.add(task);
    }






}
