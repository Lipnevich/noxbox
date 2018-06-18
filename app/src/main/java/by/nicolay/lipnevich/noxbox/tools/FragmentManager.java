package by.nicolay.lipnevich.noxbox.tools;


import android.app.Activity;
import android.app.Fragment;

import by.nicolay.lipnevich.noxbox.R;

public class FragmentManager {

    public static void createFragment(Activity activity, Fragment fragment, int layout){
        if(activity == null || fragment == null) return;
        activity.getFragmentManager().beginTransaction().replace(layout,fragment).commit();
    }

    public static void removeFragment(Activity activity,Fragment fragment){
        if(activity == null || fragment == null) return;
        activity.getFragmentManager().beginTransaction().remove(fragment).commit();
    }

    public static void createFragmentOnTop(Activity activity, Fragment fragment, int layout){
        if(activity == null || fragment == null) return;
        activity.getFragmentManager().beginTransaction().setCustomAnimations(R.animator.slide_up,R.animator.slide_down).replace(layout,fragment).commit();
    }

}
