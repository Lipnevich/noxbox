package by.nicolay.lipnevich.noxbox.tools;

import java.text.DateFormat;
import java.util.Date;

public class DateTimeFormatter {

    private static final DateFormat date = DateFormat.getDateInstance(DateFormat.SHORT);
    private static final DateFormat time = DateFormat.getTimeInstance(DateFormat.SHORT);

    public static String time(Long millis) {
        return format(millis, time);
    }
    public static String date(Long millis) {
        return format(millis, date);
    }

    private static String format(Long number, DateFormat formatter) {
        if(number == null) return "";
        return formatter.format(new Date(number));
    }



}
