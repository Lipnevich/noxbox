package by.nicolay.lipnevich.noxbox.detailed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Comment;
import by.nicolay.lipnevich.noxbox.model.MarketRole;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;
import by.nicolay.lipnevich.noxbox.state.ProfileStorage;
import by.nicolay.lipnevich.noxbox.tools.DateTimeFormatter;
import by.nicolay.lipnevich.noxbox.tools.Task;

import static by.nicolay.lipnevich.noxbox.tools.DateTimeFormatter.date;

public class DetailedActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);
    }

    @Override
    protected void onResume() {
        super.onResume();
        ProfileStorage.listenProfile(new Task<Profile>() {
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
        drawRating(profile.getViewed());
        drawPrice(profile.getViewed());
        drawButton(profile.getViewed().getRole());
        findViewById(R.id.acceptButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProfileStorage.listenProfile(new Task<Profile>() {
                    @Override
                    public void execute(Profile profile) {
                        profile.setCurrent(profile.getViewed());
                        finish();
                    }
                });
            }
        });
    }

    //TODO (vl) make textView instead title
    private void drawToolbar(Noxbox noxbox) {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Песни под гитару");
        //getSupportActionBar().setTitle(noxbox.getType().getName());
    }

    private void drawDescription(Noxbox noxbox) {
        drawDropdownElement(R.id.descriptionTitleLayout, R.id.descriptionLayout);
        changeArrowVector(R.id.descriptionLayout, R.id.descriptionArrow);
        if (noxbox.getRole() == MarketRole.supply) {
            ((TextView) findViewById(R.id.previousDescription)).setText(R.string.readyToPerform);
            ((TextView) findViewById(R.id.descriptionTitle)).setText(R.string.perform);
        } else {//TODO (vl) transfer text to xml
            ((TextView) findViewById(R.id.previousDescription)).setText(R.string.wantToGet);
            ((TextView) findViewById(R.id.descriptionTitle)).setText(R.string.willPay);
        }

        ((TextView) findViewById(R.id.date)).setText(getResources().getString(R.string.dateRegistrationService) + " " + date(noxbox.getTimeCreated()));

    }

    private void drawRating(Noxbox viewed) {
        drawDropdownElement(R.id.ratingTitleLayout, R.id.ratingLayout);
        changeArrowVector(R.id.ratingLayout, R.id.ratingArrow);
        Rating rating = viewed.getRole() == MarketRole.demand ?
                viewed.getOwner().getDemandsRating().get(viewed.getType().name()) : viewed.getOwner().getSuppliesRating().get(viewed.getType().name());

        int percentage = viewed.getOwner().ratingToPercentage();
        if (percentage >= 95) {
            ((ImageView) findViewById(R.id.ratingImage)).setColorFilter(Color.GREEN);
            ((ImageView) findViewById(R.id.ratingTitleImage)).setColorFilter(Color.GREEN);
        } else if (percentage > 90) {
            ((ImageView) findViewById(R.id.ratingImage)).setColorFilter(Color.YELLOW);
            ((ImageView) findViewById(R.id.ratingTitleImage)).setColorFilter(Color.YELLOW);
        } else {
            ((ImageView) findViewById(R.id.ratingImage)).setColorFilter(Color.RED);
            ((ImageView) findViewById(R.id.ratingTitleImage)).setColorFilter(Color.RED);
        }

        ((TextView) findViewById(R.id.ratingTitle)).setText(getResources().getString(R.string.myRating) + " " + viewed.getOwner().ratingToPercentage() + "%");
        ((TextView) findViewById(R.id.rating)).setText(viewed.getOwner().ratingToPercentage() + "%");
        ((TextView) findViewById(R.id.like)).setText(rating.getReceivedLikes() + " " + getResources().getString(R.string.like));
        ((TextView) findViewById(R.id.dislike)).setText(rating.getReceivedDislikes() + " " + getResources().getString(R.string.dislike));

        List<Comment> comments = new ArrayList<>();
        comments.add(rating.getComments().get("0"));
        comments.add(rating.getComments().get("1"));
        comments.add(rating.getComments().get("2"));

        RecyclerView recyclerView = findViewById(R.id.listComments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommentAdapter(comments));
    }

    private void drawWaitingTime(final Noxbox noxbox) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ((TextView) findViewById(R.id.travelTypeTitle)).setText("No permission");
            return;
        }

        drawDropdownElement(R.id.travelTypeTitleLayout, R.id.travelTypeLayout);
        changeArrowVector(R.id.travelTypeLayout, R.id.travelTypeArrow);
        ((ImageView) findViewById(R.id.travelTypeImageTitle)).setImageResource(noxbox.getOwner().getTravelMode().getImage());
        ((ImageView) findViewById(R.id.travelTypeImage)).setImageResource(noxbox.getOwner().getTravelMode().getImage());

        ProfileStorage.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                Geocoder geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                List<Address> addresses;
                String address = "";
                String city = "";

                try {
                    addresses = geocoder.getFromLocation(noxbox.getPosition().getLatitude(), noxbox.getPosition().getLongitude(), 1);
                    address = addresses.get(0).getAddressLine(0);
                    city = addresses.get(0).getLocality();
                } catch (IOException e) {
                    Crashlytics.log(Log.WARN, "Fail to create path", e.getMessage());
                    ((TextView) findViewById(R.id.address)).setText(noxbox.getPosition().getLatitude() + " " + noxbox.getPosition().getLongitude());
                }


                if (address.equals("")) {
                    if (city.equals("")) {
                        ((TextView) findViewById(R.id.address)).setText(noxbox.getPosition().getLatitude() + " " + noxbox.getPosition().getLongitude());
                    } else {
                        ((TextView) findViewById(R.id.address)).setText(city);
                    }
                } else {
                    ((TextView) findViewById(R.id.address)).setText(address);
                }

                float[] results = new float[1];

                Location.distanceBetween(
                        noxbox.getPosition().getLatitude(),
                        noxbox.getPosition().getLongitude(),
                        profile.getPosition().getLatitude(),
                        profile.getPosition().getLongitude(),
                        results);

                int minutes = (int) (results[0] / noxbox.getOwner().getTravelMode().getSpeedInMetersPerMinute());
                String timeTxt;
                String distanceTxt = String.valueOf((int) results[0] / 1000) + " " + getResources().getString(R.string.km);
                switch (minutes % 10) {
                    case 11: {
                        timeTxt = getResources().getString(R.string.minutes);
                        break;
                    }
                    case 1: {
                        timeTxt = getResources().getString(R.string.minute);
                        break;
                    }
                    case 2: {
                        timeTxt = getResources().getString(R.string.minutes_);
                        break;
                    }
                    case 3: {
                        timeTxt = getResources().getString(R.string.minutes_);
                        break;
                    }
                    case 4: {
                        timeTxt = getResources().getString(R.string.minutes_);
                        break;
                    }
                    default: {
                        timeTxt = getResources().getString(R.string.minutes);
                        break;
                    }
                }

                String displayTime = DateTimeFormatter.format(noxbox.getWorkSchedule().getStartTime().getHourOfDay(), noxbox.getWorkSchedule().getStartTime().getMinuteOfHour()) + " - " +
                        DateTimeFormatter.format(noxbox.getWorkSchedule().getEndTime().getHourOfDay(), noxbox.getWorkSchedule().getEndTime().getMinuteOfHour());
                ((TextView) findViewById(R.id.offerTime)).setText(R.string.validityOfTheOffer);
                ((TextView) findViewById(R.id.time)).setText(displayTime);

                //TODO (vl) нужно добавить сравнение текущего времени и времени оказания услуги
                if (noxbox.getOwner().getTravelMode() == TravelMode.none) {
                    ((TextView) findViewById(R.id.travelTypeTitle)).setText(R.string.byAddress);
                    ((TextView) findViewById(R.id.travelMode)).setText(R.string.waitingByAddress);

                } else if (noxbox.getOwner().getTravelMode() != TravelMode.none) {
                    ((TextView) findViewById(R.id.travelTypeTitle)).setText(getString(R.string.across) + " " + String.valueOf(minutes) + " " + timeTxt);
                    ((TextView) findViewById(R.id.travelMode)).setText(R.string.willArriveAtTheAddress);
                    findViewById(R.id.coordinatesSelect).setVisibility(View.VISIBLE);
                    findViewById(R.id.coordinatesSelect).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startCoordinateActivity();
                        }
                    });
                }
            }
        });


    }

    private void drawPrice(Noxbox noxbox) {
        drawDropdownElement(R.id.priceTitleLayout, R.id.priceLayout);
        changeArrowVector(R.id.priceLayout, R.id.priceArrow);
        ((TextView) findViewById(R.id.priceTitle)).setText(getResources().getString(R.string.priceTxt) + " " + noxbox.getPrice() + " " + getResources().getString(R.string.currency));
        ((TextView) findViewById(R.id.price)).setText(noxbox.getPrice());
        ((TextView) findViewById(R.id.descriptionTextInPrice)).setText(noxbox.getType().getDescription());

        String description = getResources().getString(noxbox.getType().getDescription());
        String serviceDescription = "";
        int countSpace = 0;
        for(int i = 0; i<description.length();i++){
            if(description.charAt(i)==' '){
                countSpace++;
                if(countSpace > 1){
                    serviceDescription = serviceDescription.concat(getResources().getString(R.string.ending));
                    break;
                }
            }
            serviceDescription = serviceDescription.concat(String.valueOf(description.charAt(i)));
        }

        ((TextView) findViewById(R.id.clarificationTextInPrice)).setText(getResources().getString(R.string.priceClarificationBefore) + " " + serviceDescription + " " + getResources().getString(R.string.priceClarificationAfter));
        ((ImageView) findViewById(R.id.typeImageInPrice)).setImageResource(noxbox.getType().getImage());
        //TODO (vl) create copyButton with lower price
    }

    private void drawButton(MarketRole role) {
        if (role == MarketRole.demand) {
            ((Button) findViewById(R.id.acceptButton)).setText(R.string.proceed);
        } else if (role == MarketRole.supply) {
            ((Button) findViewById(R.id.acceptButton)).setText(R.string.order);
        }
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

    private void startCoordinateActivity() {
        startActivity(new Intent(this, CoordinateActivity.class));
    }

    private void init() {
        //TODO need find background picture for all types..
        // mBackImageTypeView = (ImageView) findViewById(R.id.backgroundImageTypeView);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ProfileStorage.listenProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                profile.setViewed(null);
                finish();
            }
        });
    }
}
