package live.noxbox.tools;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Strings;
import com.google.firebase.dynamiclinks.DynamicLink;
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks;

import static live.noxbox.database.AppCache.fireProfile;
import static live.noxbox.database.AppCache.profile;

public class ReferrerCatcher extends BroadcastReceiver {


    private final static String KEY = "referrer";
    public static String referrer;

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle extras = intent.getExtras();
        if (extras != null) {
            referrer = extras.getString(KEY);
        }
    }

    public static void createLink(String userId) {
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setLink(Uri.parse("https://play.google.com/store/apps/details?id=live.noxbox&" + KEY + "=" + userId))
                .setDomainUriPrefix("https://noxbox.page.link")
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().setFallbackUrl(Uri.parse("https://play.google.com/store/apps/details?id=live.noxbox")).build())
                .buildDynamicLink();

        Uri dynamicLinkUri = dynamicLink.getUri();
        Log.d("DynamicLink", dynamicLinkUri.toString());
    }


    public static void parseLink(Intent intent, Activity activity) {
        if (!Strings.isNullOrEmpty(referrer) && !profile().getReferral().equals(referrer)) {
            profile().setReferral(referrer);
            fireProfile();
            return;
        }

        FirebaseDynamicLinks.getInstance()
                .getDynamicLink(intent)
                .addOnSuccessListener(activity, pendingDynamicLinkData -> {
                    Uri deepLink = null;
                    if (pendingDynamicLinkData != null) {
                        deepLink = pendingDynamicLinkData.getLink();
                        referrer = deepLink.getQueryParameter(KEY);
                        if (!Strings.isNullOrEmpty(referrer) && !profile().getReferral().equals(referrer)) {
                            profile().setReferral(referrer);
                            fireProfile();
                        }
                    }
                })
                .addOnFailureListener(activity, Crashlytics::logException);
    }

    public static void clearReferrer() {
        referrer = null;
    }


}
