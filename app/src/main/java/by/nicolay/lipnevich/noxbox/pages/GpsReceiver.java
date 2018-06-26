package by.nicolay.lipnevich.noxbox.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

public class GpsReceiver extends BroadcastReceiver {
    public GpsReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                //gps enabled
            } else {
                //TODO enable GPS(vlad)
            }
        }
    }
}
