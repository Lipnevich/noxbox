package live.noxbox;

import java.math.BigDecimal;

public interface Constants {
    long REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS = 60 * 1000;
    double RADIUS_IN_METERS = 50 * 1000;
    int MIN_RATE_IN_PERCENTAGE = 90;
    int NOVICE_LIKES = 5;

    int LOCATION_PERMISSION_REQUEST_CODE = 911;
    int LOCATION_PERMISSION_REQUEST_CODE_ON_PUBLISH = 912;
    int LOCATION_PERMISSION_REQUEST_CODE_ON_UPDATE = 913;
    int LOCATION_PERMISSION_REQUEST_CODE_OTHER_SITUATIONS = 914;
    int CAMERA_PERMISSION_REQUEST_CODE = 000;
    int REQUEST_IMAGE_CAPTURE = 001;

    long MINIMUM_PAYMENT_TIME_MILLIS = 15 * 60 * 1000;
    float MINIMUM_FACE_SIZE = 0.05F;
    float MINIMUM_PROBABILITY_FOR_ACCEPTANCE = 0.05F;
    String FIVE_MINUTES_PART_OF_HOUR = "12";
    int DEFAULT_BALANCE_SCALE = 10;
    BigDecimal QUARTER = new BigDecimal("0.25");
    double ADDRESS_SEARCH_RADIUS_IN_METERS = 100000D;
    int MAX_ZOOM_LEVEL = 18;
    int MIN_ZOOM_LEVEL = 1;
    int DEFAULT_ZOOM_LEVEL = 11;
    String CHANNEL_ID = "noxbox_channel";
    int CLUSTER_RENDERING_MIN_FREQUENCY = 3000;
    int CLUSTER_RENDERING_MAX_FREQUENCY = 400;
    float COMISSION_FEE = 0.1F;
    long RATINGS_UPDATE_MIN_FREQUENCY = BuildConfig.DEBUG ? 5000 : 60 * 60 * 1000;

    int MINIMUM_TIME_INTERVAL_BETWEEN_GPS_ACCESS_IN_SECONDS = 3;
    int MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS = 12;

    String DEFAULT_PRICE = "5";
    String MINIMUM_PRICE = "0.5";

    String FIRST_RUN_KEY = "FIRSTRUN";
    int MAX_MINUTES =  15;
}
