package live.noxbox.menu.history;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import live.noxbox.R;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

public class HistoryActivity extends FragmentActivity {

    public static final int CODE = 1002;
    public static Task<Object> isHistoryEmpty;
    public static Task<Object> isHistoryThere;

    public static boolean isSupplyHistoryEmpty = true;
    public static boolean isDemandHistoryEmpty = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportFragmentManager();

        Glide.with(this)
                .asGif()
                .load(R.drawable.progress_cat)
                .into((ImageView) findViewById(R.id.progress));
        final ViewPager viewPager = findViewById(R.id.viewpager);

        HistoryFragmentAdapter adapter = new HistoryFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    if (isSupplyHistoryEmpty) {
                        isHistoryEmpty.execute(null);
                    } else {
                        isHistoryThere.execute(null);
                    }
                } else {
                    if (isDemandHistoryEmpty) {
                        isHistoryEmpty.execute(null);
                    } else {
                        isHistoryThere.execute(null);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(HistoryActivity.this));

        findViewById(R.id.chooseService).setOnClickListener(v -> Router.finishActivity(HistoryActivity.this));
        isHistoryEmpty = o -> {
            findViewById(R.id.progressLayout).setVisibility(View.GONE);
            findViewById(R.id.missingHistoryLayout).setVisibility(View.VISIBLE);
        };
        isHistoryThere = o -> {
            findViewById(R.id.progressLayout).setVisibility(View.GONE);
            findViewById(R.id.missingHistoryLayout).setVisibility(View.GONE);
        };

    }

}
