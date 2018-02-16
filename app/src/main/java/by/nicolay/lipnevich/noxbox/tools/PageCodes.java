package by.nicolay.lipnevich.noxbox.tools;

/**
 * Created by nicolay.lipnevich on 21/06/2017.
 */
public enum PageCodes {

    MY_CAR(71),
    WALLET(2),
    HISTORY(3);

    private int code;

    PageCodes(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
