package live.noxbox.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import live.noxbox.R;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;
import live.noxbox.state.ProfileStorage;
import live.noxbox.tools.Task;

public class ProfileActivity extends AppCompatActivity {

    public static final int CODE = 1006;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProfileStorage.listenProfile(ProfileActivity.class.getName(), new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                ((ImageView) findViewById(R.id.travelModeImage)).setImageResource(profile.getTravelMode().getImage());
                draw(profile);
            }
        });
    }

    private void draw(Profile profile) {
        drawPhoto(profile);
        drawName(profile);
        drawTravelMode(profile);

        if (profile.getTravelMode() != TravelMode.none && profile.getHost()) {
            drawHomeIfSelected(profile);
        } else {
            findViewById(R.id.travelModeLayoutSecond).setVisibility(View.GONE);
        }

        drawAboutMe(profile);
        drawCertificates(profile);
        drawWorkSamples(profile);
    }

    private void drawPhoto(Profile profile) {
    }

    private void drawName(Profile profile) {
       
    }

    private void drawTravelMode(Profile profile) {

    }

    private void drawHomeIfSelected(Profile profile) {

    }

    private void drawAboutMe(Profile profile) {

    }

    private void drawCertificates(Profile profile) {

    }

    private void drawWorkSamples(Profile profile) {

    }
}
