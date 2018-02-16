package by.nicolay.lipnevich.noxbox.tools;

public enum TravelMode {
    // 45 kilometers per hour
    driving(45000 / 60),
    // 2 meters per second
    walking(60);

    private int speedInMetersPerMinute;

    TravelMode(int speedInMetersPerMinute) {
        this.speedInMetersPerMinute = speedInMetersPerMinute;
    }

    public int getSpeedInMetersPerMinute() {
        return speedInMetersPerMinute;
    }
}
