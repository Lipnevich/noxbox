package live.noxbox.database;

import android.os.Handler;
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
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.tools.LogProperties;
import live.noxbox.tools.Task;
import live.noxbox.tools.location.LocationOperator;

import static com.google.common.base.Objects.equal;
import static com.google.common.base.Strings.isNullOrEmpty;
import static live.noxbox.analitics.BusinessEvent.inBox;
import static live.noxbox.database.Firestore.getNewNoxboxId;
import static live.noxbox.database.Firestore.writeNoxbox;
import static live.noxbox.database.Firestore.writeProfile;
import static live.noxbox.database.GeoRealtime.createKey;
import static live.noxbox.database.GeoRealtime.offline;
import static live.noxbox.database.GeoRealtime.online;
import static live.noxbox.tools.BalanceChecker.cleanRequestBalanceQueue;
import static live.noxbox.tools.MoneyFormatter.scale;

public class AppCache {

    public static Map<String, Noxbox> availableNoxboxes = new ConcurrentHashMap<>();
    private static final Profile profile;
    public static BigDecimal wavesToUsd;

    static {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            profile = new Profile(user);
        } else {
            profile = new Profile();
        }

    }

    //key is class name
    private static Map<String, Task<Map<String, Noxbox>>> availableNoxboxesListener = new HashMap<>();

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
                    if (!isNullOrEmpty(newProfile.getNoxboxId())) {
                        startListenNoxbox(newProfile.getNoxboxId());
                    }
                }
                if (profile.getPosition().getLatitude() != 0.0
                        && newProfile.getPosition().getLatitude() != 0.0) {
                    newProfile.setPosition(profile.getPosition());
                }

                profile.copy(newProfile);
                if (profile.getFilters().getPrice() < 1) {
                    profile.getFilters().setPrice(Integer.MAX_VALUE);
                }

                if (!isNullOrEmpty(profile.getNoxboxId())) {
                    startListenNoxbox(profile.getNoxboxId());
                } else {
                    executeUITasks();
                }
                new Thread(Firestore::ratingsUpdate).start();
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

    public static void startListenAvailableNoxboxes(String className, final Task<Map<String, Noxbox>> task) {
        GeoRealtime.startListenAvailableNoxboxes(Position.from(LocationOperator.location()).toGeoLocation(), availableNoxboxes, className);
        availableNoxboxesListener.put(className, task);

        task.execute(availableNoxboxes);
    }

    public static void stopListenAvailableNoxboxes(String className) {
        GeoRealtime.stopListenAvailableNoxboxes();
        availableNoxboxesListener.remove(className);
    }

    public static void executeAvailableNoxboxesTasks() {
        for (Map.Entry<String, Task<Map<String, Noxbox>>> entry : availableNoxboxesListener.entrySet()) {
            entry.getValue().execute(availableNoxboxes);
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

    private static void clearTasks() {
        profileListeners.clear();
        profileReaders.clear();
    }

    public static void logout() {
        clearTasks();
        cleanRequestBalanceQueue();
        if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.created) {
            offline(profile.getCurrent());
        }
        Iterator<String> iterator = ids.iterator();
        while (iterator.hasNext()) {
            String id = iterator.next();
            Firestore.stopListenNoxbox();
            iterator.remove();
        }
        Firestore.stopListenProfile();
        profile.copy(new Profile()).setCurrent(null).setViewed(null).setContract(null);

    }

    private static Set<String> ids = new HashSet<>();
    private static int failedAttempts = 0;

    public static void startListenNoxbox(String noxboxId) {
        if (isNullOrEmpty(noxboxId) || ids.contains(noxboxId)) {
            return;
        }
        ids.add(noxboxId);

        FirebaseMessaging.getInstance().subscribeToTopic(noxboxId).addOnSuccessListener(o -> {
            Firestore.listenNoxbox(noxboxId, noxbox -> {
                if (noxbox.getId().equals(profile.getNoxboxId())) {
                    if (profile.getCurrent().getId().equals(noxbox.getId()) &&
                            (profile.getCurrent().getNotMe(profile.getId()) == null
                                    || !profile.getCurrent().getNotMe(profile.getId()).getPhoto()
                                    .equals(noxbox.getNotMe(profile.getId()).getPhoto()))) {
                        profile.getCurrent().setConfirmationPhoto(null);
                    }
                    if (NoxboxState.getState(profile.getCurrent(), profile) == NoxboxState.requesting) {
                        noxbox.setTimeRequested(profile.getCurrent().getTimeRequested());
                        noxbox.setParty(profile.getCurrent().getParty());
                    }
                    profile.getCurrent().copy(noxbox);
                } else {
                    profile.getViewed().copy(noxbox);
                }
                executeUITasks();
                failedAttempts = 0;
            }, onFailure -> {
                ids.remove(noxboxId);
                failedAttempts++;
                if (isNullOrEmpty(profile().getCurrent().getId())) {
                    // that mean that we just cleaned up db
                    profile().setNoxboxId("");
                    writeProfile(profile(), done -> executeUITasks());
                } else if (++failedAttempts < 5) {
                    // in case we start listen noxbox with not completed persisting
                    new Handler().postDelayed(() -> startListenNoxbox(noxboxId), 500);
                }
            });
        }).addOnFailureListener(e -> {
            ids.remove(noxboxId);
            Crashlytics.log(Log.ERROR, "failToSubscribeOnNoxbox", noxboxId);
        });
    }

    public static void stopListenNoxbox(String noxboxId) {
        if (isNullOrEmpty(noxboxId) || !ids.remove(noxboxId)) {
            return;
        }
        Firestore.stopListenNoxbox();
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
        profile().getContract().setGeoId(createKey(profile().getContract(), profile.getFilters().getAllowNovices()));

        profile.getContract().setOwner(profile.publicInfo(profile.getContract().getRole(), profile.getContract().getType()));
        profile.getCurrent().copy(profile.getContract());
        executeUITasks();

        // persist noxboxId in the profile
        writeProfile(profile, result -> {

            // create noxbox
            Firestore.updateNoxbox(profile.getContract(),
                    success -> {
                        startListenNoxbox(profile.getNoxboxId());
                        onSuccess.execute(profile);
                        online(profile().getCurrent());
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
