package live.noxbox.state;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;

import java.util.Map;

import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.tools.Task;

public final class LogProperties {

    private static String PROFILE_LISTENER = "profileListener";
    private static String PROFILE_PUBLIC_INFO = "profilePublicInfo";
    private static String PROFILE_NOXBOX_ID = "profileNoxboxId";
    private static String NOXBOX_STATE = "noxboxState";

    public static void update(Profile profile, Map<String, Task<Profile>> profileListeners) {
        int i = 0;
        for (Task<Profile> profileListener : profileListeners.values()) {
            Crashlytics.setString(PROFILE_LISTENER + ++i, profileListener.getClass().getName());
        }
        Crashlytics.setString(PROFILE_PUBLIC_INFO, new Gson().toJson(profile.notPublicInfo()));
        Crashlytics.setString(PROFILE_NOXBOX_ID, profile.getNoxboxId());
        Crashlytics.setString(NOXBOX_STATE, NoxboxState.getState(profile.getCurrent(), profile).name());
    }

}
