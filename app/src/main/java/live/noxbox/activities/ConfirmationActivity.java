package live.noxbox.activities;

import android.app.Activity;
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

    private ImageView photo;
    private ProgressBar progress;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);
        photo = findViewById(R.id.photo);
        progress = findViewById(R.id.progressCat);

    }

    @Override
    protected void onResume() {
        super.onResume();
        AppCache.readProfile(profile -> draw(profile));
    }

    private void draw(Profile profile) {
        drawToolbar();
        drawPhoto(profile, this);
        drawConfirmButton(profile, this);
        drawConformityButton(profile, this);
    }

    private void drawToolbar() {
        ((TextView) findViewById(R.id.title)).setText(R.string.verification);
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(ConfirmationActivity.this));
    }

    private void drawPhoto(Profile profile, Activity activity) {
        Glide.with(activity).asDrawable().load(profile.getCurrent().getNotMe(profile.getId()).getPhoto()).into(new ImageViewTarget<Drawable>(photo) {
            @Override
            protected void setResource(@Nullable Drawable drawable) {
                progress.setVisibility(View.GONE);
                photo.setVisibility(View.VISIBLE);
                photo.setImageDrawable(drawable);
            }
        });
    }

    private void drawConfirmButton(final Profile profile, final Activity activity) {
        ProSwipeButton proSwipeBtn = (ProSwipeButton) findViewById(R.id.swipeButtonConfirm);
        proSwipeBtn.setOnSwipeListener(() -> {
            findViewById(R.id.swipeButtonWrongPhoto).setVisibility(View.GONE);
            proSwipeBtn.setArrowColor(getResources().getColor(R.color.fullTranslucent));
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

    private void drawConformityButton(final Profile profile, Activity activity) {
        final ProSwipeButton proSwipeBtn = (ProSwipeButton) findViewById(R.id.swipeButtonWrongPhoto);
        proSwipeBtn.setOnSwipeListener(() -> {
            findViewById(R.id.swipeButtonConfirm).setVisibility(View.GONE);
            proSwipeBtn.setArrowColor(getResources().getColor(R.color.fullTranslucent));
            new Handler().postDelayed(() -> {
                long timeCanceled = System.currentTimeMillis();
                if (profile.getCurrent().getOwner().getId().equals(profile.getId())) {
                    profile.getCurrent().setTimeCanceledByOwner(timeCanceled);
                } else {
                    profile.getCurrent().setTimeCanceledByParty(timeCanceled);
                }
                updateNoxbox();
                Router.finishActivity(ConfirmationActivity.this);
            }, 0);
        });
    }
}
