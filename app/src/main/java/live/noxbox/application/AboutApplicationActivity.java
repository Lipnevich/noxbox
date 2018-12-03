package live.noxbox.application;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import live.noxbox.BaseActivity;
import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.application.tutorial.TutorialActivity;
import live.noxbox.tools.Router;

import static live.noxbox.Configuration.PROMO_VIDEO_URL;

public class AboutApplicationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_application);
        drawToolbar();
        //TODO (VL) change text after version code 1.0
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version) + " 0." + BuildConfig.VERSION_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.promoVideoView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(PROMO_VIDEO_URL)));
            }
        });
        findViewById(R.id.tutorialViewPager).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.startActivity(AboutApplicationActivity.this, TutorialActivity.class);
            }
        });
    }

    private void drawToolbar() {
        ((TextView) findViewById(R.id.title)).setText(R.string.aboutApp);
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}
