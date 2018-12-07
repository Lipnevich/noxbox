package live.noxbox.tools;

import android.os.Handler;

import java.math.BigDecimal;
import java.text.DecimalFormat;

import live.noxbox.model.Profile;

import static live.noxbox.Configuration.PRICE_FORMAT;
import static live.noxbox.Configuration.QUARTER;

public class SeparateStreamForStopwatch {

    public static long seconds = 0;
    public static BigDecimal totalMoney = BigDecimal.ZERO;
    private static Handler handler;
    private static Runnable runnable;
    public static final DecimalFormat decimalFormat = new DecimalFormat(PRICE_FORMAT);

    public static void initializeStopwatch(Profile profile, Handler newHandler, Runnable newRunnable) {
        if (newHandler == null || newRunnable == null) return;
        removeTimer();

        SeparateStreamForStopwatch.runnable = newRunnable;
        SeparateStreamForStopwatch.handler = newHandler;

        seconds = (int) ((System.currentTimeMillis() - profile.getCurrent().getTimeStartPerforming()) / 1000);
        totalMoney = new BigDecimal(profile.getCurrent().getPrice()).multiply(QUARTER);

    }

    public static void runTimer() {
        if (runnable == null || handler == null) return;

        handler.post(runnable);
    }

    public static void removeTimer() {
        if (handler != null && runnable != null) {
            handler.removeCallbacks(runnable);
        }
        handler = null;
        runnable = null;
    }


}
