package live.noxbox.application.tutorial;

import android.graphics.Color;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TutorialAdapter extends FragmentPagerAdapter {

    public TutorialAdapter(FragmentManager manager) {
        super(manager);
    }

    @Override
    public android.support.v4.app.Fragment getItem(int position) {
        switch (position) {
            case 0:
                return TutorialFragment.newInstance("Tutorial Page one", Color.RED);
            case 1:
                return TutorialFragment.newInstance("Tutorial Page two", Color.YELLOW);
            case 2:
                return TutorialFragment.newInstance("Tutorial Page three", Color.BLACK);
            case 3:
                return TutorialFragment.newInstance("Tutorial Page four", Color.GREEN);
            default:
                return TutorialFragment.newInstance("Tutorial Page four", Color.GRAY);


        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}
