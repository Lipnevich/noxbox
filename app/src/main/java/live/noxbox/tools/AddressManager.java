package live.noxbox.tools;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import com.crashlytics.android.Crashlytics;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

import live.noxbox.R;
import live.noxbox.model.Position;

import static com.google.common.base.Strings.isNullOrEmpty;

public class AddressManager {

    public static String provideAddressByPosition(Context context, Position position) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        String address = "";
        String city = "";
        DecimalFormat numberFormat = new DecimalFormat("##.0000");

        if (position == null) return context.getString(R.string.address);

        try {
            addresses = geocoder.getFromLocation(position.getLatitude(), position.getLongitude(), 1);

            if (addresses.size() > 0) {
                address = addresses.get(0).getAddressLine(0);
                city = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            Crashlytics.log(Log.WARN, "Fail to create path", e.getMessage());
            String lat = numberFormat.format(position.getLatitude());
            String lng = numberFormat.format(position.getLongitude());
            return lat + " " + lng;
        }

        if (isNullOrEmpty(address)) {
            if (isNullOrEmpty(city)) {
                String lat = numberFormat.format(position.getLatitude());
                String lng = numberFormat.format(position.getLongitude());
                return lat + " " + lng;
            } else {
                return city;
            }
        } else {
            return address;
        }
    }
}
