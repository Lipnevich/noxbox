package live.noxbox.debug;

import android.util.Log;

import live.noxbox.BuildConfig;

public class LogTracker {

    public static void showLog(String tag, String message){
        if(BuildConfig.DEBUG){
            Log.d(tag,message);
        }
    }

}
