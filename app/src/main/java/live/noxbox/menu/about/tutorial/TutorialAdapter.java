package live.noxbox.menu.about.tutorial;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TutorialAdapter extends FragmentPagerAdapter {

    public TutorialAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        return TutorialFragment.newInstance(++position);
    }

    @Override
    public int getCount() {
        return 4;
    }

}
