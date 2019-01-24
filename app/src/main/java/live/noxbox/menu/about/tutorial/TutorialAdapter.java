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
                return TutorialFragment.newInstance("Заказывайте", R.drawable.tutorial_page_one);
            case 1:
                return TutorialFragment.newInstance("Находите", R.drawable.tutorial_page_two);
            case 2:
                return TutorialFragment.newInstance("Приступайте", R.drawable.tutorial_page_three);
            case 3:
                return TutorialFragment.newInstance("Завершайте", R.drawable.tutorial_page_four);
            default:
                return TutorialFragment.newInstance("Обратитесь в поддержку проекта", R.drawable.ic_men_and_planet);


        }
    }

    @Override
    public int getCount() {
        return 4;
    }

}
