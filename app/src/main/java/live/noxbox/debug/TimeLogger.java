package live.noxbox.debug;

import android.util.Log;

public final class TimeLogger {
    private long time = System.currentTimeMillis();

    public void makeLog(String tag) {
        long timeSpent = System.currentTimeMillis() - time;
        if(timeSpent > 100) {
            Log.w(tag, "Long Execution Time: " + timeSpent);
        }
    }

}
