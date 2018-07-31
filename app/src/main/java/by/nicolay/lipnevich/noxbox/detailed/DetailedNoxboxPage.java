package by.nicolay.lipnevich.noxbox.detailed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        drawWaitingTime(profile.getViewed());
        drawAvailableTime(profile.getViewed().getWorkSchedule());
        drawRating(profile.getViewed().getOwner().getRating());
        drawPrice(profile.getViewed());
    }

    //TODO (vl) make textView instead title
    private void drawToolbar(Noxbox noxbox) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //final Drawable backArrow = getResources().getDrawable(R.drawable.arrow_back);
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
        /*LocalTime startTime = new LocalTime(workSchedule.getStartTime().getHourOfDay(), workSchedule.getStartTime().getMinuteOfHour());
        LocalTime endTime = new LocalTime(workSchedule.getEndTime().getHourOfDay(), workSchedule.getEndTime().getMinuteOfHour());

        String displayTime ="";
        if (DateFormat.is24HourFormat(getApplicationContext())) {
            displayTime = startTime.getHourOfDay() + ":" + startTime.getMinuteOfHour() + " - " + endTime.getHourOfDay() + ":" + endTime.getMinuteOfHour();
        } else {
            DateTimeFormatter fmt = DateTimeFormat.forPattern("hh:mm a");
            displayTime = (fmt.print(startTime) + " - " + fmt.print(endTime)).toUpperCase();
        }*/

        Date startTime = new Date(0, 0, 0, workSchedule.getStartTime().getHourOfDay(), workSchedule.getStartTime().getMinuteOfHour());
        Date endTime = new Date(0, 0, 0, workSchedule.getEndTime().getHourOfDay(), workSchedule.getEndTime().getMinuteOfHour());
        String displayTime = "";
        SimpleDateFormat simpleDateFormat = null;
        if (DateFormat.is24HourFormat(getApplicationContext())) {
            simpleDateFormat = new SimpleDateFormat("HH:mm");
        } else {
            simpleDateFormat = new SimpleDateFormat("hh:mm a");
        }

        displayTime = simpleDateFormat.format(startTime) + " - " + simpleDateFormat.format(endTime);
        ((TextView) findViewById(R.id.availableTimeTitle)).setText(displayTime);
        ((TextView) findViewById(R.id.availableTime)).setText(displayTime);
        ((TextView) findViewById(R.id.currentDate)).setText("Пт 27 GMT +3");
    }

    private void drawWaitingTime(final Noxbox noxbox) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ((TextView) findViewById(R.id.travelTypeTitle)).setText("No location permission");
            return;
        }

        drawDropdownElement(R.id.travelTypeTitleLayout, R.id.travelTypeLayout);
        changeArrowVector(R.id.travelTypeLayout, R.id.travelTypeArrow);
        ((ImageView) findViewById(R.id.travelTypeImageTitle)).setImageResource(noxbox.getOwner().getTravelMode().getImage());

        if (noxbox.getOwner().getTravelMode() != TravelMode.none) {
            State.listenProfile(new Task<Profile>() {
                @Override
                public void execute(Profile profile) {
                    float[] results = new float[1];
                    Location.distanceBetween(
                            noxbox.getPosition().getLatitude(),
                            noxbox.getPosition().getLongitude(),
                            profile.getPosition().getLatitude(),
                            profile.getPosition().getLongitude(), results);
                    int minutes = (int) (results[0] / noxbox.getOwner().getTravelMode().getSpeedInMetersPerMinute());
                    String timeTxt = "";
                    String distanceTxt = String.valueOf((int)results[0]/1000) + " км";
                    switch (minutes % 10) {
                        case 11: {
                            timeTxt = " минут";
                            break;
                        }
                        case 1: {
                            timeTxt = " минута";
                            break;
                        }
                        case 2: {
                            timeTxt = " минуты";
                            break;
                        }
                        case 3: {
                            timeTxt = " минуты";
                            break;
                        }
                        case 4: {
                            timeTxt = " минуты";
                            break;
                        }
                        default: {
                            timeTxt = " минут";
                            break;
                        }
                    }
                    ((TextView) findViewById(R.id.travelTypeTitle)).setText(String.valueOf(minutes) + timeTxt);
                    //((TextView) findViewById(R.id.travelTypeTitle)).setText(String.valueOf(noxbox.getPosition().getLatitude()));
                    ((ImageView) findViewById(R.id.travelTypeImage)).setImageResource(noxbox.getOwner().getTravelMode().getImage());
                    ((TextView) findViewById(R.id.travelTime)).setText(String.valueOf(minutes) + timeTxt);
                    ((TextView) findViewById(R.id.travelDistance)).setText(distanceTxt);
                }
            });

            findViewById(R.id.coordinatesSelect).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startCoordinateActivity();
                }
            });

            return;
        }
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

    private void startCoordinateActivity(){
        startActivity(new Intent(this,CoordinatePage.class));
    }
    private void init() {
        //TODO need find background picture for all types..
        // mBackImageTypeView = (ImageView) findViewById(R.id.backgroundImageTypeView);
    }

}
