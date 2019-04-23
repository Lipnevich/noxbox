package live.noxbox.activities;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import live.noxbox.R;
import live.noxbox.debug.HackerActivity;
import live.noxbox.ui.ArrowView;
import live.noxbox.ui.RoleSwitcherLayout;

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
    public static final String AVAILABLE_DEMONSTRATION_KEY = "AVAILABLE_DEMONSTRATION";
    public static final String MOVING_DEMONSTRATION_KEY = "MOVING_DEMONSTRATION";
    private SharedPreferences demonstrationPreference;
    private RelativeLayout demonstration;


    protected boolean wasDemonstrationShowed(String key) {
        if (demonstrationPreference == null) {
            demonstrationPreference = getApplicationContext().getSharedPreferences(key, MODE_PRIVATE);
        }

        if (demonstrationPreference.getBoolean(key, true)) {
            return true;
        } else {
            return false;
        }
    }

    private void onScreenClick(String key) {
        if (demonstration != null) {
            demonstration.setVisibility(View.GONE);
        }
        if (demonstrationPreference != null) {
            demonstrationPreference.edit().putBoolean(key, false).apply();
        }
        super.draw(this, profile());
    }

    protected void showAvailableDemonstration() {
        if (wasDemonstrationShowed(AVAILABLE_DEMONSTRATION_KEY)) {
            demonstration = findViewById(R.id.demonstrationAvailable);
            demonstration.setVisibility(View.VISIBLE);
            demonstration.setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.menuAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.filtersAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.locationAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.contractAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.arrowMenuAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.arrowFiltersAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.arrowLocationAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));
            findViewById(R.id.arrowContractAvailableDemonstration).setOnClickListener(v -> onScreenClick(AVAILABLE_DEMONSTRATION_KEY));

            ((RoleSwitcherLayout) findViewById(R.id.roleSwitcherAvailableDemonstration)).refresh();

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                RelativeLayout.LayoutParams textMenuParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textMenuParams.setMargins(dpToPx(24), dpToPx(126), 0, 0);
                TextView textMenuDemonstration = findViewById(R.id.textMenuAvailableDemonstration);
                textMenuDemonstration.setLayoutParams(textMenuParams);

                RelativeLayout.LayoutParams textMenuProfileDemonstrationParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuProfileDemonstrationParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstration);
                textMenuProfileDemonstrationParams.setMargins(dpToPx(24), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationProfile = findViewById(R.id.textMenuAvailableDemonstrationProfile);
                textMenuDemonstrationProfile.setLayoutParams(textMenuProfileDemonstrationParams);

                RelativeLayout.LayoutParams textMenuDemonstrationWalletParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuDemonstrationWalletParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstrationProfile);
                textMenuDemonstrationWalletParams.setMargins(dpToPx(24), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationWallet = findViewById(R.id.textMenuAvailableDemonstrationWallet);
                textMenuDemonstrationWallet.setLayoutParams(textMenuDemonstrationWalletParams);

                RelativeLayout.LayoutParams textSettingsParams = new RelativeLayout.LayoutParams(-2, -2);
                textSettingsParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstrationWallet);
                textSettingsParams.setMargins(dpToPx(24), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationSettings = findViewById(R.id.textMenuAvailableDemonstrationSettings);
                textMenuDemonstrationSettings.setLayoutParams(textSettingsParams);

                RelativeLayout.LayoutParams textFiltersParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstrationSettings);
                textFiltersParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersParams.setMargins(0, dpToPx(24), dpToPx(80), 0);
                TextView textFiltersDemonstration = findViewById(R.id.textFiltersAvailableDemonstration);
                textFiltersDemonstration.setLayoutParams(textFiltersParams);

                RelativeLayout.LayoutParams textFiltersDescriptionParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersDescriptionParams.addRule(RelativeLayout.BELOW, R.id.textFiltersAvailableDemonstration);
                textFiltersDescriptionParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersDescriptionParams.setMargins(0, 0, dpToPx(80), 0);
                TextView textFiltersDemonstrationDescription = findViewById(R.id.textFiltersAvailableDemonstrationDescription);
                textFiltersDemonstrationDescription.setLayoutParams(textFiltersDescriptionParams);

                RelativeLayout.LayoutParams textLocationParams = new RelativeLayout.LayoutParams(-2, -2);
                textLocationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textLocationParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textLocationParams.setMargins(dpToPx(56), 0, dpToPx(0), dpToPx(128));
                TextView textLocationDemonstration = findViewById(R.id.textLocationAvailableDemonstration);
                textLocationDemonstration.setLayoutParams(textLocationParams);

                RelativeLayout.LayoutParams textContractParams = new RelativeLayout.LayoutParams(-2, -2);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textContractParams.setMargins(dpToPx(24), 0, dpToPx(24), dpToPx(208));
                TextView textContractDemonstration = findViewById(R.id.textContractAvailableDemonstration);
                textContractDemonstration.setLayoutParams(textContractParams);

                ArrowView arrowMenuDemonstration = findViewById(R.id.arrowMenuAvailableDemonstration);
                ArrowView arrowFiltersDemonstration = findViewById(R.id.arrowFiltersAvailableDemonstration);
                ArrowView arrowLocationDemonstration = findViewById(R.id.arrowLocationAvailableDemonstration);
                ArrowView arrowContractDemonstration = findViewById(R.id.arrowContractAvailableDemonstration);
                ArrowView arrowRoleSwitcherDemonstration = findViewById(R.id.arrowRoleSwitcherAvailableDemonstration);
                arrowMenuDemonstration.setWillNotDraw(false);
                arrowMenuDemonstration.setStartView(findViewById(R.id.textMenuAvailableDemonstration));
                arrowMenuDemonstration.setEndView(findViewById(R.id.menuAvailableDemonstration));
                arrowMenuDemonstration.invalidate(TOP_CENTER, BOTTOM_CENTER);

                arrowFiltersDemonstration.setWillNotDraw(false);
                arrowFiltersDemonstration.setStartView(findViewById(R.id.textFiltersAvailableDemonstration));
                arrowFiltersDemonstration.setEndView(findViewById(R.id.filtersAvailableDemonstration));
                arrowFiltersDemonstration.invalidate(CENTER_END, BOTTOM_CENTER);

                arrowLocationDemonstration.setWillNotDraw(false);
                arrowLocationDemonstration.setStartView(findViewById(R.id.textLocationAvailableDemonstration));
                arrowLocationDemonstration.setEndView(findViewById(R.id.locationAvailableDemonstration));
                arrowLocationDemonstration.invalidate(BOTTOM_CENTER, TOP_CENTER);

                arrowContractDemonstration.setWillNotDraw(false);
                arrowContractDemonstration.setStartView(findViewById(R.id.textContractAvailableDemonstration));
                arrowContractDemonstration.setEndView(findViewById(R.id.contractAvailableDemonstration));
                arrowContractDemonstration.invalidate(BOTTOM_CENTER, TOP_CENTER);

                arrowRoleSwitcherDemonstration.setWillNotDraw(false);
                arrowRoleSwitcherDemonstration.setStartView(findViewById(R.id.textFiltersAvailableDemonstration));
                arrowRoleSwitcherDemonstration.setEndView(findViewById(R.id.roleSwitcherAvailableDemonstration));
                arrowRoleSwitcherDemonstration.invalidate(CENTER_END, BOTTOM_CENTER, false);

            } else {
                RelativeLayout.LayoutParams textMenuParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                textMenuParams.setMargins(dpToPx(126), dpToPx(24), 0, 0);
                TextView textMenuDemonstration = findViewById(R.id.textMenuAvailableDemonstration);
                textMenuDemonstration.setLayoutParams(textMenuParams);

                RelativeLayout.LayoutParams textMenuProfileDemonstrationParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuProfileDemonstrationParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstration);
                textMenuProfileDemonstrationParams.setMargins(dpToPx(126), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationProfile = findViewById(R.id.textMenuAvailableDemonstrationProfile);
                textMenuDemonstrationProfile.setLayoutParams(textMenuProfileDemonstrationParams);

                RelativeLayout.LayoutParams textMenuDemonstrationWalletParams = new RelativeLayout.LayoutParams(-2, -2);
                textMenuDemonstrationWalletParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstrationProfile);
                textMenuDemonstrationWalletParams.setMargins(dpToPx(126), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationWallet = findViewById(R.id.textMenuAvailableDemonstrationWallet);
                textMenuDemonstrationWallet.setLayoutParams(textMenuDemonstrationWalletParams);

                RelativeLayout.LayoutParams textSettingsParams = new RelativeLayout.LayoutParams(-2, -2);
                textSettingsParams.addRule(RelativeLayout.BELOW, R.id.textMenuAvailableDemonstrationWallet);
                textSettingsParams.setMargins(dpToPx(126), dpToPx(16), 0, 0);
                TextView textMenuDemonstrationSettings = findViewById(R.id.textMenuAvailableDemonstrationSettings);
                textMenuDemonstrationSettings.setLayoutParams(textSettingsParams);

                RelativeLayout.LayoutParams textFiltersParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersParams.setMargins(dpToPx(0), dpToPx(102), dpToPx(134), 0);
                TextView textFiltersDemonstration = findViewById(R.id.textFiltersAvailableDemonstration);
                textFiltersDemonstration.setLayoutParams(textFiltersParams);

                RelativeLayout.LayoutParams textFiltersDescriptionParams = new RelativeLayout.LayoutParams(-2, -2);
                textFiltersDescriptionParams.addRule(RelativeLayout.BELOW, R.id.textFiltersAvailableDemonstration);
                textFiltersDescriptionParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textFiltersDescriptionParams.setMargins(dpToPx(0), dpToPx(16), dpToPx(134), 0);
                TextView textFiltersDemonstrationDescription = findViewById(R.id.textFiltersAvailableDemonstrationDescription);
                textFiltersDemonstrationDescription.setLayoutParams(textFiltersDescriptionParams);

                RelativeLayout.LayoutParams textLocationParams = new RelativeLayout.LayoutParams(-2, -2);
                textLocationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textLocationParams.setMargins(dpToPx(126), 0, dpToPx(0), dpToPx(44));
                TextView textLocationDemonstration = findViewById(R.id.textLocationAvailableDemonstration);
                textLocationDemonstration.setLayoutParams(textLocationParams);

                RelativeLayout.LayoutParams textContractParams = new RelativeLayout.LayoutParams(-2, -2);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textContractParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                textContractParams.setMargins(0, 0, dpToPx(184), dpToPx(80));
                TextView textContractDemonstration = findViewById(R.id.textContractAvailableDemonstration);
                textContractDemonstration.setLayoutParams(textContractParams);

                ArrowView arrowMenuDemonstration = findViewById(R.id.arrowMenuAvailableDemonstration);
                ArrowView arrowFiltersDemonstration = findViewById(R.id.arrowFiltersAvailableDemonstration);
                ArrowView arrowLocationDemonstration = findViewById(R.id.arrowLocationAvailableDemonstration);
                ArrowView arrowContractDemonstration = findViewById(R.id.arrowContractAvailableDemonstration);
                ArrowView arrowRoleSwitcherDemonstration = findViewById(R.id.arrowRoleSwitcherAvailableDemonstration);
                arrowMenuDemonstration.setWillNotDraw(false);
                arrowMenuDemonstration.setStartView(textMenuDemonstration);
                arrowMenuDemonstration.setEndView(findViewById(R.id.menuAvailableDemonstration));
                arrowMenuDemonstration.invalidate(CENTER_START, CENTER_END);

                arrowFiltersDemonstration.setWillNotDraw(false);
                arrowFiltersDemonstration.setStartView(textFiltersDemonstration);
                arrowFiltersDemonstration.setEndView(findViewById(R.id.filtersAvailableDemonstration));
                arrowFiltersDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowLocationDemonstration.setWillNotDraw(false);
                arrowLocationDemonstration.setStartView(textLocationDemonstration);
                arrowLocationDemonstration.setEndView(findViewById(R.id.locationAvailableDemonstration));
                arrowLocationDemonstration.invalidate(CENTER_START, CENTER_END);

                arrowContractDemonstration.setWillNotDraw(false);
                arrowContractDemonstration.setStartView(textContractDemonstration);
                arrowContractDemonstration.setEndView(findViewById(R.id.contractAvailableDemonstration));
                arrowContractDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowRoleSwitcherDemonstration.setWillNotDraw(false);
                arrowRoleSwitcherDemonstration.setStartView(findViewById(R.id.textFiltersAvailableDemonstration));
                arrowRoleSwitcherDemonstration.setEndView(findViewById(R.id.roleSwitcherAvailableDemonstration));
                arrowRoleSwitcherDemonstration.invalidate(CENTER_START, BOTTOM_CENTER, false);
            }
        }
    }

    protected void showMovingDemonstration() {
        if (wasDemonstrationShowed(MOVING_DEMONSTRATION_KEY)) {
            demonstration = findViewById(R.id.demonstrationMoving);
            demonstration.setVisibility(View.VISIBLE);
            demonstration.setOnClickListener(v -> onScreenClick(MOVING_DEMONSTRATION_KEY));
            findViewById(R.id.chatMovingDemonstration).setOnClickListener(v -> onScreenClick(MOVING_DEMONSTRATION_KEY));
            findViewById(R.id.navigatorMovingDemonstration).setOnClickListener(v -> onScreenClick(MOVING_DEMONSTRATION_KEY));
            findViewById(R.id.verificationMovingDemonstration).setOnClickListener(v -> onScreenClick(MOVING_DEMONSTRATION_KEY));

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                RelativeLayout.LayoutParams textChatParams = new RelativeLayout.LayoutParams(-2, -2);
                textChatParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textChatParams.setMargins(dpToPx(24), dpToPx(126), 0, 0);
                TextView textChatDemonstration = findViewById(R.id.textChatMovingDemonstration);
                textChatDemonstration.setLayoutParams(textChatParams);

                RelativeLayout.LayoutParams textNavigatorParams = new RelativeLayout.LayoutParams(-2, -2);
                textNavigatorParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textNavigatorParams.addRule(RelativeLayout.BELOW, R.id.textChatMovingDemonstration);
                textNavigatorParams.setMargins(dpToPx(38), dpToPx(68), dpToPx(0), dpToPx(0));
                TextView textNavigatorDemonstration = findViewById(R.id.textNavigatorMovingDemonstration);
                textNavigatorDemonstration.setLayoutParams(textNavigatorParams);

                RelativeLayout.LayoutParams textVerificationParams = new RelativeLayout.LayoutParams(-2, -2);
                textVerificationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textVerificationParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textVerificationParams.setMargins(dpToPx(22), 0, dpToPx(0), dpToPx(126));
                TextView textVerificationDemonstration = findViewById(R.id.textVerificationMovingDemonstration);
                textVerificationDemonstration.setLayoutParams(textVerificationParams);

                ArrowView arrowChatDemonstration = findViewById(R.id.arrowChatMovingDemonstration);
                ArrowView arrowNavigatorDemonstration = findViewById(R.id.arrowNavigatorMovingDemonstration);
                ArrowView arrowVerificationDemonstration = findViewById(R.id.arrowVerificationMovingDemonstration);
                arrowChatDemonstration.setWillNotDraw(false);
                arrowChatDemonstration.setStartView(findViewById(R.id.textChatMovingDemonstration));
                arrowChatDemonstration.setEndView(findViewById(R.id.chatMovingDemonstration));
                arrowChatDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowNavigatorDemonstration.setWillNotDraw(false);
                arrowNavigatorDemonstration.setStartView(findViewById(R.id.textNavigatorMovingDemonstration));
                arrowNavigatorDemonstration.setEndView(findViewById(R.id.navigatorMovingDemonstration));
                arrowNavigatorDemonstration.invalidate(BOTTOM_CENTER, CENTER_START);

                arrowVerificationDemonstration.setWillNotDraw(false);
                arrowVerificationDemonstration.setStartView(findViewById(R.id.textVerificationMovingDemonstration));
                arrowVerificationDemonstration.setEndView(findViewById(R.id.verificationMovingDemonstration));
                arrowVerificationDemonstration.invalidate(BOTTOM_CENTER, CENTER_START);

            } else {
                RelativeLayout.LayoutParams textChatParams = new RelativeLayout.LayoutParams(-2, -2);
                textChatParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                textChatParams.setMargins(dpToPx(0), dpToPx(90), 0, 0);
                TextView textChatDemonstration = findViewById(R.id.textChatMovingDemonstration);
                textChatDemonstration.setLayoutParams(textChatParams);

                RelativeLayout.LayoutParams textNavigatorParams = new RelativeLayout.LayoutParams(-2, -2);
                textNavigatorParams.addRule(RelativeLayout.CENTER_VERTICAL);
                textNavigatorParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
                textNavigatorParams.setMargins(dpToPx(0), dpToPx(0), dpToPx(0), dpToPx(0));
                TextView textNavigatorDemonstration = findViewById(R.id.textNavigatorMovingDemonstration);
                textNavigatorDemonstration.setLayoutParams(textNavigatorParams);

                RelativeLayout.LayoutParams textVerificationParams = new RelativeLayout.LayoutParams(-2, -2);
                textVerificationParams.addRule(RelativeLayout.ALIGN_PARENT_START);
                textVerificationParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                textVerificationParams.setMargins(dpToPx(90), dpToPx(0), dpToPx(0), dpToPx(90));
                TextView textVerificationDemonstration = findViewById(R.id.textVerificationMovingDemonstration);
                textVerificationDemonstration.setLayoutParams(textVerificationParams);

                ArrowView arrowChatDemonstration = findViewById(R.id.arrowChatMovingDemonstration);
                ArrowView arrowNavigatorDemonstration = findViewById(R.id.arrowNavigatorMovingDemonstration);
                ArrowView arrowVerificationDemonstration = findViewById(R.id.arrowVerificationMovingDemonstration);
                arrowChatDemonstration.setWillNotDraw(false);
                arrowChatDemonstration.setStartView(findViewById(R.id.textChatMovingDemonstration));
                arrowChatDemonstration.setEndView(findViewById(R.id.chatMovingDemonstration));
                arrowChatDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowNavigatorDemonstration.setWillNotDraw(false);
                arrowNavigatorDemonstration.setStartView(findViewById(R.id.textNavigatorMovingDemonstration));
                arrowNavigatorDemonstration.setEndView(findViewById(R.id.navigatorMovingDemonstration));
                arrowNavigatorDemonstration.invalidate(CENTER_END, CENTER_START);

                arrowVerificationDemonstration.setWillNotDraw(false);
                arrowVerificationDemonstration.setStartView(findViewById(R.id.textVerificationMovingDemonstration));
                arrowVerificationDemonstration.setEndView(findViewById(R.id.verificationMovingDemonstration));
                arrowVerificationDemonstration.invalidate(CENTER_END, CENTER_START);
            }
        }
    }
}
