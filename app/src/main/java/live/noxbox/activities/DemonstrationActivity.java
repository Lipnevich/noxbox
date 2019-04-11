package live.noxbox.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.debug.HackerActivity;
import live.noxbox.ui.ArrowView;
import live.noxbox.ui.RoleSwitcherLayout;

import static live.noxbox.Constants.FIRST_DEMONSTRATION_KEY;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;
import static live.noxbox.ui.ArrowView.BOTTOM_CENTER;
import static live.noxbox.ui.ArrowView.CENTER_END;
import static live.noxbox.ui.ArrowView.CENTER_START;
import static live.noxbox.ui.ArrowView.TOP_CENTER;

/**
 * Created by Vladislaw Kravchenok on 08.04.2019.
 */
public class DemonstrationActivity extends HackerActivity {

    private SharedPreferences demonstrationPreference;
    private RelativeLayout demonstration;


    protected boolean isFirstRunDemonstration() {
        if (demonstrationPreference == null) {
            demonstrationPreference = getApplicationContext().getSharedPreferences(FIRST_DEMONSTRATION_KEY, MODE_PRIVATE);
        }

        if (demonstrationPreference.getBoolean(FIRST_DEMONSTRATION_KEY, true)) {
            return true;
        } else {
            return false;
        }
    }

    private void onScreenClick() {
        if (demonstration != null) {
            demonstration.setVisibility(View.GONE);
        }
        if (demonstrationPreference != null) {
            demonstrationPreference.edit().putBoolean(FIRST_DEMONSTRATION_KEY, false).apply();
        }
        super.draw(this, profile());
    }

