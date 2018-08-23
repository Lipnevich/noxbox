package live.noxbox.profile;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
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

import java.io.Serializable;
import java.util.List;

import live.noxbox.R;
import live.noxbox.model.NoxboxType;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.DebugMessage;
import live.noxbox.tools.FirestoreReference;
import live.noxbox.tools.Task;

import static live.noxbox.tools.ImageManager.createCircleImageFromUrl;

public class ProfileActivity extends FragmentActivity {

    public static final int CODE = 1006;
    public static final int SELECT_IMAGE = 1007;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        ProfileStorage.listenProfile(ProfileActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });

        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    private void draw(final Profile profile) {
        findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawEditable(profile);

            }
        });
        ((ImageView) findViewById(R.id.editProfile)).setImageResource(R.drawable.edit);
        drawPhoto(profile);
        drawName(profile);
        drawTravelMode(profile);
        drawHost(profile);
        drawCertificates(profile);
        drawWorkSamples(profile);
    }

    private void drawPhoto(final Profile profile) {
        findViewById(R.id.profileImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        if (profile.getPhoto() != null) {
            createCircleImageFromUrl(this, profile.getPhoto(), (ImageView) findViewById(R.id.profileImage));
            return;
        }
        ((ImageView) findViewById(R.id.profileImage)).setImageResource(R.drawable.unknown_profile);
    }

    private void drawName(Profile profile) {
        ((EditText) findViewById(R.id.editName)).setText(profile.getName());
        ((EditText) findViewById(R.id.editName)).setEnabled(false);
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

        setHostStatus(profile.getTravelMode() == TravelMode.none || profile.getHost(),profile);

        findViewById(R.id.switchHost).setVisibility(View.GONE);
    }

    private List<String> certificatesList;

    private void drawCertificates(final Profile profile) {
        certificatesList = profile.getPortfolio().get(NoxboxType.haircut.name()).getCertificates();
        findViewById(R.id.addCertificate).setVisibility(View.GONE);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.certificatesList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new CertificatesAdapter(certificatesList, this));

        findViewById(R.id.certificateLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });

        setOnItemCertificateClickListener(recyclerView);
    }

    private List<String> workSampleList;

    private void drawWorkSamples(final Profile profile) {
        workSampleList = profile.getPortfolio().get(NoxboxType.haircut.name()).getWorkSamples();

        findViewById(R.id.addWorkSample).setVisibility(View.GONE);

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.workSampleList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new WorkSamplesAdapter(workSampleList, this));

        findViewById(R.id.workSampleLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { }
        });

        setOnItemWorkSampleClickListener(recyclerView);
    }


    private void drawEditable(final Profile profile) {
        findViewById(R.id.editProfile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                draw(profile);

            }
        });
        ((ImageView) findViewById(R.id.editProfile)).setImageResource(R.drawable.yes);
        drawEditPhoto(profile);
        drawEditName(profile);
        drawEditTravelMode(profile);
        drawEditHost(profile);
        drawEditCertificates(profile);
        drawEditWorkSamples(profile);
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
        ((EditText) findViewById(R.id.editName)).setEnabled(true);
        ((EditText) findViewById(R.id.editName)).setSelection(profile.getName().length());
        ((EditText) findViewById(R.id.editName)).addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                profile.setName(String.valueOf(s));

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
        ((Switch) findViewById(R.id.switchHost)).setVisibility(View.VISIBLE);
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

    private void drawEditCertificates(final Profile profile) {
        certificatesList = profile.getPortfolio().get(NoxboxType.haircut.name()).getCertificates();
        findViewById(R.id.addCertificate).setVisibility(View.VISIBLE);
        findViewById(R.id.addCertificate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) invite user to upload certificate
                DebugMessage.popup(ProfileActivity.this, "Upload work certificates");
            }
        });
        findViewById(R.id.certificateLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) invite user to upload certificate
                DebugMessage.popup(ProfileActivity.this, "Upload work certificates");
            }
        });
    }

    private void drawEditWorkSamples(final Profile profile) {
        workSampleList = profile.getPortfolio().get(NoxboxType.haircut.name()).getWorkSamples();
        findViewById(R.id.addWorkSample).setVisibility(View.VISIBLE);
        findViewById(R.id.addWorkSample).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) invite user to upload work sample
                DebugMessage.popup(ProfileActivity.this, "Upload work sample");
            }
        });
        findViewById(R.id.workSampleLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) invite user to upload work sample
                DebugMessage.popup(ProfileActivity.this, "Upload work sample");
            }
        });

    }

    private void setTravelModeStatus(Profile profile) {
        ((TextView) findViewById(R.id.travelModeName)).setText(profile.getTravelMode().name());
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    FirestoreReference.createImageReference(this, data.getData(), (ImageView) findViewById(R.id.profileImage));
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                DebugMessage.popup(this, "Cancelled");
            }
        }
        if (requestCode == TravelModeListActivity.CODE) {

            ProfileStorage.readProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    drawEditable(profile);
                }
            });

        }
    }


    private void setOnItemCertificateClickListener(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("photos", (Serializable) certificatesList);
                bundle.putInt("position", position);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment slideShowFragment = SlideshowDialogFragment.newInstance();
                slideShowFragment.setArguments(bundle);
                slideShowFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) { }
        }));
    }

    private void setOnItemWorkSampleClickListener(RecyclerView recyclerView) {
        recyclerView.addOnItemTouchListener(new RecyclerTouchListener(getApplicationContext(), recyclerView, new RecyclerClickListener() {
            @Override
            public void onClick(View view, int position) {
                Bundle bundle = new Bundle();
                bundle.putSerializable("photos", (Serializable) workSampleList);
                bundle.putInt("position", position);

                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                SlideshowDialogFragment slideShowFragment = SlideshowDialogFragment.newInstance();
                slideShowFragment.setArguments(bundle);
                slideShowFragment.show(fragmentTransaction, "slideshow");
            }

            @Override
            public void onLongClick(View view, int position) { }
        }));
    }
}
