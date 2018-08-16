package live.noxbox.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

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

        drawHomeIfSelected(profile);

        drawAboutMe(profile);
        drawCertificates(profile);
        drawWorkSamples(profile);
    }

    private void drawPhoto(Profile profile) {
        ((ImageView)findViewById(R.id.profileImage)).setImageResource(R.drawable.unknown_profile);
        //((ImageView)findViewById(R.id.profileImage)).setImageResource(profile.getProfileImage());
    }

    private void drawName(Profile profile) {
        this.getSupportActionBar().setTitle(profile.getName());
    }

    private void drawTravelMode(Profile profile) {
        ((TextView) findViewById(R.id.travelModeName)).setText(profile.getTravelMode().getName());
    }

    private void drawHomeIfSelected(Profile profile) {
        if (profile.getTravelMode() == TravelMode.none || profile.getHost()) {
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.yes);
        } else {
            ((TextView) findViewById(R.id.hostDescription)).setText(R.string.no);
        }
    }

    private void drawAboutMe(Profile profile) {

    }

    private void drawCertificates(Profile profile) {
        List<String> certificatesList = new ArrayList<>();
        certificatesList.add("https://i.pinimg.com/736x/1d/ba/a1/1dbaa1fb5b2f64e54010cf6aae72b8b1.jpg");
        certificatesList.add("http://4u-professional.com/assets/images/sert/gel-lak.jpg");
        certificatesList.add("https://www.hallyuuk.com/wp-content/uploads/2018/06/reiki-master-certificate-template-inspirational-reiki-certificate-templates-idealstalist-of-reiki-master-certificate-template.jpg");
        certificatesList.add("http://www.childminder.ng/blog_pics/1479134810.jpg");
        certificatesList.add(" ");
        RecyclerView myList = (RecyclerView) findViewById(R.id.certificatesList);
        myList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myList.setAdapter(new CertificatesAdapter(certificatesList,this));
    }

    private void drawWorkSamples(Profile profile) {
        List<String> workSampleList = new ArrayList<>();
        workSampleList.add("http://coolmanicure.com/media/k2/items/cache/stilnyy_manikur_so_strazami_XL.jpg");
        workSampleList.add("http://rosdesign.com/design_materials3/img_materials3/kopf/kopf1.jpg");
        workSampleList.add("http://vmirevolos.ru/wp-content/uploads/2015/12/61.jpg");
        workSampleList.add(" ");
        RecyclerView myList = (RecyclerView) findViewById(R.id.workSampleList);
        myList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        myList.setAdapter(new CertificatesAdapter(workSampleList,this));
    }
}
