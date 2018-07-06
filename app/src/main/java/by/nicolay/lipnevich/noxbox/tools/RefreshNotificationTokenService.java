package by.nicolay.lipnevich.noxbox.tools;

import by.nicolay.lipnevich.noxbox.state.Firebase;
import com.google.firebase.iid.FirebaseInstanceIdService;

public class RefreshNotificationTokenService extends FirebaseInstanceIdService {
    @Override
    public void onTokenRefresh() {
        Firebase.refreshNotificationToken();
    }

}