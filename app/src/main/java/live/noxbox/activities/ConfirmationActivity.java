package live.noxbox.activities;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import in.shadowfax.proswipebutton.ProSwipeButton;
import live.noxbox.R;
import live.noxbox.activities.contract.ContractActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;
import live.noxbox.tools.DateTimeFormatter;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.updateNoxbox;

public class ConfirmationActivity extends BaseActivity {

    private static final String TAG = ConfirmationActivity.class.getName();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.readProfile(new Task<Profile>() {
            @Override
            public void execute(Profile profile) {
                draw(profile);
            }
        });
    }

    private void draw(Profile profile) {
        drawToolbar();
        drawPhoto(profile, this);
        drawConfirmButton(profile, this);
        drawConformityButton(profile, this);
    }

    private void drawToolbar() {
        ((TextView) findViewById(R.id.title)).setText(R.string.verification);
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.finishActivity(ConfirmationActivity.this);
            }
        });
    }

    private void drawPhoto(Profile profile, Activity activity) {
        Glide.with(activity).asDrawable().load(profile.getCurrent().getNotMe(profile.getId()).getPhoto()).into(new SimpleTarget<Drawable>() {
            @Override
            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                ((ImageView) findViewById(R.id.photo)).setImageDrawable(resource);
            }
        });
    }

    private void drawConfirmButton(final Profile profile, final Activity activity) {
//        final SwipeButton buttonConfirm = findViewById(R.id.swipeButtonConfirm);
//        buttonConfirm.setParametrs(activity.getDrawable(R.drawable.yes), activity.getResources().getString(R.string.confirm), activity);
//        buttonConfirm.setOnTouchListener(buttonConfirm.getButtonTouchListener(new Task<Object>() {
//            @Override
//            public void execute(Object object) {
//                long timeVerified = System.currentTimeMillis();
//                if (profile.equals(profile.getCurrent().getOwner())) {
//                    Log.d(TAG, "timeOwnerVerified: " + DateTimeFormatter.time(timeVerified));
//                    profile.getCurrent().setTimeOwnerVerified(timeVerified);
//                } else {
//                    Log.d(TAG, "timePartyVerified: " + DateTimeFormatter.time(timeVerified));
//                    profile.getCurrent().setTimePartyVerified(timeVerified);
//                }
//                updateNoxbox();
//                finish();
//            }
//        }));

        ProSwipeButton proSwipeBtn = (ProSwipeButton) findViewById(R.id.swipeButtonConfirm);
        proSwipeBtn.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {
                findViewById(R.id.swipeButtonWrongPhoto).setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        proSwipeBtn.showResultIcon(true);
                        long timeVerified = System.currentTimeMillis();
                        if (profile.equals(profile.getCurrent().getOwner())) {
                            Log.d(TAG, "timeOwnerVerified: " + DateTimeFormatter.time(timeVerified));
                            profile.getCurrent().setTimeOwnerVerified(timeVerified);
                        } else {
                            Log.d(TAG, "timePartyVerified: " + DateTimeFormatter.time(timeVerified));
                            profile.getCurrent().setTimePartyVerified(timeVerified);
                        }
                        updateNoxbox();
                        Router.finishActivity(ConfirmationActivity.this);
                    }
                }, 1000);
            }
        });
    }

    private void drawConformityButton(final Profile profile, Activity activity) {
//        SwipeButton buttonConformity = findViewById(R.id.swipeButtonWrongPhoto);
//        buttonConformity.setParametrs(activity.getDrawable(R.drawable.no), activity.getResources().getString(R.string.notLikeThat), activity);
//        buttonConformity.setOnTouchListener(buttonConformity.getButtonTouchListener(new Task<Object>() {
//            @Override
//            public void execute(Object object) {
//                long timeCanceled = System.currentTimeMillis();
//                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
//                    Log.d(TAG, "timeCanceledByOwner: " + DateTimeFormatter.time(timeCanceled));
//                    profile.getCurrent().setTimeCanceledByOwner(timeCanceled);
//                } else {
//                    Log.d(TAG, "timeCanceledByParty: " + DateTimeFormatter.time(timeCanceled));
//                    profile.getCurrent().setTimeCanceledByParty(timeCanceled);
//                }
//                updateNoxbox();
//                finish();
//            }
//        }));
        ProSwipeButton proSwipeBtn = (ProSwipeButton) findViewById(R.id.swipeButtonWrongPhoto);
        proSwipeBtn.setOnSwipeListener(new ProSwipeButton.OnSwipeListener() {
            @Override
            public void onSwipeConfirm() {
                findViewById(R.id.swipeButtonConfirm).setVisibility(View.GONE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        proSwipeBtn.showResultIcon(true);
                        long timeCanceled = System.currentTimeMillis();
                        if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                            Log.d(TAG, "timeCanceledByOwner: " + DateTimeFormatter.time(timeCanceled));
                            profile.getCurrent().setTimeCanceledByOwner(timeCanceled);
                        } else {
                            Log.d(TAG, "timeCanceledByParty: " + DateTimeFormatter.time(timeCanceled));
                            profile.getCurrent().setTimeCanceledByParty(timeCanceled);
                        }
                        updateNoxbox();
                        Router.finishActivity(ConfirmationActivity.this);
                    }
                }, 1000);
            }
        });
    }
}
