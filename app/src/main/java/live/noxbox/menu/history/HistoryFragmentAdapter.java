package live.noxbox.menu.history;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import live.noxbox.R;

import static live.noxbox.model.Noxbox.isNullOrZero;

public class HistoryFragmentAdapter extends FragmentPagerAdapter {

    private Context context;
    private Integer[] tabTitles = new Integer[]{R.string.performed, R.string.received};
    private long lastNoxboxTimeCompleted;

    public HistoryFragmentAdapter(FragmentManager fm, long lastNoxboxTimeCompleted, Context context) {
        super(fm);
        this.lastNoxboxTimeCompleted = lastNoxboxTimeCompleted;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment historyFragment;

        Bundle bundle = new Bundle();
        if (!isNullOrZero(lastNoxboxTimeCompleted)) {
            bundle.putLong(HistoryActivity.KEY_COMPLETE, lastNoxboxTimeCompleted);
        }

        if (position == 0) {
            historyFragment = new HistorySupplyFragment();
        } else {
            historyFragment = new HistoryDemandFragment();
        }

        historyFragment.setArguments(bundle);
        return historyFragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return context.getString(tabTitles[position]);
    }

    @Override
    public int getCount() {
        return 2;
    }

}