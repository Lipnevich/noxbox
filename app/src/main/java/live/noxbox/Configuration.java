package live.noxbox;

public interface Configuration {

    int ACCEPTANCE_TIMEOUT_IN_SECONDS = 30;
    double RADIUS_IN_METERS = 50 * 1000;
    int MIN_RATE_IN_PERCENTAGE = 90;
    String CURRENCY = "Waves";
    int DAY_TIME_START_IN_HOURS = 10;
    int DAY_TIME_EHD_IN_HOURS = 18;
    int NIGHT_TIME_START_IN_HOURS = 18;
    int NIGHT_TIME_EHD_IN_HOURS = 10;
    int LOCATION_PERMISSION_REQUEST_CODE = 911;
    long MINIMUM_PAYMENT_TIME_MILLIS = 900000;

}
