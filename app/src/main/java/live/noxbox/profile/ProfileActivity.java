package live.noxbox.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.BaseActivity;
import live.noxbox.R;
import live.noxbox.contract.NoxboxTypeListActivity;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.AppCache;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.Task;

import static live.noxbox.tools.ImageManager.createCircleImageFromUrl;

public class ProfileActivity extends BaseActivity {

    public static final int CODE = 1006;
    public static final int SELECT_IMAGE = 1007;

    private static boolean isEditable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(ProfileActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                if (isEditable) {
                    drawEditable(profile);
                } else {
                    draw(profile);
                }
            }
        });

        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
    }

    private void draw(final Profile profile) {
        findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditable = true;
                drawEditable(profile);
            }
        });
        ((ImageView) findViewById(R.id.editProfile)).setImageResource(R.drawable.edit);
        drawPhoto(profile);
        drawName(profile);
        drawTravelMode(profile);
        drawHost(profile);
        drawPortfolioEditingMenu(profile);
        drawMenuAddingPerformer(profile);
    }

    private void drawPhoto(final Profile profile) {
        findViewById(R.id.profileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        if (profile.getPhoto() != null) {
            createCircleImageFromUrl(this, profile.getPhoto(), (ImageView) findViewById(R.id.profileImage));
        } else {
            ((ImageView) findViewById(R.id.profileImage)).setImageResource(R.drawable.unknown_profile);
        }
    }

    private void drawName(Profile profile) {
        ((EditText) findViewById(R.id.editName)).setText(profile.getName());
        findViewById(R.id.editName).setEnabled(false);
    }

    private void drawTravelMode(final Profile profile) {
        setTravelModeStatus(profile);
        findViewById(R.id.travelModeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
        findViewById(R.id.editTravelMode).setVisibility(View.GONE);
    }

    private void drawHost(final Profile profile) {

        findViewById(R.id.hostLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        setHostStatus(profile.getTravelMode() == TravelMode.none || profile.getHost(), profile);

        findViewById(R.id.switchHost).setVisibility(View.GONE);
    }

    private void drawPortfolioEditingMenu(final Profile profile) {

        List<NoxboxType> typeList = new ArrayList<>();
        for (NoxboxType type : NoxboxType.values()) {
            if (profile.getPortfolio().get(type.name()) != null) {
                typeList.add(type);
            }
        }
        if (typeList.size() >= 1) {
            findViewById(R.id.serviceNotProvidedLayout).setVisibility(View.INVISIBLE);
            findViewById(R.id.serviceProvidedText).setVisibility(View.VISIBLE);
            RecyclerView recyclerView = findViewById(R.id.noxboxTypeList);
            recyclerView.setVisibility(View.VISIBLE);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            recyclerView.setAdapter(new PortfolioNoxboxTypeAdapter(typeList, ProfileActivity.this));
        } else {
            findViewById(R.id.noxboxTypeList).setVisibility(View.GONE);
            findViewById(R.id.serviceNotProvidedLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.serviceProvidedText).setVisibility(View.GONE);
        }

    }

    private void drawMenuAddingPerformer(final Profile profile) {
        if (profile.getPortfolio().values().size() == NoxboxType.values().length){
            findViewById(R.id.addLayout).setVisibility(View.GONE);
            return;
        }

        findViewById(R.id.addLayout).setVisibility(View.VISIBLE);
        findViewById(R.id.addLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, NoxboxTypeListActivity.class);
                intent.putExtra(ProfileActivity.class.getName(), NoxboxTypeListActivity.class.getName());
                startActivityForResult(intent, NoxboxTypeListActivity.PROFILE_CODE);
            }
        });
    }


    private void drawEditable(final Profile profile) {
        findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isEditable = false;
                AppCache.fireProfile();
                draw(profile);
            }
        });
        ((ImageView) findViewById(R.id.editProfile)).setImageResource(R.drawable.yes);
        drawEditPhoto(profile);
        drawEditName(profile);
        drawEditTravelMode(profile);
        drawEditHost(profile);
        drawPortfolioEditingMenu(profile);
        drawMenuAddingPerformer(profile);
    }

    private void drawEditPhoto(final Profile profile) {
        createCircleImageFromUrl(this, profile.getPhoto(), (ImageView) findViewById(R.id.profileImage));
        findViewById(R.id.profileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_IMAGE);
            }
        });

    }

    private void drawEditName(final Profile profile) {
        findViewById(R.id.editName).setEnabled(true);
        ((EditText) findViewById(R.id.editName)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String newName = String.valueOf(s);
                if (!profile.getName().equals(newName)) {
                    profile.setName(newName);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void drawEditTravelMode(final Profile profile) {


        if (profile.getTravelMode() == TravelMode.none) {
            findViewById(R.id.hostLayout).setEnabled(false);
            findViewById(R.id.switchHost).setEnabled(false);
        } else {
            findViewById(R.id.hostLayout).setEnabled(true);
            findViewById(R.id.switchHost).setEnabled(true);
        }

        setTravelModeStatus(profile);

        findViewById(R.id.editTravelMode).setVisibility(View.VISIBLE);
        findViewById(R.id.travelModeLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ProfileActivity.this, TravelModeListActivity.class);
                startActivityForResult(intent, TravelModeListActivity.CODE);
            }
        });

    }

    private void drawEditHost(final Profile profile) {
        findViewById(R.id.switchHost).setVisibility(View.VISIBLE);
        ((Switch) findViewById(R.id.switchHost)).setChecked(profile.getHost());
        findViewById(R.id.hostLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (profile.getHost()) {
                    setHostStatus(false, profile);
                } else {
                    setHostStatus(true, profile);
                }
            }
        });
        ((Switch) findViewById(R.id.switchHost)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setHostStatus(isChecked, profile);
            }
        });
    }

    private void setTravelModeStatus(Profile profile) {
        ((TextView) findViewById(R.id.travelModeName)).setText(getText(profile.getTravelMode().getName()));
        ((ImageView) findViewById(R.id.travelModeImage)).setImageResource(profile.getTravelMode().getImage());
    }

    private void setHostStatus(boolean isChecked, Profile profile) {
        if (isChecked) {
            profile.setHost(isChecked);
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.haveHost);
        } else {
            profile.setHost(isChecked);
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.notHaveHost);
        }
        ((Switch) findViewById(R.id.switchHost)).setChecked(profile.getHost());
    }

    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    ImageManager.uploadPhoto(this, data.getData());
                    AppCache.readProfile(new Task<Profile>() {
                        @Override
                        public void execute(Profile profile) {
                            profile.setPhoto(data.getData().toString());
                        }
                    });
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebugMessage.popup(this, "Cancelled");
            }
        }
        if (requestCode == TravelModeListActivity.CODE) {

            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    drawEditable(profile);
                }
            });

        }

        if (requestCode == NoxboxTypeListActivity.PROFILE_CODE) {

            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    draw(profile);
                }
            });

        }
        if (requestCode == ProfilePerformerActivity.CODE) {
            AppCache.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    draw(profile);
                }
            });
        }
    }

}
