package live.noxbox.menu.history;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import live.noxbox.R;

public class HistoryFragmentAdapter extends FragmentPagerAdapter {

    private Context context;
    private Integer[] tabTitles = new Integer[]{R.string.performed, R.string.received};

    public HistoryFragmentAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new HistorySupplyFragment();
        } else {
            return new HistoryDemandFragment();
        }
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