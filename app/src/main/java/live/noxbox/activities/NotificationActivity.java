package live.noxbox.activities;

import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.NotificationType;
import live.noxbox.notifications.factory.NotificationFactory;
import live.noxbox.services.MessagingService;

/**
 * Created by Vladislaw Kravchenok on 02.04.2019.
 */
public class NotificationActivity extends BusinessActivity {

    @Override
    protected void onResume() {
        super.onResume();
        MessagingService.removeNotifications(getApplicationContext());
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.readProfile(profile -> {
            NotificationFactory.buildNotification(getApplicationContext(), profile, NotificationType.fromNoxboxState(profile)).setSilent(true).show();
        });

    }
}
