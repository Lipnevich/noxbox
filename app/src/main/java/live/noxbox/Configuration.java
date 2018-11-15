package live.noxbox;

import java.math.BigDecimal;

public interface Configuration {
    int REQUESTING_AND_ACCEPTING_TIMEOUT_IN_SECONDS = 60;
    int REQUESTING_AND_ACCEPTING_TIMEOUT_IN_MILLIS = 60 * 1000;
    String START_TIME = "00:00:00";
    double RADIUS_IN_METERS = 50 * 1000;
    int MIN_RATE_IN_PERCENTAGE = 90;
    String CURRENCY = "Waves";
    int LOCATION_PERMISSION_REQUEST_CODE = 911;
    long MINIMUM_PAYMENT_TIME_MILLIS = 15 * 60 * 1000;
    float MINIMUM_FACE_SIZE = 0.6F;
    float MINIMUM_PROBABILITY_FOR_ACCEPTANCE = 0.6F;
    String FIVE_MINUTES_PART_OF_HOUR = "12";
    int DEFAULT_BALANCE_SCALE = 10;
    String PRICE_FORMAT = "###.###";
    BigDecimal QUARTER = new BigDecimal("0.25");
    double ADDRESS_SEARCH_RADIUS_IN_METERS = 100000D;
    int MAX_ZOOM_LEVEL = 18;
    String CHANNEL_ID = "noxbox_channel";
    int CLUSTER_RENDERING_FREQUENCY = 500;

}
