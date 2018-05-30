package by.nicolay.lipnevich.noxbox.model;

public enum TravelMode {
    // 45 km/h
    driving(45000 / 60),
    // 15 km/h
    transit(15000 / 60),
    // 15 km/h
    bicycling(15000 / 60),
    // 7.5 km/h
    walking(7500 / 60);

    // TODO (nli) use user average speed per travel mode after first usage
    private int speedInMetersPerMinute;

    TravelMode(int speedInMetersPerMinute) {
        this.speedInMetersPerMinute = speedInMetersPerMinute;
    }

    public int getSpeedInMetersPerMinute() {
        return speedInMetersPerMinute;
    }
}
