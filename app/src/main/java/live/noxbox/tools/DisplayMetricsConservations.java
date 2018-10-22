package live.noxbox.tools;

import android.content.res.Resources;

public class DisplayMetricsConservations {
    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }
}
