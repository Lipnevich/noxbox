package live.noxbox.menu.history;


import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.View;

import live.noxbox.R;
import live.noxbox.tools.Router;
import live.noxbox.tools.Task;

public class HistoryActivity extends FragmentActivity {

    public static final int CODE = 1002;
    public static Task<Object> isHistoryEmpty;
    public static Task<Object> isHistoryThere;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
        getSupportFragmentManager();


    }

    @Override
    protected void onResume() {
        super.onResume();
        isHistoryEmpty = o -> findViewById(R.id.missingHistoryLayout).setVisibility(View.VISIBLE);
        isHistoryThere = o -> findViewById(R.id.missingHistoryLayout).setVisibility(View.GONE);

        findViewById(R.id.chooseService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO (vl) open list of services depends on the role
            }
        });

        final ViewPager viewPager = findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);

        findViewById(R.id.homeButton).setOnClickListener(v -> Router.finishActivity(HistoryActivity.this));
    }

    private void setupViewPager(ViewPager viewPager) {
        HistoryFragmentAdapter adapter = new HistoryFragmentAdapter(getSupportFragmentManager(), this);
        viewPager.setAdapter(adapter);
    }


}
