package live.noxbox.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class ReferrerCatcher extends BroadcastReceiver {


    public final static String KEY = "referrer";
    public static String referrer;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            referrer = extras.getString(KEY);
        }
    }

    public static void clearReferrer() {
        referrer = null;
    }


}
