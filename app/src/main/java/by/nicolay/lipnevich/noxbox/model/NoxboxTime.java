package by.nicolay.lipnevich.noxbox.model;

import org.joda.time.DateTime;

import java.util.Calendar;

public class NoxboxTime {
    private TimePeriod period;
    private DateTime start;
    private DateTime end;

    public NoxboxTime(TimePeriod period) {
        this.period = period;
        if (period == TimePeriod.daily) {
            Calendar calendar = Calendar.getInstance();
            this.start = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 10, 0);
            this.end = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 18, 0);
        } else if (period == TimePeriod.nightly) {
            Calendar calendar = Calendar.getInstance();
            this.start = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 18, 0);
            this.end = new DateTime(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), 10, 0);
        } else if (period == TimePeriod.accurate) {
            //TODO mb starting dialog from here?!? (vlad)
        }
    }

    public NoxboxTime() {
        this.start = new DateTime();
        this.end = new DateTime();
    }

    public NoxboxTime(DateTime start, DateTime end) {
        this.start = start;
        this.end = end;
        this.period = TimePeriod.accurate;
    }

    public TimePeriod getPeriod() {
        return period;
    }

    public NoxboxTime setPeriod(TimePeriod period) {
        this.period = period;
        return this;
    }

    public DateTime getStart() {
        return start;
    }

    public NoxboxTime setStart(DateTime start) {
        this.start = start;
        return this;
    }

    public DateTime getEnd() {
        return end;
    }

    public NoxboxTime setEnd(DateTime end) {
        this.end = end;
        return this;
    }

    public String getTimeAsString() {
        return start.getHourOfDay() + ":" + start.getMinuteOfHour() + "-" + end.getHourOfDay() + ":" + end.getMinuteOfHour();
    }
}
