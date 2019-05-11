package live.noxbox.menu.settings;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;
import live.noxbox.tools.Router;

import static live.noxbox.menu.settings.NoxboxTypeSelectionFragment.SETTINGS_CODE;

public class MapSettingsActivity extends BaseActivity {

    public static final int CODE = 1005;

    private Switch novice;
    private Switch demand;
    private Switch supply;
    private SeekBar price;
    private TextView priceText;
    private LinearLayout typeLayout;
    private TextView noviceTitle;
    private TextView demandTitle;
    private TextView supplyTitle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initializeUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(MapSettingsActivity.class.getName(), profile -> draw(profile));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
        AppCache.fireProfile();
    }

    private void initializeUi() {
        novice = findViewById(R.id.novice);
        demand = findViewById(R.id.demand);
        supply = findViewById(R.id.supply);
        price = findViewById(R.id.priceBar);
        priceText = findViewById(R.id.participantName);
        typeLayout = findViewById(R.id.typeLayout);
        noviceTitle = findViewById(R.id.noviceTitle);
        demandTitle = findViewById(R.id.demandTitle);
        supplyTitle = findViewById(R.id.supplyTitle);
    }

    private void draw(final Profile profile) {
        drawToolbar();
        drawTitles();
        drawNovice(profile);
        drawDemand(profile);
        drawSupply(profile);
        drawPrice(profile);
        drawTypeList(profile);
    }

    private void drawToolbar() {
        ((TextView) findViewById(R.id.title)).setText(R.string.settings);
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(MapSettingsActivity.this));
    }

    private void drawTitles() {
        noviceTitle.setText(getResources().getString(R.string.novice));
        demandTitle.setText(getResources().getString(R.string.demand).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.demand).substring(1)));
        supplyTitle.setText(getResources().getString(R.string.supply).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.supply).substring(1)));
    }

    private void drawNovice(final Profile profile) {
        novice.setChecked(profile.getFilters().getAllowNovices());
        novice.setOnCheckedChangeListener((buttonView, isChecked) -> profile.getFilters().setAllowNovices(isChecked));
    }

    private void drawDemand(final Profile profile) {
        demand.setChecked(profile.getFilters().getDemand());
        demand.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profile.getFilters().setDemand(isChecked);
            if (!isChecked && supply != null) {
                supply.setChecked(true);
            }
        });
    }

    private void drawSupply(final Profile profile) {
        supply.setChecked(profile.getFilters().getSupply());
        supply.setOnCheckedChangeListener((buttonView, isChecked) -> {
            profile.getFilters().setSupply(isChecked);
            if (!isChecked && demand != null) {
                demand.setChecked(true);
            }

        });
    }

    private void drawPrice(final Profile profile) {
        price.setMax(100);
        price.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    if (progress == 0) {
                        profile.getFilters().setPrice(1);
                        priceText.setText("1 " + getString(R.string.currency));
                        return;
                    }
                    if (progress == 100) {
                        profile.getFilters().setPrice(Integer.MAX_VALUE);
                        priceText.setText(R.string.max);
                        return;
                    }
                    profile.getFilters().setPrice(progress);
                    priceText.setText(progress + " " + getResources().getString(R.string.currency));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        if (profile.getFilters().getPrice() >= 100) {
            price.setProgress(100);
            priceText.setText(getResources().getString(R.string.max));
        } else {
            priceText.setText(profile.getFilters().getPrice() + "" + getResources().getString(R.string.currency));
            price.setProgress(profile.getFilters().getPrice());
        }
    }

    private DialogFragment dialog;

    private void drawTypeList(final Profile profile) {
        typeLayout.setOnClickListener(v -> {
            if (dialog == null || !dialog.isVisible()) {
                dialog = new NoxboxTypeSelectionFragment();
                Bundle bundle = new Bundle();
                bundle.putInt("key", SETTINGS_CODE);
                dialog.setArguments(bundle);
                dialog.show(MapSettingsActivity.this.getSupportFragmentManager(), NoxboxTypeSelectionFragment.TAG);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                Router.finishActivity(MapSettingsActivity.this);
                break;
        }
        return true;
    }
}
