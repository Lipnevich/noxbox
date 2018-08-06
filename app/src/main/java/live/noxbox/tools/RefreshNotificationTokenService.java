package live.noxbox.tools;

import com.google.firebase.iid.FirebaseInstanceIdService;

import live.noxbox.state.Firebase;

public class RefreshNotificationTokenService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        Firebase.refreshNotificationToken();
    }

}