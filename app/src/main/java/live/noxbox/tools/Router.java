package live.noxbox.tools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class Router {
    public static void startActivity(Context context, Class<?> cls) {
        context.startActivity(new Intent(context, cls));
    }

    public static void startActivityForResult(Activity activity, Class<?> cls, int code) {
        activity.startActivityForResult(new Intent(activity, cls), code);
    }

    public static void startActivityForResult(Activity activity, Intent intent, int code) {
        activity.startActivityForResult(intent, code);
    }
}
