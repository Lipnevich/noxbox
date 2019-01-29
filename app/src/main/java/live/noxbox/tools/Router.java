package live.noxbox.tools;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import live.noxbox.R;

public class Router {
    public static void startActivity(Activity activity, Class<?> cls) {
        long timeStarted = System.currentTimeMillis();
        activity.startActivity(new Intent(activity, cls));
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        Log.e("TIMETIMETIMETIMETIME", System.currentTimeMillis() - timeStarted + "");
    }

    public static void startActivityForResult(Activity activity, Class<?> cls, int code) {
        activity.startActivityForResult(new Intent(activity, cls), code);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void startActivityForResult(Activity activity, Intent intent, int code) {
        activity.startActivityForResult(intent, code);
        activity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    public static void finishActivity(Activity activity){
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}
