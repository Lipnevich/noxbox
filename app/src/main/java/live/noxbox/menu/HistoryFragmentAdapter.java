package live.noxbox.menu;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import live.noxbox.R;

public class HistoryFragmentAdapter extends FragmentPagerAdapter {

    private Context context;
    private Integer[] tabTitles = new Integer[]{R.string.performed, R.string.received};
    private final List<Fragment> fragments = new ArrayList<>();

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
        //return fragments.get(position);
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

    public void addFragment(Fragment fragment){
        fragments.add(fragment);
    }
}