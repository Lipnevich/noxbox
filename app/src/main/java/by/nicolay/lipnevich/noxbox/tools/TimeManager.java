package by.nicolay.lipnevich.noxbox.tools;

import android.app.Activity;

import org.joda.time.LocalTime;
import org.joda.time.Period;

import java.util.Calendar;

public class TimeManager {
    public static boolean compareTime(int startInHour,int startInMinutes, Activity activity) {
        Calendar calendar = Calendar.getInstance();
        LocalTime startTime = new LocalTime(startInHour,startInMinutes);
        LocalTime current = new LocalTime(calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE));
        int result = new Period(current,startTime).getHours();
        new Period(new LocalTime(),new LocalTime());
        if (result >= 8 || result <= 0) {
            return true;
        }
        return false;
    }
}
