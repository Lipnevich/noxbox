package by.nicolay.lipnevich.noxbox.tools;


import android.app.Activity;
import android.app.Fragment;

public class FragmentManager {

    public static void createFragment(Activity activity, Fragment fragment, int layout){
        if(activity == null || fragment == null) return;
        activity.getFragmentManager().beginTransaction().replace(layout,fragment).commit();
    }

    public static void removeFragment(Activity activity,Fragment fragment){
        if(activity == null || fragment == null) return;
        activity.getFragmentManager().beginTransaction().remove(fragment).commit();
    }

}
