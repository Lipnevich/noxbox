package by.nicolay.lipnevich.noxbox.detailed;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Comment;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.model.WorkSchedule;
import by.nicolay.lipnevich.noxbox.state.State;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.tools.DateTimeFormatter.date;

public class DetailedNoxboxPage extends AppCompatActivity {

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
        drawWaitingTime(profile.getViewed());
        drawPrice(profile.getViewed());

    }

    //TODO (vl) make textView instead title
    private void drawToolbar(Noxbox noxbox) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        final Drawable backArrow = getResources().getDrawable(R.drawable.arrow_back);
        //upArrow.setColorFilter(getResources().getColor(R.color.grey), PorterDuff.Mode.SRC_ATOP);
        //getSupportActionBar().setHomeAsUpIndicator(backArrow);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //getSupportActionBar().setTitle(noxbox.getType().getName());

    }

    private void drawDescription(Noxbox noxbox) {
        drawDropdownElement(R.id.descriptionTitleLayout, R.id.descriptionLayout);
        changeArrowVector(R.id.descriptionLayout, R.id.descriptionArrow);
        //((TextView) findViewById(R.id.descriptionTitle)).setText(getResources().getString(R.string.description));
        if (noxbox.getRole() == MarketRole.supply) {
            ((TextView) findViewById(R.id.previousDescription)).setText("Готов предоставить услугу:");
            ((TextView) findViewById(R.id.descriptionTitle)).setText("Предоставлю");
        } else {//TODO (vl) transfer text to xml
            ((TextView) findViewById(R.id.previousDescription)).setText("Хочу получить услугу:");
            ((TextView) findViewById(R.id.descriptionTitle)).setText("Получу");
        }

        //((TextView)findViewById(R.id.description)).setText(noxbox.getType().getDescription());
        ((TextView) findViewById(R.id.date)).setText("Дата регистрации услуги" + " " + date(noxbox.getTimeCreated()));

    }

    private void drawRating(Rating rating) {
        drawDropdownElement(R.id.ratingTitleLayout, R.id.ratingLayout);
        changeArrowVector(R.id.ratingLayout, R.id.ratingArrow);
        //((TextView) findViewById(R.id.ratingTitle)).setText(R.string.rating);
        ((TextView) findViewById(R.id.ratingTitle)).setText(rating.toPercentage() + "%");
        ((TextView) findViewById(R.id.rating)).setText(rating.toPercentage() + "%");
        ((TextView) findViewById(R.id.like)).setText(rating.getReceivedLikes() + " like");
        ((TextView) findViewById(R.id.dislike)).setText(rating.getReceivedDislikes() + " dislike");

        List<Comment> comments = new ArrayList<>();
        comments.add(rating.getComments().get("0"));
        comments.add(rating.getComments().get("1"));
        comments.add(rating.getComments().get("2"));

        RecyclerView recyclerView = findViewById(R.id.listComments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommentAdapter(comments));
    }

    private void drawAvailableTime(WorkSchedule workSchedule) {
        drawDropdownElement(R.id.availableTimeTitleLayout, R.id.availableTimeLayout);
        changeArrowVector(R.id.availableTimeLayout, R.id.timeArrow);
        //((TextView) findViewById(R.id.availableTimeTitle)).setText(R.string.availableTime);
        LocalTime startTime = new LocalTime(workSchedule.getStartTime().getHourOfDay(), workSchedule.getStartTime().getMinuteOfHour());
        LocalTime endTime = new LocalTime(workSchedule.getEndTime().getHourOfDay(), workSchedule.getEndTime().getMinuteOfHour());

        String displayTime ="";
        if (DateFormat.is24HourFormat(getApplicationContext())) {
            displayTime = startTime.getHourOfDay() + ":" + startTime.getMinuteOfHour() + " - " + endTime.getHourOfDay() + ":" + endTime.getMinuteOfHour();
        } else {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("hh:mm a");
            displayTime = fmt.print(startTime) + " - " + fmt.print(endTime);
        }
        ((TextView) findViewById(R.id.availableTimeTitle)).setText(displayTime);
        ((TextView) findViewById(R.id.availableTime)).setText(displayTime);
        ((TextView) findViewById(R.id.currentDate)).setText("Пт 27 GMT +3");
    }

    private void drawWaitingTime(Noxbox noxbox) {
        drawDropdownElement(R.id.travelTypeTitleLayout, R.id.travelTypeLayout);
        changeArrowVector(R.id.travelTypeLayout, R.id.travelTypeArrow);
        ((ImageView) findViewById(R.id.travelTypeImage)).setImageResource(noxbox.getOwner().getTravelMode().getImage());
        //TODO
        if (noxbox.getOwner().getTravelMode() == TravelMode.none) {
            ((TextView) findViewById(R.id.travelTypeTitle)).setText("30 минут");
        }
        ((TextView) findViewById(R.id.travelTypeTitle)).setText("30 минут");
    }

    private void drawPrice(Noxbox noxbox) {
        drawDropdownElement(R.id.priceTitleLayout, R.id.priceLayout);
        changeArrowVector(R.id.priceLayout, R.id.priceArrow);
        ((TextView) findViewById(R.id.priceTitle)).setText(noxbox.getPrice());
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

    private void drawDropdownElement(int titleId, final int contentId) {
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

    private void changeArrowVector(int layout, final int element) {
        final ViewGroup listeningLayout = findViewById(layout);
        listeningLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (listeningLayout.getVisibility() == View.VISIBLE) {
                    ((ImageView) findViewById(element)).setImageResource(R.drawable.arrow_up);
                } else if (listeningLayout.getVisibility() == View.GONE) {
                    ((ImageView) findViewById(element)).setImageResource(R.drawable.arrow_down);
                }
            }
        });

    }

    private void init() {
        //TODO need find background picture for all types..
        // mBackImageTypeView = (ImageView) findViewById(R.id.backgroundImageTypeView);
    }
}
