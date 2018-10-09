package live.noxbox.tools;

import android.os.Handler;

import live.noxbox.model.Profile;

public class SeparateStreamForStopwatch {

    public static int seconds;
    public static double totalMoney;
    private static Handler handler;
    private static Runnable runnable;

    public static void initializeStopwatch(Profile profile, Handler handler, Runnable runnable) {
        if (runnable == null || handler == null) return;

        SeparateStreamForStopwatch.runnable = runnable;
        SeparateStreamForStopwatch.handler = handler;

        seconds = (int) ((System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        totalMoney = Double.parseDouble(profile.getCurrent().getPrice()) / 4;

    }

    public static void runTimer() {
        if (runnable == null || handler == null) return;

        handler.post(runnable);
    }

    public static void removeTimer() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        handler = null;
        runnable = null;
    }


}
