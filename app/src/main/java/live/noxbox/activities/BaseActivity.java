package live.noxbox.activities;

import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.view.MenuItem;
import android.view.View;

import com.crashlytics.android.Crashlytics;

import live.noxbox.analitics.BusinessActivity;
import live.noxbox.services.NetworkReceiver;
import live.noxbox.tools.ProgressDialogFragment;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.FIRST_RUN_KEY;
import static live.noxbox.tools.BalanceChecker.cancelBalanceUpdate;

public abstract class BaseActivity extends BusinessActivity {
    protected BroadcastReceiver networkReceiver;

    private SharedPreferences firstRunPreference;


    @Override
    protected void onResume() {
        super.onResume();
        networkReceiver = new NetworkReceiver(this);
        registerReceiver(networkReceiver, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
    }

    @Override
    protected void onStop() {
        super.onStop();
        cancelBalanceUpdate();
        clearProgressDialog();
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

    protected boolean isFirstRun(boolean update) {
        if (firstRunPreference == null) {
            firstRunPreference = getApplicationContext().getSharedPreferences(FIRST_RUN_KEY, MODE_PRIVATE);
        }

        if (firstRunPreference.getBoolean(FIRST_RUN_KEY, true)) {
            if (update) {
                firstRunPreference.edit().putBoolean(FIRST_RUN_KEY, false).apply();
            }
            return true;
        } else {
            return false;
        }
    }

    private ProgressDialogFragment dialogFragment;
    private View shadowed;

    protected void showProgressDialog(View shadowed, String key) {
        if (dialogFragment != null) {
            clearProgressDialog();
        }
        this.dialogFragment = new ProgressDialogFragment();
        this.shadowed = shadowed;

        dialogFragment.setCancelable(false);
        dialogFragment.show(getSupportFragmentManager(), key);
        shadowed.setAlpha(0.3f);
    }

    protected void clearProgressDialog() {
        if (dialogFragment == null) return;

        if (!dialogFragment.isCancelable()) {
            dialogFragment.setCancelable(true);
        }
        if (dialogFragment.isVisible()) {
            dialogFragment.dismiss();
        }
        if (shadowed.getAlpha() != 1.0f) {
            shadowed.setAlpha(1.0f);
        }
        dialogFragment = null;
        shadowed = null;
    }


}
