package live.noxbox.activities;

import android.content.Intent;

import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.services.MessagingService;
import live.noxbox.services.NotificationService;
import live.noxbox.tools.ServiceMonitoring;

import static live.noxbox.database.AppCache.profile;

/**
 * Created by Vladislaw Kravchenok on 02.04.2019.
 */
public class NotificationActivity extends BusinessActivity {

    @Override
    protected void onResume() {
        super.onResume();
        if(ServiceMonitoring.isMyServiceRunning(NotificationService.class, getApplicationContext())){
            stopService(new Intent(getApplicationContext(), NotificationService.class));
        }
        MessagingService.removeNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (AppCache.isProfileReady()
                && profile().getCurrent().getTimeRequested() != null
                && !profile().getCurrent().getFinished()
                && !ServiceMonitoring.isMyServiceRunning(NotificationService.class, getApplicationContext())) {
            startService(new Intent(getApplicationContext(), NotificationService.class));
        }

    }
}
