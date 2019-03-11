package live.noxbox.activities;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.ImageViewTarget;

import in.shadowfax.proswipebutton.ProSwipeButton;
import live.noxbox.R;
import live.noxbox.analitics.BusinessActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.Profile;
import live.noxbox.tools.Router;

import static live.noxbox.analitics.BusinessEvent.verification;
import static live.noxbox.database.AppCache.updateNoxbox;

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
        AppCache.readProfile(profile -> draw(profile));
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
            profile.getCurrent().setConfirmationPhoto(null);
            wrongPhoto.setVisibility(View.GONE);
            confirm.setArrowColor(getResources().getColor(R.color.fullTranslucent));
            new Handler().postDelayed(() -> {
                long timeVerified = System.currentTimeMillis();
                BusinessActivity.businessEvent(verification);
                if (profile.equals(profile.getCurrent().getOwner())) {
                    profile.getCurrent().setTimeOwnerVerified(timeVerified);
                } else {
                    profile.getCurrent().setTimePartyVerified(timeVerified);
                }
                updateNoxbox();
                Router.finishActivity(ConfirmationActivity.this);
            }, 0);
        });
    }

    private void drawConformityButton(final Profile profile) {
        wrongPhoto.setOnSwipeListener(() -> {
            profile.getCurrent().setConfirmationPhoto(null);
            wrongPhoto.setArrowColor(getResources().getColor(R.color.fullTranslucent));
            confirm.setVisibility(View.GONE);
            new Handler().postDelayed(() -> {
                long timeRejected = System.currentTimeMillis();
                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                    profile.getCurrent().setTimeOwnerRejected(timeRejected);
                } else {
                    profile.getCurrent().setTimePartyRejected(timeRejected);
                }
                updateNoxbox();
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
