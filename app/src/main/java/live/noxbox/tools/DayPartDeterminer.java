package live.noxbox.tools;

import java.util.Calendar;

public class DayPartDeterminer {
    private static final Calendar calendar = Calendar.getInstance();

    public static boolean isItDayNow() {
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        if (hourOfDay >= 9 && hourOfDay < 21) {
            return true;
        } else {
            return false;
        }
    }
}
