package live.noxbox.tools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import live.noxbox.analitics.BusinessActivity;

public class ReferrerCatcher extends BroadcastReceiver {


    public final static String KEY = "referrer";
    public final static String SPLITTER = "Campaign_code_";
    public static String referrer;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null && extras.getString(KEY) != null) {
            String [] values = extras.getString(KEY).split(SPLITTER);
            referrer = values[0];
            if(values.length > 1) {
                BusinessActivity.businessEvent(values[1]);
            }
        }
    }

    public static void clearReferrer() {
        referrer = null;
    }


}
