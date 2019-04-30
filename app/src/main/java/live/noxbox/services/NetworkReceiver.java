package live.noxbox.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;

import live.noxbox.database.AppCache;

import static live.noxbox.services.ConnectivityManager.hideOffline;
import static live.noxbox.services.ConnectivityManager.showOffline;

public class NetworkReceiver extends BroadcastReceiver {

    private Activity activity;

    public NetworkReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if(isOnline(activity)){
            hideOffline();
            AppCache.startListening();
        }else{
            showOffline(activity);
        }
    }
    public static boolean isOnline(Activity activity) {
        ConnectivityManager cm
                = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }
}
