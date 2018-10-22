package live.noxbox.tools;

import android.content.Context;
import android.widget.Toast;

import live.noxbox.BuildConfig;

public class DebugMessage {
    public static void popup(Context context, String message) {
        if(BuildConfig.DEBUG){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }
}