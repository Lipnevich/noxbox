package by.nicolay.lipnevich.noxbox.detailed;

import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.DebugMessage;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class DetailedNoxboxPage extends AppCompatActivity implements AppBarLayout.OnOffsetChangedListener {

    private static final float PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR  = 0.9f;
    private static final float PERCENTAGE_TO_HIDE_TITLE_DETAILS     = 0.3f;
    private static final int ALPHA_ANIMATIONS_DURATION              = 200;

    private boolean mIsTheTitleVisible          = false;
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

        init();

        mAppBarLayout.addOnOffsetChangedListener(this);

        startAlphaAnimation(mTitle, 0, View.INVISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        State.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    private void draw(Profile profile){
        DebugMessage.popup(this,getResources().getString(profile.getViewed().getType().getName()));
        //drawTypeIcon(profile);
    }
    private void drawTypeIcon(Profile profile){
        mTypeImageView.setImageResource(profile.getViewed().getType().getImage());
    }

    private void init() {
        mTitle          = (TextView) findViewById(R.id.toolbarTextView);
        mTitleContainer = (LinearLayout) findViewById(R.id.titleContainerLayout);
        mAppBarLayout   = (AppBarLayout) findViewById(R.id.appBarLayout);
        mTypeImageView   = (ImageView) findViewById(R.id.typeImageView);

        //TODO need find background picture for all types..
        mBackImageTypeView   = (ImageView) findViewById(R.id.backgroundImageTypeView);

        findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO processing of acceptance of service..
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

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int offset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(offset) / (float) maxScroll;

        handleAlphaOnTitle(percentage);
        handleToolbarTitleVisibility(percentage);
    }

    private void handleToolbarTitleVisibility(float percentage) {
        if (percentage >= PERCENTAGE_TO_SHOW_TITLE_AT_TOOLBAR) {

            if(!mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleVisible = true;
            }

        } else {

            if (mIsTheTitleVisible) {
                startAlphaAnimation(mTitle, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleVisible = false;
            }
        }
    }

    private void handleAlphaOnTitle(float percentage) {
        if (percentage >= PERCENTAGE_TO_HIDE_TITLE_DETAILS) {
            if(mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.INVISIBLE);
                mIsTheTitleContainerVisible = false;
            }

        } else {

            if (!mIsTheTitleContainerVisible) {
                startAlphaAnimation(mTitleContainer, ALPHA_ANIMATIONS_DURATION, View.VISIBLE);
                mIsTheTitleContainerVisible = true;
            }
        }
    }

    public static void startAlphaAnimation (View v, long duration, int visibility) {
        AlphaAnimation alphaAnimation = (visibility == View.VISIBLE)
                ? new AlphaAnimation(0f, 1f)
                : new AlphaAnimation(1f, 0f);

        alphaAnimation.setDuration(duration);
        alphaAnimation.setFillAfter(true);
        v.startAnimation(alphaAnimation);
    }
}
