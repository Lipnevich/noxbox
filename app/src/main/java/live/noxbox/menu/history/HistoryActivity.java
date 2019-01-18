package live.noxbox.menu.history;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;

import live.noxbox.R;
import live.noxbox.tools.Router;

public class HistoryActivity extends FragmentActivity {

    public static final int CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportFragmentManager();
        //setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
//        int size = 384;
//        progressView = findViewById(R.id.progressView);
//        Glide.with(this).asGif()
//                .load(R.drawable.progress_cat)
//                .apply(RequestOptions.overrideOf(size, size))
//                .into(progressView);
        final ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        HistoryFragmentAdapter adapter = new HistoryFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.homeButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Router.finishActivity(HistoryActivity.this);
            }
        });
    }
}
