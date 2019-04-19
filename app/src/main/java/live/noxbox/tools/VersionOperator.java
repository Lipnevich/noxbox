package live.noxbox.tools;

import android.os.Build;

/**
 * Created by Vladislaw Kravchenok on 08.03.2019.
 */
public class VersionOperator {

    public static boolean isSdkHighestThan23OrEqual() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isSdkHighestThan24OrEqual() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return true;
        } else {
            return false;
        }
    }
    public static boolean isSdkHighestThan26OrEqual() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return true;
        } else {
            return false;
        }
    }
}
