package live.noxbox.tools;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import live.noxbox.model.Position;

public class AddressManager {

    public static String provideAddressByPosition(Context context, Position position){
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        String address = "";
        String city = "";

        try {
            addresses = geocoder.getFromLocation(position.getLatitude(), position.getLongitude(), 1);
            address = addresses.get(0).getAddressLine(0);
            city = addresses.get(0).getLocality();
        } catch (IOException e) {
            Crashlytics.log(Log.WARN, "Fail to create path", e.getMessage());
            return position.getLatitude() + " " + position.getLongitude();
        }


        if (address.equals("")) {
            if (city.equals("")) {
                return position.getLatitude() + " " + position.getLongitude();
            } else {
                return city;
            }
        } else {
            return address;
        }
    }
}
