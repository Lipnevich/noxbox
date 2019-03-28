package live.noxbox.tools;

import android.app.ActivityManager;
import android.content.Context;
import android.widget.Toast;

import live.noxbox.debug.DebugMessage;

/**
 * Created by Vladislaw Kravchenok on 28.03.2019.
 */
public class ServiceMonitoring {
    public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                DebugMessage.popup(context,"LocationListenerService is running");
                return true;
            }
        }
        DebugMessage.popup(context,"LocationListenerService isn't running", Toast.LENGTH_LONG);
        return false;
    }
}
