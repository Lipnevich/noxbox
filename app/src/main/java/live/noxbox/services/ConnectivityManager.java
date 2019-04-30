package live.noxbox.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.provider.Settings;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.RelativeLayout;

import live.noxbox.MapActivity;
import live.noxbox.R;

import static live.noxbox.tools.location.LocationOperator.isLocationPermissionGranted;

/**
 * Created by Vladislaw Kravchenok on 30.04.2019.
 */
public class ConnectivityManager {
    private static RelativeLayout offline;
    private static RelativeLayout gps;

    public static void showOffline(Activity activity) {
        offline = activity.findViewById(R.id.offlineLayout);

        if (activity instanceof MapActivity && isNavigationBar(activity)) {
            indentTheBottom(activity, offline);
        }

        offline.setVisibility(View.VISIBLE);
        offline.findViewById(R.id.connect).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_SETTINGS);
            activity.startActivity(intent);
        });
    }

    public static void hideOffline() {
        if (offline != null && offline.getVisibility() == View.VISIBLE) {
            offline.setVisibility(View.GONE);
        }
    }

    public static void showOffGps(Activity activity) {
        gps = activity.findViewById(R.id.gpsOffLayout);
        if (activity instanceof MapActivity && isNavigationBar(activity)) {
            indentTheBottom(activity, gps);
        }
        gps.setVisibility(View.VISIBLE);
        gps.findViewById(R.id.connect).setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            activity.startActivity(intent);
        });
    }

    public static void hideOffGps() {
        if (gps != null && gps.getVisibility() == View.VISIBLE) {
            gps.setVisibility(View.GONE);
        }
    }

    public static void hideConnectivityMessages() {
        hideOffGps();
        hideOffline();
    }

    public static boolean checkInternetConnection(Activity activity) {
        if (NetworkReceiver.isOnline(activity)) {
            hideOffline();
            return true;
        } else {
            showOffline(activity);
            return false;
        }
    }

    public static boolean checkGpsEnabled(Activity activity) {
        if (isLocationPermissionGranted(activity)) {
            if (!isGpsEnabled(activity)) {
                showOffGps(activity);
                return false;
            } else {
                hideOffGps();
                return true;
            }
        }
        return false;
    }

    private static boolean isGpsEnabled(Activity activity) {
        LocationManager locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isNavigationBar(Activity activity) {
        boolean hasMenuKey = ViewConfiguration.get(activity).hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

        return !hasMenuKey && !hasBackKey;
    }

    private static void indentTheBottom(Activity activity, RelativeLayout root) {
        RelativeLayout innerLayout = root.findViewById(R.id.innerLayout);
        RelativeLayout.LayoutParams relativeParams = (RelativeLayout.LayoutParams) innerLayout.getLayoutParams();
        int navigationBarHeight = 0;
        int resourceId = activity.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId > 0) {
            navigationBarHeight = activity.getResources().getDimensionPixelSize(resourceId);
        }
        if (navigationBarHeight != 0) {
            relativeParams.setMargins(0, 0, 0, navigationBarHeight);
            innerLayout.setLayoutParams(relativeParams);
        }
    }
}