    @Override
    protected void onResume() {
        showDemonstration();
        super.onResume();
        Log.e("DemonstrationActivity", "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("DemonstrationActivity", "onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.e("DemonstrationActivity", "onStop");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e("DemonstrationActivity", "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.e("DemonstrationActivity", "onRestart");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        showDemonstration();
    }


    protected void showDemonstration() {
        if (isFirstRunDemonstration() || BuildConfig.DEBUG) {
            demonstration = findViewById(R.id.demonstration);
            demonstration.setVisibility(View.VISIBLE);
            demonstration.setOnClickListener(v -> onScreenClick());
            findViewById(R.id.menuDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.filtersDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.locationDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.contractDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.arrowMenuDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.arrowFiltersDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.arrowLocationDemonstration).setOnClickListener(v -> onScreenClick());
            findViewById(R.id.arrowContractDemonstration).setOnClickListener(v -> onScreenClick());

            ((RoleSwitcherLayout) findViewById(R.id.roleSwitcherDemonstration)).refresh();

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                RelativeLayout.LayoutParams textMenuParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textMenuParams.setMargins(dpToPx(24), dpToPx(126), 0, 0);
                TextView textMenuDemonstration = findViewById(R.id.textMenuDemonstration);
                textMenuDemonstration.setLayoutParams(textMenuParams);

                RelativeLayout.LayoutParams textMenuProfileDemonstrationParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuProfileDemonstrationParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstration);
                textMenuProfileDemonstrationParams.setMargins(dpToPx(24), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationProfile = findViewById(R.id.textMenuDemonstrationProfile);
                textMenuDemonstrationProfile.setLayoutParams(textMenuProfileDemonstrationParams);

                RelativeLayout.LayoutParams textMenuDemonstrationWalletParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuDemonstrationWalletParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstrationProfile);
                textMenuDemonstrationWalletParams.setMargins(dpToPx(24), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationWallet = findViewById(R.id.textMenuDemonstrationWallet);
                textMenuDemonstrationWallet.setLayoutParams(textMenuDemonstrationWalletParams);

                RelativeLayout.LayoutParams textSettingsParams = new RelativeLayout.LayoutParams(-2, -2);
                textSettingsParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstrationWallet);
                textSettingsParams.setMargins(dpToPx(24), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationSettings = findViewById(R.id.textMenuDemonstrationSettings);
                textMenuDemonstrationSettings.setLayoutParams(textSettingsParams);

                RelativeLayout.LayoutParams textFiltersParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstrationSettings);
                textFiltersParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersParams.setMargins(0, dpToPx(24), dpToPx(80), 0);
                TextView textFiltersDemonstration = findViewById(R.id.textFiltersDemonstration);
                textFiltersDemonstration.setLayoutParams(textFiltersParams);

                RelativeLayout.LayoutParams textFiltersDescriptionParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersDescriptionParams.addRule(RelativeLayout.BELOW, R.id.textFiltersDemonstration);
                textFiltersDescriptionParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersDescriptionParams.setMargins(0, 0, dpToPx(80), 0);
                TextView textFiltersDemonstrationDescription = findViewById(R.id.textFiltersDemonstrationDescription);
                textFiltersDemonstrationDescription.setLayoutParams(textFiltersDescriptionParams);

                RelativeLayout.LayoutParams textLocationParams = new RelativeLayout.LayoutParams(-2, -2);
                textLocationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textLocationParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textLocationParams.setMargins(dpToPx(56), 0, dpToPx(0), dpToPx(128));
                TextView textLocationDemonstration = findViewById(R.id.textLocationDemonstration);
                textLocationDemonstration.setLayoutParams(textLocationParams);

                RelativeLayout.LayoutParams textContractParams = new RelativeLayout.LayoutParams(-2, -2);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textContractParams.setMargins(dpToPx(24), 0, dpToPx(24), dpToPx(208));
                TextView textContractDemonstration = findViewById(R.id.textContractDemonstration);
                textContractDemonstration.setLayoutParams(textContractParams);

                ArrowView arrowMenuDemonstration = findViewById(R.id.arrowMenuDemonstration);
                ArrowView arrowFiltersDemonstration = findViewById(R.id.arrowFiltersDemonstration);
                ArrowView arrowLocationDemonstration = findViewById(R.id.arrowLocationDemonstration);
                ArrowView arrowContractDemonstration = findViewById(R.id.arrowContractDemonstration);
                ArrowView arrowRoleSwitcherDemonstration = findViewById(R.id.arrowRoleSwitcherDemonstration);
                arrowMenuDemonstration.setWillNotDraw(false);
                arrowMenuDemonstration.setStartView(findViewById(R.id.textMenuDemonstration));
                arrowMenuDemonstration.setEndView(findViewById(R.id.menuDemonstration));
                arrowMenuDemonstration.invalidate(TOP_CENTER, BOTTOM_CENTER);

                arrowFiltersDemonstration.setWillNotDraw(false);
                arrowFiltersDemonstration.setStartView(findViewById(R.id.textFiltersDemonstration));
                arrowFiltersDemonstration.setEndView(findViewById(R.id.filtersDemonstration));
                arrowFiltersDemonstration.invalidate(CENTER_END, BOTTOM_CENTER);

                arrowLocationDemonstration.setWillNotDraw(false);
                arrowLocationDemonstration.setStartView(findViewById(R.id.textLocationDemonstration));
                arrowLocationDemonstration.setEndView(findViewById(R.id.locationDemonstration));
                arrowLocationDemonstration.invalidate(BOTTOM_CENTER, TOP_CENTER);

                arrowContractDemonstration.setWillNotDraw(false);
                arrowContractDemonstration.setStartView(findViewById(R.id.textContractDemonstration));
                arrowContractDemonstration.setEndView(findViewById(R.id.contractDemonstration));
                arrowContractDemonstration.invalidate(BOTTOM_CENTER, TOP_CENTER);

                arrowRoleSwitcherDemonstration.setWillNotDraw(false);
                arrowRoleSwitcherDemonstration.setStartView(findViewById(R.id.textFiltersDemonstration));
                arrowRoleSwitcherDemonstration.setEndView(findViewById(R.id.roleSwitcherDemonstration));
                arrowRoleSwitcherDemonstration.invalidate(CENTER_END, BOTTOM_CENTER, false);

            } else {
                RelativeLayout.LayoutParams textMenuParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                textMenuParams.setMargins(dpToPx(126), dpToPx(24), 0, 0);
                TextView textMenuDemonstration = findViewById(R.id.textMenuDemonstration);
                textMenuDemonstration.setLayoutParams(textMenuParams);

                RelativeLayout.LayoutParams textMenuProfileDemonstrationParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuProfileDemonstrationParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstration);
                textMenuProfileDemonstrationParams.setMargins(dpToPx(126), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationProfile = findViewById(R.id.textMenuDemonstrationProfile);
                textMenuDemonstrationProfile.setLayoutParams(textMenuProfileDemonstrationParams);

                RelativeLayout.LayoutParams textMenuDemonstrationWalletParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuDemonstrationWalletParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstrationProfile);
                textMenuDemonstrationWalletParams.setMargins(dpToPx(126), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationWallet = findViewById(R.id.textMenuDemonstrationWallet);
                textMenuDemonstrationWallet.setLayoutParams(textMenuDemonstrationWalletParams);

                RelativeLayout.LayoutParams textSettingsParams = new RelativeLayout.LayoutParams(-2, -2);
                textSettingsParams.addRule(RelativeLayout.BELOW, R.id.textMenuDemonstrationWallet);
                textSettingsParams.setMargins(dpToPx(126), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationSettings = findViewById(R.id.textMenuDemonstrationSettings);
                textMenuDemonstrationSettings.setLayoutParams(textSettingsParams);

                RelativeLayout.LayoutParams textFiltersParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersParams.setMargins(dpToPx(0), dpToPx(102), dpToPx(134), 0);
                TextView textFiltersDemonstration = findViewById(R.id.textFiltersDemonstration);
                textFiltersDemonstration.setLayoutParams(textFiltersParams);

                RelativeLayout.LayoutParams textFiltersDescriptionParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersDescriptionParams.addRule(RelativeLayout.BELOW, R.id.textFiltersDemonstration);
                textFiltersDescriptionParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersDescriptionParams.setMargins(dpToPx(0), dpToPx(16), dpToPx(134), 0);
                TextView textFiltersDemonstrationDescription = findViewById(R.id.textFiltersDemonstrationDescription);
                textFiltersDemonstrationDescription.setLayoutParams(textFiltersDescriptionParams);

                RelativeLayout.LayoutParams textLocationParams = new RelativeLayout.LayoutParams(-2, -2);
                textLocationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textLocationParams.setMargins(dpToPx(126), 0, dpToPx(0), dpToPx(44));
                TextView textLocationDemonstration = findViewById(R.id.textLocationDemonstration);
                textLocationDemonstration.setLayoutParams(textLocationParams);

                RelativeLayout.LayoutParams textContractParams = new RelativeLayout.LayoutParams(-2, -2);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textContractParams.setMargins(0, 0, dpToPx(184), dpToPx(80));
                TextView textContractDemonstration = findViewById(R.id.textContractDemonstration);
                textContractDemonstration.setLayoutParams(textContractParams);

                ArrowView arrowMenuDemonstration = findViewById(R.id.arrowMenuDemonstration);
                ArrowView arrowFiltersDemonstration = findViewById(R.id.arrowFiltersDemonstration);
                ArrowView arrowLocationDemonstration = findViewById(R.id.arrowLocationDemonstration);
                ArrowView arrowContractDemonstration = findViewById(R.id.arrowContractDemonstration);
                ArrowView arrowRoleSwitcherDemonstration = findViewById(R.id.arrowRoleSwitcherDemonstration);
                arrowMenuDemonstration.setWillNotDraw(false);
                arrowMenuDemonstration.setStartView(textMenuDemonstration);
                arrowMenuDemonstration.setEndView(findViewById(R.id.menuDemonstration));
                arrowMenuDemonstration.invalidate(CENTER_START, CENTER_END);

                arrowFiltersDemonstration.setWillNotDraw(false);
                arrowFiltersDemonstration.setStartView(textFiltersDemonstration);
                arrowFiltersDemonstration.setEndView(findViewById(R.id.filtersDemonstration));
                arrowFiltersDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowLocationDemonstration.setWillNotDraw(false);
                arrowLocationDemonstration.setStartView(textLocationDemonstration);
                arrowLocationDemonstration.setEndView(findViewById(R.id.locationDemonstration));
                arrowLocationDemonstration.invalidate(CENTER_START, CENTER_END);

                arrowContractDemonstration.setWillNotDraw(false);
                arrowContractDemonstration.setStartView(textContractDemonstration);
                arrowContractDemonstration.setEndView(findViewById(R.id.contractDemonstration));
                arrowContractDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowRoleSwitcherDemonstration.setWillNotDraw(false);
                arrowRoleSwitcherDemonstration.setStartView(findViewById(R.id.textFiltersDemonstration));
                arrowRoleSwitcherDemonstration.setEndView(findViewById(R.id.roleSwitcherDemonstration));
                arrowRoleSwitcherDemonstration.invalidate(CENTER_START, BOTTOM_CENTER, false);
            }
        }
    }
}
