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
                return TutorialFragment.newInstance("Tutorial Page one", R.drawable.ic_men_and_planet);
            case 1:
                return TutorialFragment.newInstance("Tutorial Page two", R.drawable.ic_men_and_planet);
            case 2:
                return TutorialFragment.newInstance("Tutorial Page three", R.drawable.ic_men_and_planet);
            case 3:
                return TutorialFragment.newInstance("Tutorial Page four", R.drawable.ic_men_and_planet);
            default:
                return TutorialFragment.newInstance("Tutorial Page four", R.drawable.ic_men_and_planet);


        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}
