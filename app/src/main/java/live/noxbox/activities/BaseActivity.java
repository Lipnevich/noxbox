package live.noxbox.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.MenuItem;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.Collections;
import java.util.Map;

import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.Firestore;
import live.noxbox.services.NetworkReceiver;
import live.noxbox.tools.Router;

import static live.noxbox.tools.BalanceChecker.cancelBalanceUpdate;

public abstract class BaseActivity extends BusinessActivity {
    protected BroadcastReceiver networkReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();

        writes = getSharedPreferences("writes", Context.MODE_PRIVATE);
        reads = getSharedPreferences("reads", Context.MODE_PRIVATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        networkReceiver = new NetworkReceiver(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();

        SharedPreferences writes = getApplicationContext().getSharedPreferences("writes", MODE_PRIVATE);
        writes.edit().putLong("writes", writes.getLong("writes", 0L) + Firestore.writes).apply();
        Firestore.writes = 0L;

        SharedPreferences reads = getApplicationContext().getSharedPreferences("reads", MODE_PRIVATE);
        reads.edit().putLong("reads", reads.getLong("reads", 0L) + Firestore.reads).apply();
        Firestore.reads = 0L;

        cancelBalanceUpdate();
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                Router.finishActivity(this);
                break;
        }
        return true;
    }

    public static void businessEvent(String name) {
        businessEvent(name, Collections.EMPTY_MAP);
    }

    public static void businessEvent(String name, Map<String, String> params) {
        if(context == null) return;

        Bundle bundle = new Bundle();
        bundle.putString(name, name);
        for (Map.Entry<String, String> param : params.entrySet()) {
            bundle.putString(param.getKey(), param.getValue());
        }
        FirebaseAnalytics.getInstance(context).logEvent(name, bundle);
    }
}
