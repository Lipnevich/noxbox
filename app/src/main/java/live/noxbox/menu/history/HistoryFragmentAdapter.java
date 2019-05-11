package live.noxbox.menu.history;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

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