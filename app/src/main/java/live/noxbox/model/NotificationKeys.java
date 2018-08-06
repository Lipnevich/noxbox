package live.noxbox.model;

/**
 * Created by nicolay.lipnevich on 4/29/2018.
 */
public class NotificationKeys {

    private String android, ios;

    public String getAndroid() {
        return android;
    }

    public NotificationKeys setAndroid(String android) {
        this.android = android;
        return this;
    }

    public String getIos() {
        return ios;
    }

    public NotificationKeys setIos(String ios) {
        this.ios = ios;
        return this;
    }
}
