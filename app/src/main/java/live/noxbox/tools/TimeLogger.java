package live.noxbox.tools;

import android.util.Log;

public final class TimeLogger {
    private long time = System.currentTimeMillis();

    public void makeLog(String tag) {
        Log.d(tag, "Execution time: " + (System.currentTimeMillis() - time));
    }

}
