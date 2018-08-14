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

import live.noxbox.R;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.Task;

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
        setContentView(R.layout.activity_filters);

        demand = findViewById(R.id.demand);
        supply = findViewById(R.id.supply);
        price = findViewById(R.id.price);
        priceText = findViewById(R.id.priceText);
        typeLayout = findViewById(R.id.types);
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

    private void draw(final Profile profile) {
        demand.setChecked(profile.getFilters().getDemand());
        supply.setChecked(profile.getFilters().getSupply());
        price.setProgress(profile.getFilters().getPrice());
        priceText.setText(profile.getFilters().getPrice() + " " + getResources().getString(R.string.currency));

        demand.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.getFilters().setDemand(isChecked);
            }
        });

        supply.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                profile.getFilters().setSupply(isChecked);
            }
        });

        price.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                profile.getFilters().setPrice(progress);
                priceText.setText(seekBar.getProgress() + " " + getResources().getString(R.string.currency));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        typesName = new String[NoxboxType.values().length];
        typesChecked = new boolean[NoxboxType.values().length];
        for (NoxboxType type : NoxboxType.values()) {
            typesName[type.getId()] = getResources().getString(type.getName());
            typesChecked[type.getId()] = profile.getFilters().getTypes().get(type.name());
        }


        typeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MapFiltersActivity.this);
                builder.setMultiChoiceItems(typesName, typesChecked, new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                        DebugMessage.popup(MapFiltersActivity.this, String.valueOf(which));
                        typesChecked[which] = isChecked;
                    }
                });

                builder.setCancelable(true);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (NoxboxType type : NoxboxType.values()) {
                            profile.getFilters().getTypes().put(type.name(), typesChecked[type.getId()]);
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (NoxboxType type : NoxboxType.values()) {
                            typesChecked[type.getId()] = profile.getFilters().getTypes().get(type.name());
                        }
                        dialog.dismiss();
                    }
                });

                builder.setNeutralButton(R.string.chooseAll, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        for (NoxboxType type : NoxboxType.values()) {
                            typesChecked[type.getId()] = true;
                            profile.getFilters().getTypes().put(type.name(), true);
                        }
                    }
                });
                AlertDialog mAlertDialog = builder.create();
                mAlertDialog.show();

            }
        });
    }
}
