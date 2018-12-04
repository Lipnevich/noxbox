package live.noxbox.menu.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;

import live.noxbox.BuildConfig;
import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.menu.about.tutorial.TutorialActivity;
import live.noxbox.tools.Router;

public class AboutApplicationActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_application);
        drawToolbar();
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version) + BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.promoVideoView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.promoVideoLink))));
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
