package by.nicolay.lipnevich.noxbox.detailed;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class DetailedNoxboxPage extends AppCompatActivity {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION = 200;

    private boolean mIsTheTitleVisible = false;
    private boolean mIsTheTitleContainerVisible = true;

    private LinearLayout mTitleContainer;
    private TextView mTitle;
    private AppBarLayout mAppBarLayout;
    private ImageView mTypeImageView;
    private ImageView mBackImageTypeView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed_description);
        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                init();
                draw(profile);
            }
        });
    }

    private void draw(Profile profile) {
        drawToolbar(profile);
        drawNoxboxPrice(profile);
        //drawTypeIcon(profile);
    }

    private void drawToolbar(Profile profile) {
        setSupportActionBar(((Toolbar) findViewById(R.id.toolbar)));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(profile.getViewed().getType().getName());
    }

    private void drawNoxboxPrice(Profile profile) {
        ((TextView) findViewById(R.id.noxboxPriceView)).setText(profile.getCurrent().getPrice());
    }



    private void drawTypeIcon(Profile profile) {
        Glide.with(getApplicationContext())
                .asBitmap()
                //.load(profile.getViewed().getType().getImage())
                .load(R.drawable.cat)
                .apply(RequestOptions.circleCropTransform())
                .into(mTypeImageView);

    }

    private void init() {
        //mTitle = (TextView) findViewById(R.id.toolbarTextView);
        // mTitleContainer = (LinearLayout) findViewById(R.id.titleContainerLayout);
        //mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        // mTypeImageView = (ImageView) findViewById(R.id.typeImageView);


        //TODO need find background picture for all types..
        // mBackImageTypeView = (ImageView) findViewById(R.id.backgroundImageTypeView);

        findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                State.listenProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        profile.setCurrent(profile.getViewed());
                        finish();
                    }
                });
            }
        });
        findViewById(R.id.cancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                State.listenProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        profile.setViewed(null);
                        finish();
                    }
                });
            }
        });
    }


}
