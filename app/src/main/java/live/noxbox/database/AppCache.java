package live.noxbox.database;

import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;
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
import static live.noxbox.database.Firestore.getNewNoxboxId;
import static live.noxbox.database.Firestore.writeNoxbox;
import static live.noxbox.database.Firestore.writeProfile;
import static live.noxbox.database.GeoRealtime.offline;
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

                if (!profile.getNoxboxId().equals(newProfile.getNoxboxId())) {
                    if (!isNullOrEmpty(profile.getNoxboxId())) {
                        FirebaseMessaging.getInstance().unsubscribeFromTopic(profile.getNoxboxId());
                        stopListenNoxbox(profile.getNoxboxId());
                    }
                    startListenNoxbox(newProfile.getNoxboxId());
                }
                if (profile.getPosition().getLatitude() != 0.0
                        && newProfile.getPosition().getLatitude() == 0.0) {
                    newProfile.setPosition(profile.getPosition());
                }

                profile.copy(newProfile);
                if (profile.getFilters().getPrice() < 1) {
                    profile.getFilters().setPrice(Integer.MAX_VALUE);
                }

                if (!isNullOrEmpty(profile.getNoxboxId()) && !ids.contains(profile.getNoxboxId())) {
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
        profileListeners.put(clazz, task);

        if (isProfileReady())
            task.execute(profile);
    }

    public static void readProfile(final Task<Profile> task) {
        if (isProfileReady()) {
            task.execute(profile);
        } else {
            profileReaders.put(task.hashCode() + "", task);
        }
    }

    public static boolean isProfileReady() {
        return profile.getId() != null
                && (profile.getNoxboxId().isEmpty() || profile.getNoxboxId().equals(profile.getCurrent().getId()))
                && profile.getWallet().getAddress() != null;
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
            offline(profile.getCurrent());
        }
        Iterator<String> iterator = ids.iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            Firestore.listenNoxbox(id, NONE, NONE);
            iterator.remove();
        }
        Firestore.listenProfile(NONE);
        profile.copy(new Profile()).setCurrent(null).setViewed(null).setContract(null);
    }

    private static Set<String> ids = new HashSet<>();

    public static void startListenNoxbox(String noxboxId) {
        if (isNullOrEmpty(noxboxId) || ids.contains(noxboxId)) {
            return;
        }
        ids.add(noxboxId);
        FirebaseMessaging.getInstance().subscribeToTopic(noxboxId).addOnSuccessListener(o -> {
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
            }, onFailure -> {
                if (isNullOrEmpty(profile().getCurrent().getId())) {
                    // that mean that we just cleaned up db
                    profile().setNoxboxId("");
                    writeProfile(profile(), done -> executeUITasks());
                }
            });
        }).addOnFailureListener(e -> {
            Crashlytics.log(Log.ERROR, "failToSubscribeOnNoxbox", noxboxId);
        });
    }

    public static void stopListenNoxbox(String noxboxId) {
        if (isNullOrEmpty(noxboxId) || !ids.remove(noxboxId)) {
            return;
        }
        Firestore.listenNoxbox(noxboxId, NONE, NONE);
    }


    public static void createNoxbox(Task<Profile> onSuccess, Task<Exception> onFailure) {
        if (!isProfileReady()) return;
        profile.setNoxboxId(getNewNoxboxId());
        profile.getContract().setId(profile.getNoxboxId());
        profile.getContract().setTimeCreated(System.currentTimeMillis());
        if (profile.getContract().getRole() == MarketRole.supply) {
            profile.getContract().getOwner().setPortfolio(profile.getPortfolio());
        } else {
            profile.getContract().getOwner().setPortfolio(null);
        }

        profile.getCurrent().copy(profile.getContract());
        executeUITasks();

        // persist noxboxId in the profile
        writeProfile(profile, result -> {

            // create noxbox
            Firestore.updateNoxbox(profile.getContract(),
                    success -> {
                        onSuccess.execute(profile);
                    },
                    error -> {
                        // remove noxboxId from the profile
                        profile.setNoxboxId("");
                        offline(profile.getContract());
                        writeProfile(profile, NONE);
                        onFailure.execute(error);
                    });
        });
    }


    public static void removeNoxbox(Task<Profile> onSuccess) {
        if (!isProfileReady()) return;
        String noxboxId = profile.getNoxboxId();
        if (!isNullOrEmpty(noxboxId)) {
            profile.setNoxboxId("");
            profile.getCurrent().setTimeRemoved(System.currentTimeMillis());
            profile.getCurrent().setFinished(true);
            FirebaseMessaging.getInstance().unsubscribeFromTopic(noxboxId);
            stopListenNoxbox(noxboxId);
            offline(profile.getCurrent());

            writeNoxbox(profile.getCurrent(),
                    success -> writeProfile(profile, onSuccess), NONE);
        } else {
            onSuccess.execute(profile);
        }
    }

    public static void updateNoxbox() {
        if (!isProfileReady()) return;
        Firestore.updateNoxbox(profile.getCurrent(), noxboxId -> executeUITasks(), NONE);
    }

    public static String showPriceInUsd(String currency, String price) {
        if (!Strings.isNullOrEmpty(price.trim()) && wavesToUsd != null) {
            BigDecimal priceInWaves = new BigDecimal(price);
            BigDecimal priceInUSD = scale(priceInWaves.multiply(wavesToUsd));

            return currency + " (" + priceInUSD + "$)";
        }

        return currency;
    }
}
