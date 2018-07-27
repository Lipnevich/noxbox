package by.nicolay.lipnevich.noxbox.detailed;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Comment;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.WorkSchedule;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.Task;

public class DetailedNoxboxPage extends AppCompatActivity {


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
        drawToolbar(profile.getViewed());
        drawDescription(profile.getViewed());
        drawRating(profile.getViewed().getOwner().getRating());
        drawAvailableTime(profile.getViewed().getWorkSchedule());
        drawPrice(profile.getViewed());

    }

    //TODO (vl) make textView instead title
    private void drawToolbar(Noxbox noxbox) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Drawable backArrow = getResources().getDrawable(R.drawable.arrow_back);
        //upArrow.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
       // getSupportActionBar().setHomeAsUpIndicator(backArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(noxbox.getType().getName());

    }

    private void drawDescription(Noxbox noxbox){
        ((ImageView)findViewById(R.id.typeImage)).setImageResource(R.drawable.information);
        ((TextView)findViewById(R.id.descriptionTitle)).setText(getResources().getString(R.string.description));
        if(noxbox.getRole() == MarketRole.supply){
            ((TextView)findViewById(R.id.previousDescription)).setText("Готов предоставить услугу:");
        }else{//TODO (vl) transfer text to xml
            ((TextView)findViewById(R.id.previousDescription)).setText("Хочу получить услугу:");
        }

        ((TextView)findViewById(R.id.description)).setText(noxbox.getType().getDescription());
        drawDropdownElement(R.id.descriptionTitleLayout,R.id.descriptionLayout);
    }

    private void drawRating(Rating rating) {
        drawDropdownElement(R.id.ratingTitleLayout,R.id.ratingLayout);
        ((ImageView)findViewById(R.id.ratingTitleImage)).setImageResource(R.drawable.star);
        ((TextView)findViewById(R.id.ratingTitle)).setText(R.string.rating);
        ((TextView)findViewById(R.id.rating))
                .setText("Rating in percentage = " + rating.toPercentage() + ";" + "likes = " + rating.getReceivedLikes() + ";" + "dislikes = " + rating.getReceivedLikes());

        List<Comment> comments = new ArrayList<>();
        comments.add(rating.getComments().get("0"));
        comments.add(rating.getComments().get("1"));
        comments.add(rating.getComments().get("2"));

        RecyclerView recyclerView = findViewById(R.id.listComments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommentAdapter(comments));
    }

    @SuppressLint("SetTextI18n")
    private void drawAvailableTime(WorkSchedule workSchedule){
        drawDropdownElement(R.id.availableTimeTitleLayout,R.id.availableTimeLayout);
        ((ImageView)findViewById(R.id.availableTimeImage)).setImageResource(R.drawable.clock_circle);
        ((TextView)findViewById(R.id.availableTimeTitle)).setText(R.string.availableTime);
        ((TextView)findViewById(R.id.availableTime))
                .setText("Available time = " + workSchedule.getStartTime().getHourOfDay() + ":"+ workSchedule.getStartTime().getMinuteOfHour() + " - "
                        + workSchedule.getEndTime().getHourOfDay() + ":"+ workSchedule.getEndTime().getMinuteOfHour());
    }

    @SuppressLint("SetTextI18n")
    private void drawPrice(Noxbox noxbox) {
        drawDropdownElement(R.id.priceTitleLayout,R.id.priceLayout);
        ((ImageView)findViewById(R.id.priceImage)).setImageResource(R.drawable.coin);
        ((TextView) findViewById(R.id.priceTitle)).setText(getResources().getString(R.string.priceTxt));
        ((TextView) findViewById(R.id.price)).setText(noxbox.getPrice());
        ((TextView) findViewById(R.id.typeTextInPrice)).setText(noxbox.getType().getName());
        ((TextView) findViewById(R.id.descriptionTextInPrice)).setText(noxbox.getType().getDescription());
        ((ImageView) findViewById(R.id.typeImageInPrice)).setImageResource(noxbox.getType().getImage());
        //TODO (vl) create copyButton with lower price

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

    private void drawDropdownElement(int titleId, final int contentId){
        findViewById(titleId).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (findViewById(contentId).isShown()) {
                    findViewById(contentId).setVisibility(View.GONE);
                    findViewById(contentId).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
                } else {
                    findViewById(contentId).setVisibility(View.VISIBLE);
                    findViewById(contentId).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
                }
            }
        });
    }

    private void init() {
        //mTitle = (TextView) findViewById(R.id.toolbarTextView);
        // mTitleContainer = (LinearLayout) findViewById(R.id.titleContainerLayout);
        //mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        // mTypeImageView = (ImageView) findViewById(R.id.typeImageView);


        //TODO need find background picture for all types..
        // mBackImageTypeView = (ImageView) findViewById(R.id.backgroundImageTypeView);


    }


}
