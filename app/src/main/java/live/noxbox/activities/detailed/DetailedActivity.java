package live.noxbox.activities.detailed;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
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
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.math.BigDecimal;
import java.util.List;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.analitics.BusinessEvent;
import live.noxbox.cluster.ClusterItemsActivity;
import live.noxbox.cluster.NoxboxMarker;
import live.noxbox.database.AppCache;
import live.noxbox.database.Firestore;
import live.noxbox.menu.profile.ImageListAdapter;
import live.noxbox.model.ImageType;
import live.noxbox.model.MarketRole;
import live.noxbox.model.Noxbox;
import live.noxbox.model.NoxboxState;
import live.noxbox.model.Position;
import live.noxbox.model.Profile;
import live.noxbox.model.Rating;
import live.noxbox.states.Accepting;
import live.noxbox.tools.AddressManager;
import live.noxbox.tools.BalanceCalculator;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.GyroscopeObserver;
import live.noxbox.tools.ImageManager;
import live.noxbox.tools.MoneyFormatter;
import live.noxbox.tools.PanoramaImageView;
import live.noxbox.tools.Router;

import static live.noxbox.Constants.LOCATION_PERMISSION_REQUEST_CODE;
import static live.noxbox.activities.detailed.CoordinateActivity.COORDINATE;
import static live.noxbox.activities.detailed.CoordinateActivity.LAT;
import static live.noxbox.activities.detailed.CoordinateActivity.LNG;
import static live.noxbox.analitics.BusinessEvent.cancel;
import static live.noxbox.analitics.BusinessEvent.read;
import static live.noxbox.analitics.BusinessEvent.request;
import static live.noxbox.database.AppCache.profile;
import static live.noxbox.database.AppCache.startListenNoxbox;
import static live.noxbox.database.AppCache.stopListenNoxbox;
import static live.noxbox.database.Firestore.isFinished;
import static live.noxbox.database.GeoRealtime.offline;
import static live.noxbox.model.Noxbox.isNullOrZero;
import static live.noxbox.model.TravelMode.none;
import static live.noxbox.tools.BalanceChecker.checkBalance;
import static live.noxbox.tools.BottomSheetDialog.openNameNotVerifySheetDialog;
import static live.noxbox.tools.BottomSheetDialog.openPhotoNotVerifySheetDialog;
import static live.noxbox.tools.BottomSheetDialog.openWalletAddressSheetDialog;
import static live.noxbox.tools.DateTimeFormatter.getFormatTimeFromMillis;
import static live.noxbox.tools.LocationCalculator.getTimeInMinutesBetweenUsers;
import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;
import static live.noxbox.tools.location.LocationOperator.startLocationPermissionRequest;

public class DetailedActivity extends BaseActivity {

    private GyroscopeObserver gyroscopeObserver;

