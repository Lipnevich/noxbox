package live.noxbox.tools;

import android.content.res.Resources;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import live.noxbox.R;

public class DateTimeFormatter {

    private static final String datePattern = "d MMMM";
    private static final String yearPattern = "yyyy";
    private static final SimpleDateFormat date = new SimpleDateFormat(datePattern);
    private static final SimpleDateFormat year = new SimpleDateFormat(yearPattern);
    private static final DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);

    public static String time(Long millis) {
        return format(millis, time);
    }

    public static String date(Long millis) {
        return format(millis, date);
    }

    public static String year(Long millis) {
        return format(millis, year);
    }

    private static String format(Long number, DateFormat formatter) {
        if (number == null) return "";
        return formatter.format(new Date(number));
    }

    public static String format(int hour, int minute) {
        Calendar calendar = new GregorianCalendar(0, 0, 0, hour, minute);
        return format(calendar.getTimeInMillis(), time);
    }

    private static String format(Long millis, SimpleDateFormat year) {
        return format(millis, (DateFormat) year);
    }

    public static String getTimeFromMillis(Long timeStart, Long timeEnd, Resources resources){
        int minutes = (int)(timeEnd - timeStart) / 1000;

        String time = minutes + " ";




        switch (minutes % 10) {
            case 1: {
                time = time.concat(resources.getString(R.string.minute));
                break;
            }
            case 2: {
                time = time.concat(resources.getString(R.string.minutes_));
                break;
            }
            case 3: {
                time = time.concat(resources.getString(R.string.minutes_));
                break;
            }
            case 4: {
                time = time.concat(resources.getString(R.string.minutes_));
                break;
            }
            default: {
                time = time.concat(resources.getString(R.string.minutes));
                break;
            }
        }

        return time;

    }
}
