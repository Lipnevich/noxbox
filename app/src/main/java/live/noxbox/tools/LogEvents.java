package live.noxbox.tools;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class LogEvents {

    private static FirebaseAnalytics firebaseAnalytics;

    public static void generateLogEvent(Context context, String nameOfEvent) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(context);
        Bundle bundle = new Bundle();
        bundle.putString(nameOfEvent, nameOfEvent);
        firebaseAnalytics.logEvent(nameOfEvent, bundle);
    }
}
