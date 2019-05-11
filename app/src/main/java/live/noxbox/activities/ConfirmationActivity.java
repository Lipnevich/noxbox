package live.noxbox.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;

import androidx.annotation.Nullable;
import in.shadowfax.proswipebutton.ProSwipeButton;
import live.noxbox.R;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;
import live.noxbox.tools.Router;

import static live.noxbox.analitics.BusinessEvent.rejectPhoto;
import static live.noxbox.analitics.BusinessEvent.verification;
import static live.noxbox.database.AppCache.executeUITasks;
import static live.noxbox.database.AppCache.updateNoxbox;
import static live.noxbox.model.Noxbox.isNullOrZero;

public class ConfirmationActivity extends BaseActivity {

    private TextView title;
    private ImageView homeButton;
    private ImageView photo;
    private ProgressBar progress;
    private ProSwipeButton confirm;
    private ProSwipeButton wrongPhoto;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        title = findViewById(R.id.title);
        homeButton = findViewById(R.id.homeButton);
        photo = findViewById(R.id.photo);
        progress = findViewById(R.id.progressCat);
        confirm = findViewById(R.id.swipeButtonConfirm);
        wrongPhoto = findViewById(R.id.swipeButtonWrongPhoto);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.listenProfile(ConfirmationActivity.class.getName(), profile -> {
            if (!isNullOrZero(profile.getCurrent().getTimePartyRejected())
                    || !isNullOrZero(profile.getCurrent().getTimeOwnerRejected())) {
                Router.finishActivity(ConfirmationActivity.this);
                return;
            }

            draw(profile);
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppCache.stopListen(this.getClass().getName());
    }

    private void draw(Profile profile) {
        drawToolbar();
        drawPhoto(profile);
        drawConfirmButton(profile);
        drawConformityButton(profile);
    }

    private void drawToolbar() {
        title.setText(R.string.verification);
        homeButton.setOnClickListener(v -> Router.finishActivity(ConfirmationActivity.this));
    }

    private void drawPhoto(Profile profile) {
        if (profile.getCurrent().getConfirmationPhoto() != null) {
            showPhoto(profile.getCurrent().getConfirmationPhoto());
        } else {
            Glide.with(this)
                    .asDrawable()
                    .load(profile.getCurrent().getNotMe(profile.getId()).getPhoto())
                    .into(new ImageViewTarget<Drawable>(photo) {
                        @Override
                        protected void setResource(@Nullable Drawable drawable) {
                            showPhoto(drawable);
                        }
                    });
        }

    }

    private void drawConfirmButton(final Profile profile) {
        confirm.setOnSwipeListener(() -> {

            wrongPhoto.setVisibility(View.GONE);
            confirm.setArrowColor(getResources().getColor(R.color.fullTranslucent));
            new Handler().postDelayed(() -> {
                long timeVerified = System.currentTimeMillis();
                if (profile.equals(profile.getCurrent().getOwner())) {
                    profile.getCurrent().setTimeOwnerVerified(timeVerified);
                    profile.getCurrent().setTimePartyVerified(null);
                } else {
                    profile.getCurrent().setTimePartyVerified(timeVerified);
                    profile.getCurrent().setTimePartyVerified(null);
                }
                updateNoxbox(onSuccess -> {
                    businessEvent(verification);
                    profile.getCurrent().setConfirmationPhoto(null);
                    profile.getCurrent().setWasNotificationVerification(false);
                }, onFailure -> {
                    if (profile.equals(profile.getCurrent().getOwner())) {
                        profile.getCurrent().setTimeOwnerVerified(0l);
                    } else {
                        profile.getCurrent().setTimePartyVerified(0l);
                    }
                    executeUITasks();
                });
                Router.finishActivity(ConfirmationActivity.this);
            }, 0);
        });
    }

    private void drawConformityButton(final Profile profile) {
        wrongPhoto.setOnSwipeListener(() -> {

            wrongPhoto.setArrowColor(getResources().getColor(R.color.fullTranslucent));
            confirm.setVisibility(View.GONE);
            new Handler().postDelayed(() -> {
                long timeRejected = System.currentTimeMillis();
                if (profile.getCurrent().getOwner().equals(profile)) {
                    profile.getCurrent().setTimeOwnerRejected(timeRejected);
                    profile.getCurrent().setTimeRatingUpdated(timeRejected);
                } else {
                    profile.getCurrent().setTimePartyRejected(timeRejected);
                    profile.getCurrent().setTimeRatingUpdated(timeRejected);
                }

                updateNoxbox(onSuccess -> {
                    businessEvent(rejectPhoto);
                    profile.getCurrent().setConfirmationPhoto(null);
                    profile.getCurrent().setWasNotificationVerification(false);
                }, onFailure -> {
                    if (profile.getCurrent().getOwner().equals(profile)) {
                        profile.getCurrent().setTimeOwnerRejected(1l);
                    } else {
                        profile.getCurrent().setTimePartyRejected(1l);
                    }
                    executeUITasks();
                });
                Router.finishActivity(ConfirmationActivity.this);
            }, 0);
        });
    }

    private void showPhoto(Drawable drawable) {
        progress.setVisibility(View.GONE);
        photo.setVisibility(View.VISIBLE);
        photo.setImageDrawable(drawable);
    }
}
