package live.noxbox.debug;

import android.content.Context;
import android.widget.Toast;

import live.noxbox.BuildConfig;

public class DebugMessage {
    public static void popup(Context context, String message) {
        popup(context, message, Toast.LENGTH_SHORT);
    }

    public static void popup(Context context, String message, int duration) {
        if(BuildConfig.DEBUG){
            Toast.makeText(context, message, duration).show();
        }
    }
}