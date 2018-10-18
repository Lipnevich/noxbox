package live.noxbox.tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;

import static live.noxbox.MapActivity.dpToPx;

public class MarkerCreator {
    public static Marker createPositionMarker(TravelMode travelMode, LatLng position, GoogleMap googleMap) {
        return googleMap.addMarker(new MarkerOptions()
                .position(position)
                .icon(BitmapDescriptorFactory.fromResource(travelMode.getImage()))
                .anchor(0.5f, 1f));
    }

    public static Marker createCustomMarker(Noxbox noxbox, GoogleMap googleMap, Resources resources) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, noxbox.getType().getImage());
        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, dpToPx(56), dpToPx(56), false);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(noxbox.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(resizedImage))
                .anchor(0.5f, 1f);

        Marker marker = googleMap.addMarker(markerOptions);

        marker.setTag(noxbox);

        return marker;
    }

    public static Marker createPartyMarker(Profile party, GoogleMap googleMap, Resources resources) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, party.getTravelMode().getImage());
        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, dpToPx(28), dpToPx(28), false);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(party.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(resizedImage))
                .anchor(0.5f, 1f);

        return googleMap.addMarker(markerOptions);
    }

    public static MarkerOptions createCustomMarker(Noxbox noxbox, Resources resources) {
        int height = 128;
        int width = 128;
        //density = getResources().getDisplayMetrics().density;
        // return 0.75 if it's LDPI
        // return 1.0 if it's MDPI
        // return 1.5 if it's HDPI
        // return 2.0 if it's XHDPI
        // return 3.0 if it's XXHDPI
        // return 4.0 if it's XXXHDPI

//        ldpi	@ 56.00dp	= 42.00px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
//        mdpi	@ 56.00dp	= 56.00px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
//        tvdpi	@ 56.00dp	= 74.55px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
//        hdpi	@ 56.00dp	= 84.00px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
//        xhdpi	@ 56.00dp	= 112.00px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
//        xxhdpi	@ 56.00dp	= 168.00px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
//        xxxhdpi	@ 56.00dp	= 224.00px	= 56.00sp	= 8.89mm	= 0.35in	= 25.20pt
        if (resources.getDisplayMetrics().density == 0.75) {
            height = 42;
            width = 42;
        } else if (resources.getDisplayMetrics().density == 1.0) {
            height = 56;
            width = 56;
        } else if (resources.getDisplayMetrics().density == 1.5) {
            height = 84;
            width = 84;
        } else if (resources.getDisplayMetrics().density == 2.0) {
            height = 112;
            width = 112;
        } else if (resources.getDisplayMetrics().density == 2.5) {
            height = 168;
            width = 168;
        } else if (resources.getDisplayMetrics().density == 3.0) {
            height = 224;
            width = 224;
        }
        Bitmap b = BitmapFactory.decodeResource(resources, noxbox.getType().getImage());
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);

        return new MarkerOptions()
                .position(noxbox.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
                .anchor(0.5f, 1f);
    }

}
