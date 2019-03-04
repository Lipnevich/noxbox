package live.noxbox.tools;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import live.noxbox.model.Noxbox;
import live.noxbox.model.Position;
import live.noxbox.model.TravelMode;

import static live.noxbox.tools.DisplayMetricsConservations.dpToPx;

public class MarkerCreator {
    public static Marker createCustomMarker(Noxbox noxbox, GoogleMap googleMap, Resources resources) {
        Bitmap bitmap = BitmapFactory.decodeResource(resources, noxbox.getType().getImageDemand());
        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, dpToPx(56), dpToPx(56), false);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(noxbox.getPosition().toLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(resizedImage))
                .anchor(0.5f, 0.5f);

        Marker marker = googleMap.addMarker(markerOptions);

        marker.setTag(noxbox);

        return marker;
    }

    public static Marker createMovingMemberMarker(TravelMode travelMode, Position position, GoogleMap googleMap, Resources resources) {
        Drawable drawable = resources.getDrawable(travelMode.getImage());
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        Bitmap resizedImage = Bitmap.createScaledBitmap(bitmap, dpToPx(42), dpToPx(42), false);
        MarkerOptions markerOptions = new MarkerOptions()
                .position(position.toLatLng())
                .icon(BitmapDescriptorFactory.fromBitmap(resizedImage))
                .anchor(0.5f, 1f);

        return googleMap.addMarker(markerOptions);
    }

}
