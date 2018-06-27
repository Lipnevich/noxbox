package by.nicolay.lipnevich.noxbox;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;

import by.nicolay.lipnevich.noxbox.pages.NetworkReceiver;
import io.fabric.sdk.android.Fabric;

public class App extends Application implements Application.ActivityLifecycleCallbacks {
    private BroadcastReceiver networkReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        initCrashReporting();
        registerActivityLifecycleCallbacks(this);
    }
    private void initCrashReporting() {
        CrashlyticsCore crashlyticsCore = new CrashlyticsCore.Builder()
                .disabled(BuildConfig.DEBUG)
                .build();
        Fabric.with(this, new Crashlytics.Builder().core(crashlyticsCore).build());
    }
    @Override
    public void onActivityResumed(Activity activity) {
        networkReceiver = new NetworkReceiver(activity);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    public void onActivityPaused(Activity activity) {
        unregisterReceiver(networkReceiver);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
