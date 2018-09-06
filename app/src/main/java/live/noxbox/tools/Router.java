package live.noxbox.tools;

import android.app.Activity;
import android.content.Intent;

public class Router {
    public static void startActivity(Activity activity, Class<?> cls) {
        activity.startActivity(new Intent(activity, cls));
    }
}
