package live.noxbox.state;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Map;

import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

public final class LogProperties {

    private static String PROFILE_LISTENER = "profileListener";
    private static String PROFILE = "profile";
    private static String NOXBOX_STATE = "noxboxState";
    private static String NOXBOX_STRATEGY = "noxboxUpdateStrategy";

    public static void update(Profile profile, Map<String, Task<Profile>> profileListeners, Map off) {
        int i = 0;
        for(Task<Profile> profileListener : profileListeners.values()) {
            Crashlytics.setString(PROFILE_LISTENER + ++i, profileListener.getClass().getName());
        }
        Crashlytics.setString(PROFILE, new Gson().toJson(profile));
        Crashlytics.setString(NOXBOX_STATE, NoxboxState.getState(profile.getCurrent(), profile).name());
        Crashlytics.setBool(NOXBOX_STRATEGY, off == profile.getCurrent().strategy);
    }

}
