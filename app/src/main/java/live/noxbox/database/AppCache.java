package live.noxbox.database;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.messaging.FirebaseMessaging;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import live.noxbox.analitics.BusinessActivity;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Profile;
import live.noxbox.tools.LogProperties;
import live.noxbox.tools.Task;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Strings.isNullOrEmpty;
import static live.noxbox.analitics.BusinessEvent.complete;
import static live.noxbox.analitics.BusinessEvent.inBox;
import static live.noxbox.database.Firestore.createOrUpdateNoxbox;
import static live.noxbox.database.Firestore.writeNoxbox;
import static live.noxbox.database.Firestore.writeProfile;
import static live.noxbox.database.GeoRealtime.offline;
import static live.noxbox.database.GeoRealtime.online;
import static live.noxbox.tools.MoneyFormatter.scale;

public class AppCache {

    public static Map<String, Noxbox> availableNoxboxes = new ConcurrentHashMap<>();
    public static BigDecimal wavesToUsd;
    private static final Profile profile;


    static {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            profile = new Profile(user);
        } else {
            profile = new Profile();
        }

    }

    public static Profile profile() {
        return profile;
    }

    private static Map<String, Task<Profile>> profileListeners = new HashMap<>();
    private static Map<String, Task<Profile>> profileReaders = new HashMap<>();

    public static final Task NONE = noxbox -> {
    };

    public static void startListening() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (!isProfileReady()
                || (user != null && !user.getUid().equals(profile.getId()))) {
            Firestore.listenProfile(newProfile -> {
                eventBalanceUpdate(profile.getWallet().getBalance(), newProfile.getWallet().getBalance());

                profile.copy(newProfile);

                if (!profile.getNoxboxId().isEmpty()) {
                    startListenNoxbox(profile.getNoxboxId());
                } else {
                    executeUITasks();
                }
            });
        }
    }

    private static void eventBalanceUpdate(String previous, String next) {
        BigDecimal previousBalance = scale(previous != null ? new BigDecimal(previous) : BigDecimal.ZERO);
        BigDecimal nextBalance = scale(next != null ? new BigDecimal(next) : BigDecimal.ZERO);
        if (nextBalance.compareTo(previousBalance) > 0) {
            BusinessActivity.businessEvent(inBox);
        }

    }

    public static void stopListen(String className) {
        profileListeners.remove(className);
    }

    public static void listenProfile(String clazz, final Task<Profile> task) {
        if (isProfileReady()) {
            task.execute(profile);
        }
        profileListeners.put(clazz, task);
    }

    public static void readProfile(final Task<Profile> task) {
        if (isProfileReady()) {
            task.execute(profile);
        } else {
            profileReaders.put(task.hashCode() + "", task);
        }
    }

    public static boolean isProfileReady() {
        return (profile.getNoxboxId().isEmpty() || profile.getCurrent() != null) && profile.getWallet().getAddress() != null;
    }

    public static void fireProfile() {
        if (!isProfileReady()) return;

        if (!equal(profile.getCurrent().getId(), profile.getNoxboxId())) {
            profile.setNoxboxId(profile.getCurrent().getId());
        }
        writeProfile(profile, profile -> {
            Crashlytics.log(Log.DEBUG, "fireProfile()", profile.getName());
            executeUITasks();
        });
    }

    public static void executeUITasks() {
        LogProperties.update(profile, profileListeners);

        for (Map.Entry<String, Task<Profile>> entry : profileListeners.entrySet()) {
            entry.getValue().execute(profile);
        }

        for (Iterator<Map.Entry<String, Task<Profile>>> task = profileReaders.entrySet().iterator(); task.hasNext(); ) {
            Map.Entry<String, Task<Profile>> entry = task.next();
            entry.getValue().execute(profile);
            task.remove();
        }
    }

    public static void clearTasks() {
        profileListeners.clear();
        profileReaders.clear();
    }

    public static void logout() {
        clearTasks();
        if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.created) {
            removeNoxbox(profile.getCurrent(), task -> task.getCurrent().clean());
        }
        for (String noxboxId : ids) {
            stopListenNoxbox(noxboxId);
        }
        Firestore.listenProfile(NONE);
        profile.copy(new Profile()).setCurrent(null).setViewed(null);
    }

    private static Set<String> ids = new HashSet<>();

    public static void startListenNoxbox(String noxboxId) {
        if (isNullOrEmpty(noxboxId) || ids.contains(noxboxId)) return;
        ids.add(noxboxId);
        FirebaseMessaging.getInstance().subscribeToTopic(noxboxId);
        Firestore.listenNoxbox(noxboxId, noxbox -> {
            if (noxbox.getId().equals(profile.getNoxboxId())) {
                long oldTimeCompleted = profile().getCurrent().getTimeCompleted();
                long newTimeCompleted = noxbox.getTimeCompleted();

                profile.getCurrent().copy(noxbox);

                if (newTimeCompleted > 0 && oldTimeCompleted == 0) {
                    BusinessActivity.businessEvent(complete);
                }
            } else {
                profile.getViewed().copy(noxbox);
            }
            executeUITasks();
        });
    }

    public static void stopListenNoxbox(String noxboxId) {
        if (isNullOrEmpty(noxboxId) || !ids.remove(noxboxId)) return;
        Firestore.listenNoxbox(noxboxId, NONE);
    }


    public static void noxboxCreated(Task<Profile> onSuccess, Task<Profile> onFailure) {
        if (!isProfileReady()) return;

        // remove noxbox from backup
        removeNoxbox(profile.getCurrent(), o-> {

            profile.getCurrent().setTimeCreated(System.currentTimeMillis());
            if (profile.getCurrent().getRole() == MarketRole.supply) {
                profile.getCurrent().getOwner().setPortfolio(profile.getPortfolio());
            } else {
                profile.getCurrent().getOwner().setPortfolio(null);
            }

            createOrUpdateNoxbox(profile.getCurrent(), noxboxId -> {
                profile.setNoxboxId(noxboxId);
                writeProfile(profile, profile -> online(profile.getCurrent()));
                onSuccess.execute(profile);
            }, onFailure);
        });


    }


    public static void removeNoxbox(Noxbox noxbox, Task<Profile> onSuccess) {
        if (!isProfileReady()) return;
        String noxboxId = profile.getNoxboxId();
        if (!isNullOrEmpty(noxboxId)) {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(noxboxId);

            offline(profile.getBackup());
            profile.setNoxboxId("");
            writeNoxbox(noxbox.setTimeRemoved(System.currentTimeMillis()),
                    o-> {
                        stopListenNoxbox(noxboxId);
                        writeProfile(profile, onSuccess);
                    }, NONE);
        } else {
            onSuccess.execute(profile);
        }
    }

    public static void updateNoxbox() {
        if (!isProfileReady()) return;
        createOrUpdateNoxbox(profile.getCurrent(), noxboxId -> executeUITasks(), NONE);

    }

    public static String showPriceInUsd(String currency, String price) {
        if (wavesToUsd != null) {
            BigDecimal priceInWaves = new BigDecimal(price.replaceAll(",", "\\."));
            BigDecimal priceInUSD = priceInWaves.multiply(wavesToUsd);
            return currency + " (" + priceInUSD + "$)";
        }
        return "";
    }
}
