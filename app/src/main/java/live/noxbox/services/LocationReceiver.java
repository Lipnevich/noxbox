package live.noxbox.services;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import static android.content.Context.LOCATION_SERVICE;
import static live.noxbox.services.ConnectivityManager.hideOffGps;
import static live.noxbox.services.ConnectivityManager.showOffGps;

public class LocationReceiver extends BroadcastReceiver {
    private Activity activity;
    public LocationReceiver(Activity activity){
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            hideOffGps();
        }else{
            showOffGps(activity);
        }
    }
}
