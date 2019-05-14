package live.noxbox.tools.location.moving;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationListener;
import android.location.LocationManager;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.gms.maps.GoogleMap;

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import live.noxbox.Constants;
import live.noxbox.database.GeoRealtime;
import live.noxbox.model.NotificationType;
import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.states.Moving;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.profile;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.tools.location.moving.ForegroundLocationListener.provideContextLinks;

/**
 * Created by Vladislaw Kravchenok on 14.05.2019.
 */
public abstract class MovingWorker extends Worker {

    public MovingWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
        noxboxId = profile().getNoxboxId();
        profileId = profile().getId();
    }

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected boolean isMovingFinished;

    protected Criteria criteria;
    protected String noxboxId;
    protected String profileId;

    protected HashMap<String, String> movingNotificationData;
    protected Long lastNotificationUpdateTime;

    protected static Activity activity;
    protected static GoogleMap googleMap;

    @NonNull
    @Override
    public Result doWork() {
        return null;
    }

    protected void showMovingNotification(Position lastKnownPosition) {
        if (lastNotificationUpdateTime == null || lastNotificationUpdateTime + Constants.TIME_BETWEEN_NOTIFICATION_MOVING_UPDATES <= System.currentTimeMillis()) {
            lastNotificationUpdateTime = System.currentTimeMillis();
            movingNotificationData = new HashMap<>();
            movingNotificationData.put("type", NotificationType.moving.name());
            movingNotificationData.put("id", noxboxId);
            movingNotificationData.put("profileId", profileId);
            movingNotificationData.put("lat", lastKnownPosition.getLatitude() + "");
            movingNotificationData.put("lon", lastKnownPosition.getLongitude() + "");
            NotificationFactory.buildNotification(getApplicationContext(), null, movingNotificationData).setSilent(true).show();
        }
    }

    private static void runBackgroundLocationListener(Context context) {
        String TAG = BackgroundLocationListener.TAG;

        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(BackgroundLocationListener.class).setInitialDelay(2, TimeUnit.SECONDS).setConstraints(constraints).addTag(TAG).build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
    }

    private static void runForegroundLocationListener(Context context) {
        String TAG = ForegroundLocationListener.TAG;

        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(ForegroundLocationListener.class).setInitialDelay(2, TimeUnit.SECONDS).setConstraints(constraints).addTag(TAG).build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
    }

    private static void runBackgroundLocationReceiver(Context context) {
        String TAG = BackgroundLocationReceiver.TAG;
        Constraints constraints = new Constraints.Builder().setRequiresBatteryNotLow(true).setRequiredNetworkType(NetworkType.CONNECTED).build();
        OneTimeWorkRequest oneTimeWorkRequest = new OneTimeWorkRequest.Builder(BackgroundLocationReceiver.class).setInitialDelay(5, TimeUnit.SECONDS).setConstraints(constraints).addTag(TAG).build();
        WorkManager.getInstance(context).enqueueUniqueWork(TAG, ExistingWorkPolicy.KEEP, oneTimeWorkRequest);
    }


    public static void cancelWorkerByTag(Context context, String tag) {
        WorkManager.getInstance(context).cancelAllWorkByTag(tag);

        if (tag.equals(ForegroundLocationListener.TAG)) {
            ForegroundLocationListener.removeContextLinks();
        }
    }

    public static void runForegroundLocationWorker(Activity activity, GoogleMap googleMap, Profile profile, Task<Object> draw) {
        if (profile.getCurrent().getProfileWhoComes().equals(profile)) {
            cancelWorkerByTag(activity.getApplicationContext(), BackgroundLocationListener.TAG);
            provideContextLinks(activity, googleMap);
            runForegroundLocationListener(activity.getApplicationContext());
        } else if (profile.getCurrent().getProfileWhoWait().equals(profile)) {
            cancelWorkerByTag(activity.getApplicationContext(), BackgroundLocationReceiver.TAG);
            GeoRealtime.listenPosition(profile.getCurrent().getId(), position -> {
                Moving.memberWhoMovingPosition = position;
                draw.execute(null);
            });
        }
    }


    public static void runBackgroundLocationWorker(Context context) {
        Noxbox current = new Noxbox();
        current.copy(profile().getCurrent());

        Profile me = new Profile();
        me.copy(profile());

        if ((isNullOrZero(current.getTimeOwnerVerified()) || isNullOrZero(current.getTimePartyVerified()))
                && isNullOrZero(current.getTimeCanceledByOwner())
                && isNullOrZero(current.getTimeCanceledByParty())
                && isNullOrZero(current.getTimeOwnerRejected())
                && isNullOrZero(current.getTimePartyRejected())) {

            if (me.equals(current.getProfileWhoComes())) {
                runBackgroundLocationListener(context);
            } else if (me.equals(current.getProfileWhoWait())) {
                runBackgroundLocationReceiver(context);
            }
        }
    }
}
