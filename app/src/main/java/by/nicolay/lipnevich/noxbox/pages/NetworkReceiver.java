package by.nicolay.lipnevich.noxbox.pages;

import android.app.Activity;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.tools.FragmentManager;

public class NetworkReceiver extends BroadcastReceiver {

    private Activity activity;
    private Fragment fragment;

    public NetworkReceiver(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo wifi = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            NetworkInfo mobile = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifi.isConnectedOrConnecting() || mobile.isConnectedOrConnecting()) {
                if (fragment != null) {
                    FragmentManager.removeFragment(activity, fragment);
                }
            } else {
                if(fragment == null){
                    fragment = new WarningFragmemt();
                }
                FragmentManager.createFragment(activity,fragment, R.id.messageContainer);
            }
        }
    }

}
