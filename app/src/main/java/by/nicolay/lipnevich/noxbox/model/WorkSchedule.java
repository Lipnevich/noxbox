package by.nicolay.lipnevich.noxbox.model;

import java.io.Serializable;

import static by.nicolay.lipnevich.noxbox.Configuration.DAY_TIME_EHD_IN_HOURS;
import static by.nicolay.lipnevich.noxbox.Configuration.DAY_TIME_START_IN_HOURS;
import static by.nicolay.lipnevich.noxbox.Configuration.NIGHT_TIME_EHD_IN_HOURS;
import static by.nicolay.lipnevich.noxbox.Configuration.NIGHT_TIME_START_IN_HOURS;

public class WorkSchedule implements Serializable {

    private TimePeriod period;
    private int startInHours;
    private int startInMinutes;
    private int endInHours;
    private int endInMinutes;

    public WorkSchedule(TimePeriod period) {
        this.period = period;
        if (period == TimePeriod.daily) {
            this.startInHours = DAY_TIME_START_IN_HOURS;
            this.startInMinutes = 0;
            this.endInHours = DAY_TIME_EHD_IN_HOURS;
            this.endInMinutes = 0;
        } else if (period == TimePeriod.nightly) {
            this.startInHours = NIGHT_TIME_START_IN_HOURS;
            this.startInMinutes = 0;
            this.endInHours = NIGHT_TIME_EHD_IN_HOURS;
            this.endInMinutes = 0;
        } else if (period == TimePeriod.accurate) {
            //TODO mb starting dialog from here?!? (vlad)
        }
    }

    public WorkSchedule(int startInHours, int startInMinutes, int endInHours, int endInMinutes) {
        this.startInHours = startInHours;
        this.startInMinutes = startInMinutes;
        this.endInHours = endInHours;
        this.endInMinutes = endInMinutes;
    }
    public WorkSchedule(){ }

    public TimePeriod getPeriod() {
        return period;
    }

    public WorkSchedule setPeriod(TimePeriod period) {
        this.period = period;
        return this;
    }

    public int getStartInHours() {
        return startInHours;
    }

    public WorkSchedule setStartInHours(int startInHours) {
        this.startInHours = startInHours;
        return this;
    }

    public int getStartInMinutes() {
        return startInMinutes;
    }

    public WorkSchedule setStartInMinutes(int startInMinutes) {
        this.startInMinutes = startInMinutes;
        return this;
    }

    public int getEndInHours() {
        return endInHours;
    }

    public WorkSchedule setEndInHours(int endInHours) {
        this.endInHours = endInHours;
        return this;
    }

    public int getEndInMinutes() {
        return endInMinutes;
    }

    public WorkSchedule setEndInMinutes(int endInMinutes) {
        this.endInMinutes = endInMinutes;
        return this;
    }

}
