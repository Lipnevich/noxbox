package live.noxbox.tools;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;

import java.util.Map;

import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;

public final class LogProperties {

    private static String PROFILE_LISTENER = "profileListener";
    private static String PROFILE_NOXBOX_ID = "profileNoxboxId";
    private static String NOXBOX_STATE = "noxboxState";

    public static void update(Profile profile, Map<String, Task<Profile>> profileListeners) {
        int i = 0;
        for (Task<Profile> profileListener : profileListeners.values()) {
            Crashlytics.setString(PROFILE_LISTENER + ++i, profileListener.getClass().getName());
        }
        Crashlytics.setUserName(profile.getName());
        if(!Strings.isNullOrEmpty(profile.getNoxboxId())) {
            Crashlytics.setString(PROFILE_NOXBOX_ID, profile.getNoxboxId());
        }
        Crashlytics.setString(NOXBOX_STATE, NoxboxState.getState(profile.getCurrent(), profile).name());
    }

}
