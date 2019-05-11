package live.noxbox.menu.about;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;

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
        ((TextView) findViewById(R.id.version)).setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.tutorialViewPager).setOnClickListener(v -> Router.startActivity(AboutApplicationActivity.this, TutorialActivity.class));

        findViewById(R.id.promoVideoView).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.promoVideoLink)))));

        findViewById(R.id.rules).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.rulesLink)))));

        findViewById(R.id.privacyPolicy).setOnClickListener(v -> startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(getString(R.string.privacyPolicyLink)))));

    }

    private void drawToolbar() {
        ((TextView) findViewById(R.id.title)).setText(R.string.aboutApp);
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(AboutApplicationActivity.this));
    }
}
