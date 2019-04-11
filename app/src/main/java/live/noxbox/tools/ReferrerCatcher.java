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

import live.noxbox.database.Firestore;
import live.noxbox.debug.DebugMessage;

import static live.noxbox.database.AppCache.NONE;
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
        Uri link = Uri.parse("https://play.google.com/store/apps/details?id=live.noxbox&" + KEY + "=" + userId);
        DynamicLink dynamicLink = FirebaseDynamicLinks.getInstance().createDynamicLink()
                .setDomainUriPrefix("https://noxbox.page.link")
                .setLink(link)
                .setAndroidParameters(new DynamicLink.AndroidParameters.Builder().setFallbackUrl(link).build())
                .buildDynamicLink();

        Log.d("DynamicLink", dynamicLink.getUri().toString());
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
                        DebugMessage.popup(activity, "new:" + referrer
                                + ";old:" + profile().getReferral());

                        if (!Strings.isNullOrEmpty(referrer) && !profile().getReferral().equals(referrer)
                                && !profile().getId().equals(referrer)) {
                            profile().setReferral(referrer);
                            Firestore.writeProfile(profile(), NONE);
                        }
                    }
                })
                .addOnFailureListener(activity, Crashlytics::logException);
    }

    public static void clearReferrer() {
        referrer = null;
    }


}
