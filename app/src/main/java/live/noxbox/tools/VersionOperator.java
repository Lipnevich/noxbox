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
}
