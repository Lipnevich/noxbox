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
    int MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 002;

    long MINIMUM_PAYMENT_TIME_MILLIS = 15 * 60 * 1000;
    float MINIMUM_FACE_SIZE = 0.05F;
    float MINIMUM_PROBABILITY_FOR_ACCEPTANCE = 0.05F;
    int DEFAULT_BALANCE_SCALE = 10;
    double ADDRESS_SEARCH_RADIUS_IN_METERS = 100000D;
    int MAX_ZOOM_LEVEL = 18;
    int MIN_ZOOM_LEVEL = 1;
    int DEFAULT_ZOOM_LEVEL = 11;
    String CHANNEL_ID = "noxbox_channel";
    int CLUSTER_RENDERING_MIN_FREQUENCY = 3000;
    int CLUSTER_RENDERING_MAX_FREQUENCY = 400;
    long RATINGS_UPDATE_MIN_FREQUENCY = BuildConfig.DEBUG ? 5000 : 60 * 60 * 1000;

    int TIME_INTERVAL_BETWEEN_SINGLES_LOCATION_UPDATES_IN_MILLIS = 11000;
    int MINIMUM_TIME_INTERVAL_BETWEEN_LOCATION_UPDATES_IN_MILLIS = 5000;
    int MINIMUM_CHANGE_DISTANCE_BETWEEN_RECEIVE_IN_METERS = 12;

    String DEFAULT_PRICE = "5";
    String MINIMUM_PRICE = "0.5";

    String FIRST_RUN_KEY = "FIRSTRUN";
    int MAX_MINUTES =  15;



    int DEFAULT_MARKER_SIZE = 56;
    int BIG_MARKER_SIZE = 64;

    BigDecimal NOXBOX_FEE = new BigDecimal("0.072");

    String AUTHORITY = "live.noxbox.fileprovider";
}
