package live.noxbox.menu.about.tutorial;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import live.noxbox.R;

public class TutorialAdapter extends FragmentPagerAdapter {

    public TutorialAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position) {
            case 0:
                return TutorialFragment.newInstance(R.string.toOrder, R.drawable.tutorial_page_one, 1);
            case 1:
                return TutorialFragment.newInstance(R.string.toFind, R.drawable.tutorial_page_two, 2);
            case 2:
                return TutorialFragment.newInstance(R.string.getStarted, R.drawable.tutorial_page_three, 3);
            case 3:
                return TutorialFragment.newInstance(R.string.getComplete, R.drawable.tutorial_page_four, 4);
            default:
                return TutorialFragment.newInstance(R.string.toOrder, R.drawable.ic_men_and_planet, 1);


        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}
