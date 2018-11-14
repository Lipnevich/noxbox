package live.noxbox.notifications;

import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.Iterator;

import live.noxbox.tools.Task;

public class Repeater {

    public static void repeat(final long interval, final long start, final long duration, final Task finalTask, final Task ... animation) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                Iterator<Task> iterator = Arrays.asList(animation).iterator();
                while (start + duration < System.currentTimeMillis()) {
                    if(!iterator.hasNext()) {
                        iterator = Arrays.asList(animation).iterator();
                    }
                    Task task = iterator.next();
                    task.execute(null);

                    try {
                        Thread.sleep(interval);
                    } catch (InterruptedException e) {
                        Crashlytics.logException(e);
                    }
                }
                finalTask.execute(null);
            }
        }).start();

    }


}
