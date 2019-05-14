package live.noxbox.tools.location.moving;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import live.noxbox.database.GeoRealtime;
import live.noxbox.notifications.NotificationMoving;

/**
 * Created by Vladislaw Kravchenok on 14.05.2019.
 */
public class BackgroundLocationReceiver extends MovingWorker {
    public BackgroundLocationReceiver(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    public static final String TAG = "BackgroundLocationReceiver";

    @NonNull
    @Override
    public Result doWork() {
        GeoRealtime.listenPosition(noxboxId, position -> {
            if (movingNotification != null && movingNotification instanceof NotificationMoving) {
                updateMovingNotification(position);
            } else {
                showMovingNotification(position);
            }
        });
        return Result.success();
    }


}
