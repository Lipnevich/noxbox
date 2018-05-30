package by.nicolay.lipnevich.noxbox.tools;

import com.google.firebase.iid.FirebaseInstanceIdService;

public class PushTokenService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        Firebase.refreshNotificationToken();
    }

}