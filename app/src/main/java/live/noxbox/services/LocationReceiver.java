package live.noxbox.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import live.noxbox.MapActivity;

import static android.content.Context.LOCATION_SERVICE;
import static live.noxbox.tools.ConfirmationMessage.dismissGpsMessage;
import static live.noxbox.tools.ConfirmationMessage.messageGps;

public class LocationReceiver extends BroadcastReceiver {
    private MapActivity activity;
    public LocationReceiver(MapActivity activity){
        this.activity = activity;
    }
    @Override
    public void onReceive(Context context, Intent intent) {
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        assert locationManager != null;
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            dismissGpsMessage();
        }else{
            messageGps(activity);
        }
    }
}
