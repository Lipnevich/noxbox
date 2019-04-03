package live.noxbox.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.view.MotionEvent;

import com.github.amlcurran.showcaseview.OnShowcaseEventListener;
import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ViewTarget;

import live.noxbox.R;
import live.noxbox.debug.HackerActivity;

import static live.noxbox.Constants.FIRST_DEMONSTRATION_KEY;

/**
 * Created by Vladislaw Kravchenok on 02.04.2019.
 */
public class DemonstrationActivity extends HackerActivity {
    private SharedPreferences demonstrationPreference;


    protected boolean isFirstRunDemonstration() {
        if (demonstrationPreference == null) {
            demonstrationPreference = getApplicationContext().getSharedPreferences(FIRST_DEMONSTRATION_KEY, MODE_PRIVATE);
        }

        if (demonstrationPreference.getBoolean(FIRST_DEMONSTRATION_KEY, true)) {
            demonstrationPreference.edit().putBoolean(FIRST_DEMONSTRATION_KEY, false).apply();
            return true;
        } else {
            return false;
        }
    }

    protected void startDemonstration() {
        if (!isFirstRunDemonstration()) return;
        final Activity activity = this;
        new ShowcaseView.Builder(activity)
                .setTarget(new ViewTarget(R.id.menu, activity))
                .setStyle(com.github.amlcurran.showcaseview.R.styleable.CustomTheme_showcaseViewStyle)
                .setContentTitle("Menu")
                .setContentText("Navigation")
                .setShowcaseEventListener(new OnShowcaseEventListener() {
                    @Override
                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                        new ShowcaseView.Builder(activity)
                                .setTarget(new ViewTarget(R.id.filter, activity))
                                .setStyle(com.github.amlcurran.showcaseview.R.styleable.CustomTheme_showcaseViewStyle)
                                .setContentTitle("Filters")
                                .setContentText("You can filter services by type in the map")
                                .setShowcaseEventListener(new OnShowcaseEventListener() {
                                    @Override
                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

                                    }

                                    @Override
                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                        new ShowcaseView.Builder(activity)
                                                .setTarget(new ViewTarget(R.id.customFloatingView, activity))
                                                .setStyle(com.github.amlcurran.showcaseview.R.styleable.CustomTheme_showcaseViewStyle)
                                                .setContentTitle("Location")
                                                .setContentText("You can aiming your current location")
                                                .setShowcaseEventListener(new OnShowcaseEventListener() {
                                                    @Override
                                                    public void onShowcaseViewHide(ShowcaseView showcaseView) {

                                                    }

                                                    @Override
                                                    public void onShowcaseViewDidHide(ShowcaseView showcaseView) {
                                                        new ShowcaseView.Builder(activity)
                                                                .setTarget(new ViewTarget(R.id.locationButton, activity))
                                                                .setStyle(com.github.amlcurran.showcaseview.R.styleable.CustomTheme_showcaseViewStyle)
                                                                .setContentTitle("Service creater")
                                                                .setContentText("You can create a service")
                                                                .hideOnTouchOutside()
                                                                .build();
                                                    }

                                                    @Override
                                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                                    }

                                                    @Override
                                                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                                    }
                                                })
                                                .hideOnTouchOutside()
                                                .build();
                                    }

                                    @Override
                                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                                    }

                                    @Override
                                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                                    }
                                })
                                .hideOnTouchOutside()
                                .build();
                    }

                    @Override
                    public void onShowcaseViewShow(ShowcaseView showcaseView) {

                    }

                    @Override
                    public void onShowcaseViewTouchBlocked(MotionEvent motionEvent) {

                    }
                })
                .hideOnTouchOutside()
                .build();
    }
}
