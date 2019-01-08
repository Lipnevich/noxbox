package live.noxbox.tools;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;


public class PlayMarketManager {
    public static void openApplicationMarketPage(Context context) {
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + context.getPackageName())));
        } catch (android.content.ActivityNotFoundException anfe) {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName())));
        }
    }
}
