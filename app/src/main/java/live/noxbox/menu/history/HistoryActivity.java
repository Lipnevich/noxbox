package live.noxbox.menu.history;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import live.noxbox.R;
import live.noxbox.activities.BaseActivity;
import live.noxbox.database.AppCache;
import live.noxbox.model.MarketRole;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

import static live.noxbox.database.AppCache.profile;

public class HistoryActivity extends BaseActivity {

    public static final int CODE = 1002;
    public static final String KEY_COMPLETE = "lastNoxboxTimeCompleted";
    public static final String KEY_PERFORMER_ID = "lastNoxboxRole";
    public static Task<MarketRole> isHistoryEmpty;
    public static Task<MarketRole> isHistoryThere;

    public static boolean isSupplyHistoryEmpty = true;
    public static boolean isDemandHistoryEmpty = true;

    private long lastNoxboxTimeCompleted;
    private String lastNoxboxPerformerId;

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private ImageButton homeButton;
    private Button chooseService;
    private TextView missingHistoryMessage;
    private LinearLayout missingHistoryLayout;
    private LinearLayout progressLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportFragmentManager();

        Glide.with(this)
                .asGif()
                .load(R.drawable.progress_cat)
                .into((ImageView) findViewById(R.id.progress));
        viewPager = findViewById(R.id.viewpager);
        homeButton = findViewById(R.id.homeButton);
        chooseService = findViewById(R.id.chooseService);
        missingHistoryMessage = findViewById(R.id.missingHistoryMessage);
        missingHistoryLayout = findViewById(R.id.missingHistoryLayout);
        progressLayout = findViewById(R.id.progressLayout);


        lastNoxboxTimeCompleted = getIntent().getLongExtra(KEY_COMPLETE, 0);
        lastNoxboxPerformerId = getIntent().getStringExtra(KEY_PERFORMER_ID);

        HistoryFragmentAdapter adapter = new HistoryFragmentAdapter(getSupportFragmentManager(), lastNoxboxTimeCompleted, this);
        viewPager.setAdapter(adapter);

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        if (lastNoxboxPerformerId != null) {
            if (lastNoxboxPerformerId.equals(profile().getId())) {
                viewPager.setCurrentItem(0);
            } else {
                viewPager.setCurrentItem(1);
            }
        }

        chooseService.setOnClickListener(v -> {
            profile().getFilters().setDemand(false);
            profile().getFilters().setSupply(true);
            Router.finishActivity(HistoryActivity.this);
        });

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    if (isSupplyHistoryEmpty) {
                        isHistoryEmpty.execute(MarketRole.supply);
                    } else {
                        isHistoryThere.execute(null);
                    }
                } else {
                    if (isDemandHistoryEmpty) {
                        isHistoryEmpty.execute(MarketRole.demand);
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
        processYourFirstService();

    }

    @Override
    protected void onResume() {
        super.onResume();
        homeButton.setOnClickListener(v -> Router.finishActivity(HistoryActivity.this));

        isHistoryEmpty = marketRole -> {
            if (marketRole == MarketRole.demand) {
                chooseService.setOnClickListener(v -> {
                    profile().getFilters().setDemand(true);
                    profile().getFilters().setSupply(false);
                    AppCache.fireProfile();
                    Router.finishActivity(HistoryActivity.this);
                });
                missingHistoryMessage.setText(R.string.missingReceivedHistoryMessage);
                chooseService.setText(R.string.chooseDemandService);
            } else if (marketRole == MarketRole.supply) {
                processYourFirstService();
            }
            progressLayout.setVisibility(View.GONE);
            missingHistoryLayout.setVisibility(View.VISIBLE);
        };
        isHistoryThere = o -> {
            MarketRole role = o;
            progressLayout.setVisibility(View.GONE);
            missingHistoryLayout.setVisibility(View.GONE);
            if (role != null) {
                if (role == MarketRole.demand) {
                    viewPager.setCurrentItem(1);
                } else {
                    viewPager.setCurrentItem(0);
                }
            }
        };

    }


    private void processYourFirstService() {
        chooseService.setOnClickListener(v -> {
            profile().getFilters().setDemand(false);
            profile().getFilters().setSupply(true);
            AppCache.fireProfile();
            Router.finishActivity(HistoryActivity.this);
        });
        missingHistoryMessage.setText(R.string.missingPerformedHistoryMessage);
        chooseService.setText(R.string.chooseSupplyService);
    }

}
