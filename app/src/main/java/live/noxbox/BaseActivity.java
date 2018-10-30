package live.noxbox;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.support.v7.app.AppCompatActivity;

import com.crashlytics.android.Crashlytics;

import live.noxbox.pages.NetworkReceiver;

public abstract class BaseActivity extends AppCompatActivity {
    protected BroadcastReceiver networkReceiver;
    @Override
    protected void onResume() {
        super.onResume();
        networkReceiver = new NetworkReceiver(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(networkReceiver);
        } catch (IllegalArgumentException e) {
            Crashlytics.logException(e);
        }
    }
}
