package live.noxbox.tools;

import android.app.Activity;
import android.widget.Toast;

import live.noxbox.BuildConfig;

public class DebugMessage {
    public static void popup(Activity activity, String message) {
        if(BuildConfig.DEBUG){
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
        }
    }
}