    //ui
    private Toolbar toolbar;
    private PanoramaImageView illustration;
    private LinearLayout profileLayout;
    private TextView profileName;
    private ImageView profilePhoto;
    private LinearLayout descriptionTitleLayout;
    private LinearLayout descriptionLayout;
    private ImageView descriptionArrow;
    private TextView descriptionTitle;
    private ImageView typeImage;
    private TextView serviceDescription;
    private LinearLayout commentView;
    private TextView contractComment;
    private LinearLayout ratingTitleLayout;
    private LinearLayout ratingLayout;
    private ImageView ratingArrow;
    private ImageView ratingImage;
    private TextView ratingTitle;
    private TextView rating;
    private TextView like;
    private TextView dislike;
    private LinearLayout travelTypeTitleLayout;
    private LinearLayout travelTypeLayout;
    private ImageView travelTypeArrow;
    private ImageView travelTypeImageTitle;
    private ImageView travelTypeImage;
    private TextView address;
    private TextView offerTime;
    private TextView time;
    private TextView travelTypeTitle;
    private TextView travelModeText;
    private LinearLayout coordinatesSelect;
    private LinearLayout priceTitleLayout;
    private LinearLayout priceLayout;
    private ImageView priceArrow;
    private TextView priceTitle;
    private TextView descriptionTextInPrice;
    private TextView clarificationTextInPrice;
    private ImageView typeImageInPrice;
    private Button acceptButton;
    private Button joinButton;
    private Button cancelButton;
    private RelativeLayout certificateLayout;
    private RelativeLayout workSampleLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailed);

        initializeUi();
    }

    private void initializeUi() {
        illustration = findViewById(R.id.illustration);
        gyroscopeObserver = new GyroscopeObserver();
        // Set the maximum radian the device should rotate to show image's bounds.
        // It should be set between 0 and π/2.
        // The default value is π/9.
        gyroscopeObserver.setMaxRotateRadian(Math.PI / 4);
        gyroscopeObserver.addPanoramaImageView(illustration);

        toolbar = findViewById(R.id.toolbar);
        profileLayout = findViewById(R.id.profileLayout);
        profileName = findViewById(R.id.profileName);
        profilePhoto = findViewById(R.id.profilePhoto);
        descriptionTitleLayout = findViewById(R.id.descriptionTitleLayout);
        descriptionLayout = findViewById(R.id.descriptionLayout);
        descriptionArrow = findViewById(R.id.descriptionArrow);
        descriptionTitle = findViewById(R.id.descriptionTitle);
        typeImage = findViewById(R.id.typeImage);
        serviceDescription = findViewById(R.id.serviceDescription);
        commentView = findViewById(R.id.commentView);
        contractComment = findViewById(R.id.contractComment);
        ratingTitleLayout = findViewById(R.id.ratingTitleLayout);
        ratingLayout = findViewById(R.id.ratingLayout);
        ratingArrow = findViewById(R.id.ratingArrow);
        ratingImage = findViewById(R.id.ratingImage);
        ratingTitle = findViewById(R.id.ratingTitle);
        rating = findViewById(R.id.rating);
        like = findViewById(R.id.like);
        dislike = findViewById(R.id.dislike);
        travelTypeTitleLayout = findViewById(R.id.travelTypeTitleLayout);
        travelTypeLayout = findViewById(R.id.travelTypeLayout);
        travelTypeArrow = findViewById(R.id.travelTypeArrow);
        travelTypeImageTitle = findViewById(R.id.travelTypeImageTitle);
        travelTypeImage = findViewById(R.id.travelTypeImage);
        address = findViewById(R.id.address);
        offerTime = findViewById(R.id.offerTime);
        time = findViewById(R.id.time);
        travelTypeTitle = findViewById(R.id.travelTypeTitle);
        travelModeText = findViewById(R.id.travelModeText);
        coordinatesSelect = findViewById(R.id.coordinatesSelect);
        priceTitleLayout = findViewById(R.id.priceTitleLayout);
        priceLayout = findViewById(R.id.priceLayout);
        priceArrow = findViewById(R.id.priceArrow);
        priceTitle = findViewById(R.id.priceTitle);
        descriptionTextInPrice = findViewById(R.id.descriptionTextInPrice);
        clarificationTextInPrice = findViewById(R.id.clarificationTextInPrice);
        typeImageInPrice = findViewById(R.id.typeImageInPrice);
        acceptButton = findViewById(R.id.acceptButton);
        joinButton = findViewById(R.id.joinButton);
        cancelButton = findViewById(R.id.cancelButton);
        certificateLayout = findViewById(R.id.certificateLayout);
        workSampleLayout = findViewById(R.id.workSampleLayout);
        gyroscopeObserver.addPanoramaImageView(illustration);

        if (profile().getCurrent().getTimeRequested() == 0) {
            BusinessActivity.businessEvent(read);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        gyroscopeObserver.register(this);
        AppCache.listenProfile(DetailedActivity.class.getName(), profile -> {
            if (!isFinished(profile.getCurrent())
                    && profile.getNoxboxId().equals(profile.getCurrent().getId())
                    && isNullOrZero(profile.getCurrent().getTimeRequested())) {
                stopListenNoxbox(profile.getCurrent().getId());
            }
            startListenNoxbox(profile.getViewed().getId());
            if (profile.getViewed().getParty() == null) {
                profile.getViewed().setParty(profile.publicInfo(profile.getViewed().getRole() == MarketRole.demand ? MarketRole.supply : MarketRole.demand, profile.getViewed().getType()));
            }
            if (resultPosition != null && profile.getViewed() != null) {
                profile.getViewed().setPosition(resultPosition);
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

        if (user != null && !profile().getCurrent().equals(profile().getViewed())) {
            AppCache.stopListenNoxbox(profile().getViewed().getId());
        }

        gyroscopeObserver.unregister();
    }

    private void draw(Profile profile) {
        drawToolbar(profile.getViewed());
        drawOppositeProfile(profile);
        drawDescription(profile);
        drawContractComment(profile);
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
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle(noxbox.getType().getName());

        //illustration.setImageResource(noxbox.getType().getIllustration());

        Glide.with(this)
                .asDrawable()
                .load(noxbox.getType().getIllustration())
                .into(new ImageViewTarget<Drawable>(illustration) {
                    @Override
                    protected void setResource(@Nullable Drawable drawable) {
                        illustration.setImageDrawable(drawable);
                    }
                });
    }

    private void drawOppositeProfile(Profile me) {
        Profile other = me.getViewed().getNotMe(me.getId());
        if (!other.getName().isEmpty() && !other.getPhoto().isEmpty()) {
            profileLayout.setVisibility(View.VISIBLE);
            ImageManager.createCircleProfilePhotoFromUrl(this, other.getPhoto(), profilePhoto);
            profileName.setText(other.getName());
        } else {
            profileLayout.setVisibility(View.GONE);
        }
    }

    private void drawDescription(Profile profile) {
        drawDropdownElement(descriptionTitleLayout.getId(), descriptionLayout.getId());
        changeArrowVector(descriptionLayout.getId(), descriptionArrow.getId());

        if (profile.getViewed().getRole() == MarketRole.supply) {
            if (profile.getViewed().getOwner().equals(profile)) {
                descriptionTitle.setText(R.string.willPay);
            } else {
                descriptionTitle.setText(R.string.perform);
            }
        } else {
            if (profile.getViewed().getOwner().equals(profile)) {
                descriptionTitle.setText(R.string.perform);
            } else {
                descriptionTitle.setText(R.string.willPay);
            }
        }
        typeImage.setImageResource(profile.getViewed().getType().getImageDemand());
        serviceDescription.setText(getText(profile.getViewed().getType().getDescription()));
    }


    private void drawContractComment(Profile me) {
        if (!me.getViewed().getOwner().equals(me)) {
            if (me.getViewed().getContractComment().length() > 0) {
                commentView.setVisibility(View.VISIBLE);
                contractComment.setText(me.getViewed().getContractComment());
            } else {
                commentView.setVisibility(View.GONE);
            }
        }

    }

    private void drawRating(Noxbox viewed) {
        drawDropdownElement(ratingTitleLayout.getId(), ratingLayout.getId());
        changeArrowVector(ratingLayout.getId(), ratingArrow.getId());
        int percentage;
        Rating mateRating;
        if (profile().equals(viewed.getOwner())) {
            percentage = viewed.getParty().ratingToPercentage(viewed.getRole(), viewed.getType());
            mateRating = viewed.getRole() == MarketRole.demand ?
                    viewed.getParty().getDemandsRating().get(viewed.getType().name())
                    : viewed.getParty().getSuppliesRating().get(viewed.getType().name());

        } else {
            percentage = viewed.getOwner().ratingToPercentage(viewed.getRole(), viewed.getType());
            mateRating = viewed.getRole() == MarketRole.demand ?
                    viewed.getOwner().getDemandsRating().get(viewed.getType().name())
                    : viewed.getOwner().getSuppliesRating().get(viewed.getType().name());
        }

        if (mateRating == null) {
            mateRating = new Rating();
        }

        if (percentage >= 95) {
            ratingImage.setColorFilter(Color.GREEN);
        } else if (percentage > 90) {
            ratingImage.setColorFilter(Color.YELLOW);
        } else {
            ratingImage.setColorFilter(Color.RED);
        }


        ratingTitle.setText(getResources().getString(R.string.myRating) + " " + viewed.getOwner().ratingToPercentage(viewed.getRole(), viewed.getType()) + "%");
        rating.setText(viewed.getOwner().ratingToPercentage(viewed.getRole(), viewed.getType()) + "%");
        like.setText(mateRating.getReceivedLikes() + " " + getResources().getString(R.string.like));
        dislike.setText(mateRating.getReceivedDislikes() + " " + getResources().getString(R.string.dislike));
        //TODO (vl) for supply and demand
        RecyclerView recyclerView = findViewById(R.id.listComments);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new CommentAdapter(mateRating.getComments().values()));
    }

    private void drawWaitingTime(final Profile profile) {
//        if (ContextCompat.checkSelfPermission(DetailedActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            return;
//        }

        Noxbox viewed = profile.getViewed();

        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                String address = AddressManager.provideAddressByPosition(getApplicationContext(), viewed.getPosition());
                return address;
            }

            @Override
            protected void onPostExecute(String resultAddress) {
                address.setText(resultAddress);
            }
        }.execute();
        drawDropdownElement(travelTypeTitleLayout.getId(), travelTypeLayout.getId());
        changeArrowVector(travelTypeLayout.getId(), travelTypeArrow.getId());

        switch (NoxboxState.getState(viewed, profile)) {
            case created: {
                travelTypeImageTitle.setImageResource(viewed.getOwner().getTravelMode().getImage());
                travelTypeImage.setImageResource(viewed.getOwner().getTravelMode().getImage());
                coordinatesSelect.setVisibility(View.VISIBLE);
                coordinatesSelect.setOnClickListener(v -> startCoordinateActivity());
                if (viewed.getOwner().getTravelMode() == none) {
                    travelTypeTitle.setText(R.string.byAddress);
                    travelModeText.setText(R.string.waitingByAddress);
                } else {
                    long minutes = getTimeInMinutesBetweenUsers(
                            viewed.getOwner().getPosition(),
                            viewed.getParty().getPosition(),
                            viewed.getOwner().getTravelMode() == none
                                    ? viewed.getParty().getTravelMode()
                                    : viewed.getOwner().getTravelMode());

                    String timeTxt = getFormatTimeFromMillis(minutes * 60000, getResources());
                    travelTypeTitle.setText(getString(R.string.across) + " " + timeTxt);
                    travelModeText.setText(R.string.willArriveAtTheAddress);
                }
                break;
            }
            case requesting:
            case accepting:
            case moving:
                travelTypeImageTitle.setImageResource(viewed.getNotMe(profile.getId()).getTravelMode().getImage());
                travelTypeImage.setImageResource(viewed.getNotMe(profile.getId()).getTravelMode().getImage());
                coordinatesSelect.setVisibility(View.GONE);
                if (viewed.getNotMe(profile.getId()).getTravelMode() == none) {
                    travelTypeTitle.setText(R.string.byAddress);
                    travelModeText.setText(R.string.waitingByAddress);
                } else {
                    long minutes = getTimeInMinutesBetweenUsers(
                            viewed.getOwner().getPosition(),
                            viewed.getParty().getPosition(),
                            viewed.getOwner().getTravelMode() == none
                                    ? viewed.getParty().getTravelMode()
                                    : viewed.getOwner().getTravelMode());

                    String timeTxt = getFormatTimeFromMillis(minutes * 60000, getResources());
                    travelTypeTitle.setText(getString(R.string.across) + " " + timeTxt);
                    travelModeText.setText(R.string.willArriveAtTheAddress);
                }
                break;
            default:{
                travelTypeImageTitle.setImageResource(viewed.getOwner().getTravelMode().getImage());
                travelTypeImage.setImageResource(viewed.getOwner().getTravelMode().getImage());
            }

        }

        String displayTime = DateTimeFormatter.format(viewed.getWorkSchedule().getStartTime().getHourOfDay(), viewed.getWorkSchedule().getStartTime().getMinuteOfHour()) + " - " +
                DateTimeFormatter.format(viewed.getWorkSchedule().getEndTime().getHourOfDay(), viewed.getWorkSchedule().getEndTime().getMinuteOfHour());
        offerTime.setText(R.string.validityOfTheOffer);
        time.setText(displayTime);
    }

    private void drawPrice(Profile profile) {
        drawDropdownElement(priceTitleLayout.getId(), priceLayout.getId());
        changeArrowVector(priceLayout.getId(), priceArrow.getId());

        String priceTitleText = getResources().getString(R.string.priceTxt) + " "
                + MoneyFormatter.format(new BigDecimal(profile.getViewed().getPrice())) + " "
                + AppCache.showPriceInUsd(getString(R.string.currency), profile.getViewed().getPrice());

        priceTitle.setText(priceTitleText);
        descriptionTextInPrice.setText(profile.getViewed().getType().getDuration());
        clarificationTextInPrice.setText(getString(R.string.priceClarification));
        typeImageInPrice.setImageResource(profile.getViewed().getType().getImageDemand());
    }

    private void drawButtons(Profile profile) {
        switch (NoxboxState.getState(profile.getViewed(), profile)) {
            case initial:
                BusinessActivity.businessEvent(BusinessEvent.removedNoxboxOpened);
                offline(profile.getViewed());
                AppCache.availableNoxboxes
                        .remove(profile.getViewed().getId());
                ClusterItemsActivity.noxboxes
                        .remove(new NoxboxMarker(new LatLng(0, 0),
                                new Noxbox().setId(profile.getViewed().getId())));
                break;
            case created:
                if (profile.getViewed().getTimeCreated() > 1) {
                    drawJoinButton(profile);
                }
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
        acceptButton.setVisibility(View.VISIBLE);

        if (!profile.getAcceptance().isAccepted()) {
            acceptButton.setOnClickListener(v -> openPhotoNotVerifySheetDialog(DetailedActivity.this));
        } else {
            acceptButton.setOnClickListener(v -> {
                v.setVisibility(View.GONE);

                Accepting.acceptCurrent();

                Router.finishActivity(DetailedActivity.this);
            });
        }
    }

    private void drawJoinButton(final Profile profile) {

        joinButton.setVisibility(View.VISIBLE);
        if (profile.getViewed().getRole() == MarketRole.supply) {
            joinButton.setText(R.string.order);
        } else {
            joinButton.setText(R.string.proceed);
        }

        joinButton.setOnClickListener(v -> {
            if (!isLocationPermissionGranted(DetailedActivity.this)) {
                startLocationPermissionRequest(DetailedActivity.this, LOCATION_PERMISSION_REQUEST_CODE);

            } else {
                v.setVisibility(View.GONE);
                readinessCheck(profile);
            }

        });


    }

    private void readinessCheck(Profile profile) {
        if (!profile.getAcceptance().isAccepted()) {
            openPhotoNotVerifySheetDialog(DetailedActivity.this);
            return;
        }
        if (profile.getViewed().getRole() == MarketRole.supply && !BalanceCalculator.enoughBalance(profile.getViewed(), profile)) {
            joinButton.setBackground(getResources().getDrawable(R.drawable.button_corner_disabled));
            openWalletAddressSheetDialog(DetailedActivity.this, profile);
            return;
        }

        if (profile.getName() != null && profile.getName().length() == 0) {
            openNameNotVerifySheetDialog(DetailedActivity.this);
            return;
        }

        //when detailed was opened from Contract and joined while current exist
        if (!isNullOrZero(profile.getCurrent().getTimeCreated())
                && !isFinished(profile.getCurrent())) {

            AppCache.removeNoxbox(o -> {
                //show progress
                BusinessActivity.businessEvent(request);
                sendRequestToNoxbox(profile);
            });

            return;
        }

        BusinessActivity.businessEvent(request);

        sendRequestToNoxbox(profile);
    }

    private void sendRequestToNoxbox(Profile profile) {
        profile.getCurrent().copy(profile.getViewed());
        profile.setNoxboxId(profile.getCurrent().getId());
        profile.getCurrent().setTimeRequested(System.currentTimeMillis());

        profile.getCurrent().getParty().addPrivateInfo(profile);
        Firestore.writeProfile(profile, object -> {
            AppCache.updateNoxbox();

            offline(profile.getCurrent());
            if (AppCache.availableNoxboxes.get(profile.getNoxboxId()) != null) {
                AppCache.availableNoxboxes.remove(profile.getNoxboxId());
            }
            startListenNoxbox(profile.getCurrent().getId());
        });

        //hide progress
        Router.finishActivity(DetailedActivity.this);
    }

    private RadioButton longToWait;
    private RadioButton photoDoesNotMatch;
    private RadioButton rudeBehavior;
    private RadioButton substandardWork;
    private RadioButton own;

    private String cancellationReason;

    private void drawCancelButton(final Profile profile) {
        cancelButton.setVisibility(View.VISIBLE);
        cancelButton.setOnClickListener(v -> {
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
                    profile.getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
                } else {
                    profile.getViewed().setTimeCanceledByParty(System.currentTimeMillis());
                    profile.getCurrent().setTimeRatingUpdated((System.currentTimeMillis()));
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

        certificateLayout.setVisibility(View.VISIBLE);
        List<String> certificateUrlList = noxbox.getOwner().getPortfolio().get(noxbox.getType().name()).getImages().get(ImageType.certificates.name());


        RecyclerView certificateList = findViewById(R.id.certificatesList);
        certificateList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        certificateList.setAdapter(new ImageListAdapter(certificateUrlList, this, ImageType.certificates, noxbox.getType(), false));
    }

    private void drawWorkSample(Noxbox noxbox) {
        if (noxbox.getOwner().getPortfolio().get(noxbox.getType().name()) == null) return;

        workSampleLayout.setVisibility(View.VISIBLE);
        List<String> workSampleUrlList = noxbox.getOwner().getPortfolio().get(noxbox.getType().name()).getImages().get(ImageType.samples.name());

        RecyclerView workSampleList = findViewById(R.id.workSampleList);
        workSampleList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        workSampleList.setAdapter(new ImageListAdapter(workSampleUrlList, this, ImageType.samples, noxbox.getType(), false));
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
        if (itemId == android.R.id.home) {
            Router.finishActivity(DetailedActivity.this);
        }

        return true;
    }

    private Position resultPosition;

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == COORDINATE && resultCode == RESULT_OK) {
            resultPosition = new Position(data.getExtras().getDouble(LAT), data.getExtras().getDouble(LNG));
            profile().getViewed().setPosition(resultPosition);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    readinessCheck(profile());
                }
            }
        }
    }

}
