package by.nicolay.lipnevich.noxbox.tools;

import android.app.Activity;
import org.joda.time.DateTime;
import org.joda.time.Period;

import java.util.Calendar;

public class TimeManager {
    public static boolean compareTime(DateTime noxboxStartTime, Activity activity) {
        Calendar calendar = Calendar.getInstance();
        DateTime current = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
        int result = new Period(current,noxboxStartTime).getHours();
        if (result >= 8 || result <= 0) {
            return true;
        }
        return false;
    }
}
