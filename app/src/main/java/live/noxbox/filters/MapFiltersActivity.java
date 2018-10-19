package live.noxbox.filters;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import live.noxbox.Configuration;
import live.noxbox.R;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Task;

import static com.google.common.base.MoreObjects.firstNonNull;

public class MapFiltersActivity extends AppCompatActivity {

    public static final int CODE = 1005;

    private Switch demand;
    private Switch supply;
    private SeekBar price;
    private TextView priceText;
    private LinearLayout typeLayout;
    private String[] typesName;
    private boolean[] typesChecked;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.filters);
        setContentView(R.layout.activity_filters);

        initialize();
    }

    private void initialize() {
        demand = findViewById(R.id.demand);
        supply = findViewById(R.id.supply);
        price = findViewById(R.id.price);
        priceText = findViewById(R.id.priceText);
        typeLayout = findViewById(R.id.types);

        ((TextView) findViewById(R.id.demandTitle)).setText(getResources().getString(R.string.demand).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.demand).substring(1)));
        ((TextView) findViewById(R.id.supplyTitle)).setText(getResources().getString(R.string.supply).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.supply).substring(1)));
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProfileStorage.listenProfile(MapFiltersActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        ProfileStorage.stopListen(this.getClass().getName());
        ProfileStorage.fireProfile();
    }

    private void draw(final Profile profile) {
        drawDemand(profile);
        drawSupply(profile);
        drawPrice(profile);
        drawTypeList(profile);
    }

    private void drawDemand(final Profile profile) {
        demand.setChecked(profile.getFilters().getDemand());
        demand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.getFilters().setDemand(isChecked);
                if (isChecked) {
                    supply.setClickable(true);
                } else {
                    supply.setClickable(false);
                }
            }
        });
    }

    private void drawSupply(final Profile profile) {
        supply.setChecked(profile.getFilters().getSupply());
        supply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.getFilters().setSupply(isChecked);
                if (isChecked) {
                    demand.setClickable(true);
                } else {
                    demand.setClickable(false);
                }

            }
        });
    }

    private void drawPrice(final Profile profile) {
        if (profile.getFilters().getPrice().equals("0")) {
            priceText.setText(R.string.max);
            price.setProgress(100);
        } else {
            priceText.setText(profile.getFilters().getPrice() + Configuration.CURRENCY);
            price.setProgress(Integer.parseInt(profile.getFilters().getPrice()));
        }
        price.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (progress == 0) {
                    profile.getFilters().setPrice("1");
                    priceText.setText("1 " + Configuration.CURRENCY);
                    return;
                }
                if (progress == 100) {
                    profile.getFilters().setPrice("0");
                    priceText.setText(R.string.max);
                    return;
                }
                profile.getFilters().setPrice(String.valueOf(progress));
                priceText.setText(progress + " " + Configuration.CURRENCY);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
    }

    private int totalChecked;

    private void drawTypeList(final Profile profile) {

        typesName = new String[NoxboxType.values().length];
        typesChecked = new boolean[NoxboxType.values().length];
        for (NoxboxType type : NoxboxType.values()) {
            typesName[type.getId()] = getResources().getString(type.getName());
            typesChecked[type.getId()] = firstNonNull(profile.getFilters().getTypes().get(type.name()), true);
            if (typesChecked[type.getId()]) {
                totalChecked++;
            }
        }
        DebugMessage.popup(this, String.valueOf(totalChecked));


        typeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(MapFiltersActivity.this)
                        .setTitle(getResources().getString(R.string.service).substring(0, 1).toUpperCase().concat(getResources().getString(R.string.service).substring(1)))
                        .setCancelable(false)
                        .setMultiChoiceItems(typesName, typesChecked, new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                if (isChecked) {
                                    totalChecked++;
                                } else {
                                    totalChecked--;
                                }
                                typesChecked[which] = isChecked;

                            }
                        })
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (NoxboxType type : NoxboxType.values()) {
                                    profile.getFilters().getTypes().put(type.name(), typesChecked[type.getId()]);
                                }
                                if (totalChecked == 0) {
                                    totalChecked++;
                                    typesChecked[0] = true;
                                    profile.getFilters().getTypes().put(NoxboxType.byId(0).name(), true);
                                }
                            }
                        })
                        .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (NoxboxType type : NoxboxType.values()) {
                                    typesChecked[type.getId()] = profile.getFilters().getTypes().get(type.name());
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNeutralButton(R.string.chooseAll, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                for (NoxboxType type : NoxboxType.values()) {
                                    typesChecked[type.getId()] = true;
                                    profile.getFilters().getTypes().put(type.name(), true);
                                }
                            }
                        })
                        .create()
                        .show();

            }
        });
    }
}
