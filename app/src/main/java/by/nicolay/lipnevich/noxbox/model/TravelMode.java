package by.nicolay.lipnevich.noxbox.model;

import by.nicolay.lipnevich.noxbox.R;

public enum TravelMode {

    none(0, R.drawable.home),
    // 45 km/h
    driving(45000 / 60,R.drawable.car),
    // 15 km/h
    transit(15000 / 60,R.drawable.transit),
    // 15 km/h
    bicycling(15000 / 60,R.drawable.bike),
    // 7.5 km/h
    walking(7500 / 60,R.drawable.walk);

    // TODO (nli) use user average speed per travel mode after first usage
    private int speedInMetersPerMinute;
    private final int image;

    TravelMode(int speedInMetersPerMinute,int image) {
        this.speedInMetersPerMinute = speedInMetersPerMinute;
        this.image = image;
    }

    public int getSpeedInMetersPerMinute() {
        return speedInMetersPerMinute;
    }

    public int getImage() {
        return image;
    }
}
