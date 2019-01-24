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
                return TutorialFragment.newInstance(R.string.toOrder, R.drawable.tutorial_page_one);
            case 1:
                return TutorialFragment.newInstance(R.string.toFind, R.drawable.tutorial_page_two);
            case 2:
                return TutorialFragment.newInstance(R.string.getStarted, R.drawable.tutorial_page_three);
            case 3:
                return TutorialFragment.newInstance(R.string.getComplete, R.drawable.tutorial_page_four);
            default:
                return TutorialFragment.newInstance(R.string.toOrder, R.drawable.ic_men_and_planet);


        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}
