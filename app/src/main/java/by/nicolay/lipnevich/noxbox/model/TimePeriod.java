package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.R;

public enum  TimePeriod {
    accurate(1, R.string.accurate),
    daily(2, R.string.daily),
    nightly(3, R.string.nightly);

    private int id;
    private int name;

    TimePeriod(int id, int name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public int getName() {
        return name;
    }

    public static TimePeriod byId(int id){
        for (TimePeriod timePeriod : TimePeriod.values()) {
            if (timePeriod.id == id) {
                return timePeriod;
            }
        }
        return daily;
    }
}
