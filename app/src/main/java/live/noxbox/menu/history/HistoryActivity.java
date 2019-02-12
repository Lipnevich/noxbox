package live.noxbox.menu.history;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

import live.noxbox.R;
import live.noxbox.tools.Router;

public class HistoryActivity extends FragmentActivity {

    public static final int CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportFragmentManager();

        final ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
    }

    private void setupViewPager(ViewPager viewPager) {
        HistoryFragmentAdapter adapter = new HistoryFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(HistoryActivity.this));
    }
}
