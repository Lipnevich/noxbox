package live.noxbox.activities.detailed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.database.GeoRealtime;
import live.noxbox.menu.profile.ImageListAdapter;
import live.noxbox.model.ImageType;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.model.TravelMode;
import live.noxbox.tools.AddressManager;
import live.noxbox.tools.BalanceCalculator;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.GyroscopeObserver;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.PanoramaImageView;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.activities.detailed.CoordinateActivity.COORDINATE;
import static live.noxbox.activities.detailed.CoordinateActivity.LAT;
import static live.noxbox.activities.detailed.CoordinateActivity.LNG;
import static live.noxbox.analitics.BusinessEvent.accept;
import static live.noxbox.analitics.BusinessEvent.cancel;
import static live.noxbox.analitics.BusinessEvent.read;
import static live.noxbox.analitics.BusinessEvent.request;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.BottomSheetDialog.openNameNotVerifySheetDialog;
import static live.noxbox.tools.BottomSheetDialog.openPhotoNotVerifySheetDialog;
import static live.noxbox.tools.BottomSheetDialog.openWalletAddressSheetDialog;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;
import static live.noxbox.tools.LocationPermitOperator.getLocationPermission;
import static live.noxbox.tools.LocationPermitOperator.isLocationPermissionGranted;

public class DetailedActivity extends AppCompatActivity {
    private GyroscopeObserver gyroscopeObserver;
    private Profile profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        PanoramaImageView panoramaImageView = findViewById(R.id.illustration);
        gyroscopeObserver = new GyroscopeObserver();
        // Set the maximum radian the device should rotate to show image's bounds.
        // It should be set between 0 and π/2.
        // The default value is π/9.
        gyroscopeObserver.setMaxRotateRadian(Math.PI / 4);
        gyroscopeObserver.addPanoramaImageView(panoramaImageView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscopeObserver.register(this);
        AppCache.listenProfile(DetailedActivity.class.getName(), profile -> {
            if (profile.getCurrent().getTimeRequested() == 0) {
                BusinessActivity.businessEvent(read);
            }

            AppCache.startListenNoxbox(profile.getViewed().getId());
            if (profile.getViewed().getParty() == null) {
                profile.getViewed().setParty(profile.privateInfo());
            }
            draw(profile);
            checkBalance(profile, DetailedActivity.this);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(DetailedActivity.class.getName());
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) AppCache.stopListenNoxbox(user.getUid());

        gyroscopeObserver.unregister();
    }

    private void draw(Profile profile) {
        this.profile = profile;
        drawToolbar(profile.getViewed());
        drawOppositeProfile(profile);
        drawDescription(profile);
        drawOwnerComment(profile);
        drawWaitingTime(profile);
        drawRating(profile.getViewed());
        drawPrice(profile);
        drawButtons(profile);

        if (profile.getViewed().getRole() == MarketRole.supply) {
            drawCertificate(profile.getViewed());
            drawWorkSample(profile.getViewed());
        }
    }

    private void drawToolbar(Noxbox noxbox) {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(noxbox.getType().getName());

        //((ImageView) findViewById(R.id.illustration)).setImageResource(noxbox.getType().getIllustration());

        Glide.with(DetailedActivity.this)
                .asDrawable()
                .load(noxbox.getType().getIllustration())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.AUTOMATIC))
                .into(new SimpleTarget<Drawable>() {
                    @Override
                    public void onResourceReady(@NonNull Drawable drawable, @Nullable Transition<? super Drawable> transition) {
                        ((ImageView) findViewById(R.id.illustration)).setImageDrawable(drawable);
                    }
                });
    }

    private void drawOppositeProfile(Profile me) {
        Profile other = me.getViewed().getNotMe(me.getId());
        if (other.getName() != null && other.getPhoto() != null) {
            findViewById(R.id.profileLayout).setVisibility(View.VISIBLE);
            ImageManager.createCircleProfilePhotoFromUrl(this, other.getPhoto(), ((ImageView) findViewById(R.id.profilePhoto)));
            ((TextView) findViewById(R.id.profileName)).setText(other.getName());
        }
    }

    private void drawDescription(Profile profile) {
        drawDropdownElement(R.id.descriptionTitleLayout, R.id.descriptionLayout);
        changeArrowVector(R.id.descriptionLayout, R.id.descriptionArrow);

        if (profile.getViewed().getRole() == MarketRole.supply) {
            if (profile.getViewed().getOwner().equals(profile)) {
                ((TextView) findViewById(R.id.descriptionTitle)).setText(R.string.willPay);
            } else {
                ((TextView) findViewById(R.id.descriptionTitle)).setText(R.string.perform);
            }
        } else {
            if (profile.getViewed().getOwner().equals(profile)) {
                ((TextView) findViewById(R.id.descriptionTitle)).setText(R.string.perform);
            } else {
                ((TextView) findViewById(R.id.descriptionTitle)).setText(R.string.willPay);
            }
        }
        ((ImageView) findViewById(R.id.typeImage)).setImageResource(profile.getViewed().getType().getImage());
        ((TextView) findViewById(R.id.serviceDescription)).setText(getText(profile.getViewed().getType().getDescription()));
    }

    private void drawOwnerComment(Profile me) {
        if (!me.getViewed().getOwner().equals(me)) {
            if (me.getViewed().getOwnerComment().length() > 0) {
                ((TextView) findViewById(R.id.ownerComment)).setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.ownerComment)).setText(me.getViewed().getOwnerComment());
            } else {

                findViewById(R.id.commentView).setVisibility(View.GONE);
            }
        }

    }

    private void drawRating(Noxbox viewed) {
        drawDropdownElement(R.id.ratingTitleLayout, R.id.ratingLayout);
        changeArrowVector(R.id.ratingLayout, R.id.ratingArrow);
        Rating rating = viewed.getRole() == MarketRole.demand ?
                viewed.getOwner().getDemandsRating().get(viewed.getType().name())
                : viewed.getOwner().getSuppliesRating().get(viewed.getType().name());

        if (rating == null) {
            rating = new Rating();
        }
        int percentage = viewed.getOwner().ratingToPercentage(viewed.getRole(), viewed.getType());
        if (percentage >= 95) {
            ((ImageView) findViewById(R.id.ratingImage)).setColorFilter(Color.GREEN);
        } else if (percentage > 90) {
            ((ImageView) findViewById(R.id.ratingImage)).setColorFilter(Color.YELLOW);
        } else {
            ((ImageView) findViewById(R.id.ratingImage)).setColorFilter(Color.RED);
        }

        ((TextView) findViewById(R.id.ratingTitle)).setText(getResources().getString(R.string.myRating) + " " + viewed.getOwner().ratingToPercentage(viewed.getRole(), viewed.getType()) + "%");
        ((TextView) findViewById(R.id.rating)).setText(viewed.getOwner().ratingToPercentage(viewed.getRole(), viewed.getType()) + "%");
        ((TextView) findViewById(R.id.like)).setText(rating.getReceivedLikes() + " " + getResources().getString(R.string.like));
        ((TextView) findViewById(R.id.dislike)).setText(rating.getReceivedDislikes() + " " + getResources().getString(R.string.dislike));

        RecyclerView recyclerView = findViewById(R.id.listComments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommentAdapter(rating.getComments().values()));
    }

    private void drawWaitingTime(final Profile profile) {
        if (ContextCompat.checkSelfPermission(DetailedActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        drawDropdownElement(R.id.travelTypeTitleLayout, R.id.travelTypeLayout);
        changeArrowVector(R.id.travelTypeLayout, R.id.travelTypeArrow);
        ((ImageView) findViewById(R.id.travelTypeImageTitle)).setImageResource(profile.getViewed().getOwner().getTravelMode().getImage());
        ((ImageView) findViewById(R.id.travelTypeImage)).setImageResource(profile.getViewed().getOwner().getTravelMode().getImage());

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String address = AddressManager.provideAddressByPosition(getApplicationContext(), profile.getViewed().getPosition());
                return address;
            }

            @Override
            protected void onPostExecute(String address) {
                ((TextView) findViewById(R.id.address)).setText(address);
            }
        }.execute();

        TravelMode travelMode;
        if (profile.getViewed().getOwner().getTravelMode() == none) {
            travelMode = profile.getViewed().getParty().getTravelMode();
        } else {
            travelMode = profile.getViewed().getOwner().getTravelMode();
        }

        String displayTime = DateTimeFormatter.format(profile.getViewed().getWorkSchedule().getStartTime().getHourOfDay(), profile.getViewed().getWorkSchedule().getStartTime().getMinuteOfHour()) + " - " +
                DateTimeFormatter.format(profile.getViewed().getWorkSchedule().getEndTime().getHourOfDay(), profile.getViewed().getWorkSchedule().getEndTime().getMinuteOfHour());
        ((TextView) findViewById(R.id.offerTime)).setText(R.string.validityOfTheOffer);
        ((TextView) findViewById(R.id.time)).setText(displayTime);

        if (profile.getViewed().getOwner().getTravelMode() == none) {
            ((TextView) findViewById(R.id.travelTypeTitle)).setText(R.string.byAddress);
            ((TextView) findViewById(R.id.travelMode)).setText(R.string.waitingByAddress);
        } else {
            int minutes = (int) getTimeInMinutesBetweenUsers(profile.getViewed().getOwner().getPosition(), profile.getViewed().getParty().getPosition(), travelMode);

            String timeTxt;
            switch (minutes % 10) {
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


            ((TextView) findViewById(R.id.travelTypeTitle)).setText(getString(R.string.across) + " " + String.valueOf(minutes) + " " + timeTxt);

            ((TextView) findViewById(R.id.travelMode)).setText(R.string.willArriveAtTheAddress);

            if (!isNullOrZero(profile.getViewed().getTimeRequested())) {
                findViewById(R.id.coordinatesSelect).setVisibility(View.GONE);
            } else {
                findViewById(R.id.coordinatesSelect).setVisibility(View.VISIBLE);
                findViewById(R.id.coordinatesSelect).setOnClickListener(v -> startCoordinateActivity());
            }
        }
    }

    private void drawPrice(Profile profile) {
        drawDropdownElement(R.id.priceTitleLayout, R.id.priceLayout);
        changeArrowVector(R.id.priceLayout, R.id.priceArrow);

        String priceTitle = getResources().getString(R.string.priceTxt) + " " + profile.getViewed().getPrice() + " " + AppCache.showPriceInUsd(getString(R.string.currency), profile.getViewed().getPrice());

        ((TextView) findViewById(R.id.priceTitle)).setText(priceTitle);
        ((TextView) findViewById(R.id.descriptionTextInPrice)).setText(profile.getViewed().getType().getDuration());

        TextView priceView = findViewById(R.id.price);
        priceView.setText(profile.getViewed().getPrice());

        String duration = getResources().getString(profile.getViewed().getType().getDuration());
        String serviceDescription = "";
        int countSpace = 0;
        for (int i = 0; i < duration.length(); i++) {
            if (duration.charAt(i) == ' ') {
                countSpace++;
                if (countSpace > 1) {
                    serviceDescription = serviceDescription.concat(getResources().getString(R.string.ending));
                    break;
                }
            }
            serviceDescription = serviceDescription.concat(String.valueOf(duration.charAt(i)));
        }
        String desc = getResources().getString(R.string.priceClarificationBefore) + " " + serviceDescription + " " + getResources().getString(R.string.priceClarificationAfter);
        ((TextView) findViewById(R.id.clarificationTextInPrice)).setText(desc);
        ((ImageView) findViewById(R.id.typeImageInPrice)).setImageResource(profile.getViewed().getType().getImage());
    }

    private void drawButtons(Profile profile) {
        switch (NoxboxState.getState(profile.getViewed(), profile)) {
            case created:
                drawJoinButton(profile);
                break;
            case accepting:
                drawAcceptButton(profile);
            case moving:
            case requesting:
                drawCancelButton(profile);
                break;
        }
    }

    private void drawAcceptButton(final Profile profile) {
        findViewById(R.id.acceptButton).setVisibility(View.VISIBLE);

        if (!profile.getAcceptance().isAccepted()) {
            findViewById(R.id.acceptButton).setOnClickListener(v -> openPhotoNotVerifySheetDialog(DetailedActivity.this));
        } else {
            findViewById(R.id.acceptButton).setOnClickListener(v -> {
                v.setVisibility(View.GONE);
                profile.getCurrent().setTimeAccepted(System.currentTimeMillis());
                AppCache.updateNoxbox();

                BusinessActivity.businessEvent(accept);

                Router.finishActivity(DetailedActivity.this);
            });
        }
    }

    private void drawJoinButton(final Profile profile) {

        findViewById(R.id.joinButton).setVisibility(View.VISIBLE);
        if (profile.getViewed().getRole() == MarketRole.supply) {
            ((Button) findViewById(R.id.joinButton)).setText(R.string.order);
        } else {
            ((Button) findViewById(R.id.joinButton)).setText(R.string.proceed);
        }

        findViewById(R.id.joinButton).setOnClickListener(v -> {
            if (!isLocationPermissionGranted(DetailedActivity.this)) {
                getLocationPermission(DetailedActivity.this, LOCATION_PERMISSION_REQUEST_CODE);

            } else {
                v.setVisibility(View.GONE);
                sendRequestToNoxbox(profile);
            }

        });


    }

    private void sendRequestToNoxbox(Profile profile) {
        if (!profile.getAcceptance().isAccepted()) {
            openPhotoNotVerifySheetDialog(DetailedActivity.this);
            return;
        }
        if (profile.getViewed().getRole() == MarketRole.supply && !BalanceCalculator.enoughBalance(profile.getViewed(), profile)) {
            findViewById(R.id.joinButton).setBackground(getResources().getDrawable(R.drawable.button_corner_disabled));
            openWalletAddressSheetDialog(DetailedActivity.this, profile);
            return;
        }

        if (profile.getName() != null && profile.getName().length() == 0) {
            openNameNotVerifySheetDialog(DetailedActivity.this, profile);
            return;
        }

        BusinessActivity.businessEvent(request);

        profile.setCurrent(profile.getViewed());
        profile.setNoxboxId(profile.getCurrent().getId());
        //profile.getCurrent().setAvailable(false);
        profile.getCurrent().setTimeRequested(System.currentTimeMillis());

        AppCache.updateNoxbox();

        GeoRealtime.offline(profile.getCurrent());

        if (AppCache.availableNoxboxes.get(profile.getNoxboxId()) != null) {
            AppCache.availableNoxboxes.remove(profile.getNoxboxId());
        }
        Router.finishActivity(DetailedActivity.this);
    }

    private RadioButton longToWait;
    private RadioButton photoDoesNotMatch;
    private RadioButton rudeBehavior;
    private RadioButton substandardWork;
    private RadioButton own;

    private String cancellationReason;

    private void drawCancelButton(final Profile profile) {
        if (!isNullOrZero(profile.getCurrent().getTimeAccepted())) return;
        findViewById(R.id.cancelButton).setVisibility(View.VISIBLE);
        findViewById(R.id.cancelButton).setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(DetailedActivity.this);
            final View view = getLayoutInflater().inflate(R.layout.dialog_cancellation_reason, null);
            longToWait = view.findViewById(R.id.longToWait);
            photoDoesNotMatch = view.findViewById(R.id.photoDoesNotMatch);
            rudeBehavior = view.findViewById(R.id.rudeBehavior);
            substandardWork = view.findViewById(R.id.substandardWork);
            own = view.findViewById(R.id.own);
            own.setChecked(true);
            longToWait.setOnClickListener(v1 -> {
                view.findViewById(R.id.ownReason).setEnabled(false);
                view.findViewById(R.id.send).setEnabled(true);
                cancellationReason = (String) longToWait.getText();
            });
            photoDoesNotMatch.setOnClickListener(v12 -> {
                view.findViewById(R.id.ownReason).setEnabled(false);
                view.findViewById(R.id.send).setEnabled(true);
                cancellationReason = (String) photoDoesNotMatch.getText();
            });
            rudeBehavior.setOnClickListener(v13 -> {
                view.findViewById(R.id.ownReason).setEnabled(false);
                view.findViewById(R.id.send).setEnabled(true);
                cancellationReason = (String) rudeBehavior.getText();

            });
            substandardWork.setOnClickListener(v14 -> {
                view.findViewById(R.id.ownReason).setEnabled(false);
                view.findViewById(R.id.send).setEnabled(true);
                cancellationReason = (String) substandardWork.getText();
            });
            own.setOnClickListener(v15 -> {

                view.findViewById(R.id.ownReason).setEnabled(true);
                ((EditText) view.findViewById(R.id.ownReason)).addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        cancellationReason = s.toString();
                        if (s.length() > 0) {
                            view.findViewById(R.id.send).setEnabled(true);
                            view.findViewById(R.id.send).setBackground(getDrawable(R.drawable.button_corner));
                        } else {
                            view.findViewById(R.id.send).setEnabled(false);
                            view.findViewById(R.id.send).setBackground(getDrawable(R.drawable.button_corner_disabled));
                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
            });

            builder.setView(view);
            final AlertDialog alertDialog = builder.create();
            alertDialog.show();

            view.findViewById(R.id.send).setOnClickListener(v16 -> {
                BusinessActivity.businessEvent(cancel);
                if (profile.getViewed().getOwner().getId().equals(profile.getId())) {
                    profile.getViewed().setTimeCanceledByOwner(System.currentTimeMillis());
                } else {
                    profile.getViewed().setTimeCanceledByParty(System.currentTimeMillis());
                }
                profile.getViewed().setCancellationReasonMessage(cancellationReason);
                AppCache.updateNoxbox();
                alertDialog.cancel();
                Router.finishActivity(DetailedActivity.this);
            });

        });
    }

    private void drawDropdownElement(int titleId, final int contentId) {
        findViewById(titleId).setOnClickListener(v -> {
            if (findViewById(contentId).isShown()) {
                findViewById(contentId).setVisibility(View.GONE);
                findViewById(contentId).setElevation(0);
                findViewById(contentId).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_up));
            } else {
                findViewById(contentId).setVisibility(View.VISIBLE);
                findViewById(contentId).startAnimation(AnimationUtils.loadAnimation(getApplicationContext(), R.anim.slide_down));
            }
        });
    }

    private void drawCertificate(Noxbox noxbox) {
        if (noxbox.getOwner().getPortfolio().get(noxbox.getType().name()) == null) return;

        findViewById(R.id.certificateLayout).setVisibility(View.VISIBLE);
        List<String> certificateUrlList = noxbox.getOwner().getPortfolio().get(noxbox.getType().name()).getImages().get(ImageType.certificates.name());


        RecyclerView certificateList = findViewById(R.id.certificatesList);
        certificateList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        certificateList.setAdapter(new ImageListAdapter(certificateUrlList, this, ImageType.certificates, noxbox.getType()));
    }

    private void drawWorkSample(Noxbox noxbox) {
        if (noxbox.getOwner().getPortfolio().get(noxbox.getType().name()) == null) return;

        findViewById(R.id.workSampleLayout).setVisibility(View.VISIBLE);
        List<String> workSampleUrlList = noxbox.getOwner().getPortfolio().get(noxbox.getType().name()).getImages().get(ImageType.samples.name());

        RecyclerView workSampleList = findViewById(R.id.workSampleList);
        workSampleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        workSampleList.setAdapter(new ImageListAdapter(workSampleUrlList, this, ImageType.samples, noxbox.getType()));
    }

    private void changeArrowVector(int layout, final int element) {
        final ViewGroup listeningLayout = findViewById(layout);
        listeningLayout.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (listeningLayout.getVisibility() == View.VISIBLE) {
                ((ImageView) findViewById(element)).setImageResource(R.drawable.arrow_up);
            } else if (listeningLayout.getVisibility() == View.GONE) {
                ((ImageView) findViewById(element)).setImageResource(R.drawable.arrow_down);
            }
        });

    }

    private void startCoordinateActivity() {
        startActivityForResult(new Intent(this, CoordinateActivity.class), COORDINATE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case android.R.id.home:
                switch (NoxboxState.getState(profile.getViewed(), profile)) {
                    case created:
                        profile.setViewed(null);
                        break;
                    case accepting:
                    case moving:
                    case requesting:
                        break;
                }

                Router.finishActivity(DetailedActivity.this);
                break;

        }

        return true;
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppCache.readProfile(profile -> {
            if (requestCode == COORDINATE && resultCode == RESULT_OK) {
                final Position position = new Position(data.getExtras().getDouble(LAT), data.getExtras().getDouble(LNG));
                profile.getViewed().setPosition(position);
                profile.getViewed().getProfileWhoComes();
            }
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    AppCache.readProfile(profile -> sendRequestToNoxbox(profile));
                }
            }
        }
    }

}
