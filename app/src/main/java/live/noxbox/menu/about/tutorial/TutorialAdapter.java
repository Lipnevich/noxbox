package live.noxbox.menu.about.tutorial;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class TutorialAdapter extends FragmentPagerAdapter {

    public TutorialAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        return TutorialFragment.newInstance(++position);
    }

    @Override
    public int getCount() {
        return 4;
    }

}
