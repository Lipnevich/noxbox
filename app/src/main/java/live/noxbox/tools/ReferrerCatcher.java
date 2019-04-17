package live.noxbox.tools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

public class ReferrerCatcher extends BroadcastReceiver {


    public final static String KEY = "referrer";
    public static String referrer;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            referrer = extras.getString(KEY);
        }
    }

    public static void createLink(String userId) {
        Uri link = Uri.parse("https://play.google.com/store/apps/details?id=live.noxbox&" + KEY + "=" + userId);
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setDomainUriPrefix("https://noxbox.page.link")
                .setLink(link)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().setFallbackUrl(link).build())
                .buildDynamicLink();

        Log.d("DynamicLink", dynamicLink.getUri().toString());
    }


    public static void parseLink(Intent intent, Activity activity) {
        if(intent == null) {
            return;
        }

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(activity, pendingDynamicLinkData -> {
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        referrer = deepLink.getQueryParameter(KEY);
                    }
                })
                .addOnFailureListener(activity, Crashlytics::logException);
    }

    public static void clearReferrer() {
        referrer = null;
    }


}
