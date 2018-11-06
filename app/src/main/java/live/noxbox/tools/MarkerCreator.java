package live.noxbox.tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Profile;
import live.noxbox.model.TravelMode;

import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;

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
        Drawable drawable = resources.getDrawable(party.getTravelMode().getImage());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        


        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, dpToPx(28), dpToPx(28), false);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(party.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(resizedImage))
                .anchor(0.5f, 1f);

        return googleMap.addMarker(markerOptions);
    }

}
