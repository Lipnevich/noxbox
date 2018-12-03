package live.noxbox.application.tutorial;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.TextView;

import live.noxbox.BaseActivity;
import live.noxbox.R;

public class TutorialActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        drawToolbar();
        ((ViewPager) findViewById(R.id.viewpager)).setAdapter(new TutorialAdapter(getSupportFragmentManager()));
    }

    private void drawToolbar() {
        ((TextView) findViewById(R.id.title)).setText(R.string.tutorial);
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
