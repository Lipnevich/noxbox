package live.noxbox.tools;

import android.os.Handler;

public class SeparateStreamForStopwatch {

    private static final Handler handler = new Handler();
    private static Runnable runnable;

    public static void startHandler(final Task task, final long interval) {
        if (task == null) return;
        stopHandler();

        runnable = new Runnable() {
            @Override
            public void run() {
                task.execute(null);
                handler.postDelayed(this, interval);
            }
        };

        handler.post(runnable);
    }

    public static void stopHandler() {
        if (runnable != null) {
            handler.removeCallbacks(runnable);
        }
        runnable = null;
    }


}
