package live.noxbox.model;

import live.noxbox.R;

public enum TravelMode {

    none(0, R.string.no, 0, R.drawable.home),
    // 45 km/h
    driving(1, R.string.onCar, 45000 / 60, R.drawable.car),
    // 15 km/h
    transit(2, R.string.onTransit, 15000 / 60, R.drawable.transit),
    // 15 km/h
    bicycling(3, R.string.onBike, 15000 / 60, R.drawable.bike),
    // 7.5 km/h
    walking(4, R.string.beWalk, 7500 / 60, R.drawable.walk);

    // TODO (nli) use user average speed per travel mode after first usage
    private int id;
    private int name;
    private int speedInMetersPerMinute;
    private final int image;


    TravelMode(int id, int name, int speedInMetersPerMinute, int image) {
        this.id = id;
        this.name = name;
        this.speedInMetersPerMinute = speedInMetersPerMinute;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public int getSpeedInMetersPerMinute() {
        return speedInMetersPerMinute;
    }

    public int getImage() {
        return image;
    }

    public int getName() {
        return name;
    }

    public static TravelMode byId(int id) {
        for (TravelMode mode : TravelMode.values()) {
            if (mode.id == id) {
                return mode;
            }
        }
        return none;
    }
}
