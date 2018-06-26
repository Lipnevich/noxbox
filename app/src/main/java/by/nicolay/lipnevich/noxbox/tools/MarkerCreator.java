package by.nicolay.lipnevich.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;

public class MarkerCreator {

    public static Marker createCustomMarker(Noxbox noxbox, Profile profile, GoogleMap googleMap, Activity activity) {
        if (noxbox.getPayer().getId().equals(profile.getId())) {
            //Noxbox.Perfomer
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(noxbox.getPosition().toLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(drawImage(
                            getIconBitmap(activity, noxbox.getType().getImage()),
                            getRatingColor(noxbox.getPerformer().getRating().getReceived(), activity),
                            activity.getResources().getColor(R.color.icon_background),
                            getIconTravelModeBitmap(activity, noxbox.getPerformer().getTravelMode().getImage())
                    )))
                    .anchor(0.5f, 1f));

            marker.setTag(noxbox);
            return marker;
        } else {
            //marker for Noxbox.Payer
        }
        return null;
    }

    private static Bitmap drawImage(Bitmap bitmap, int raitingColor, int backgroundColor, Bitmap travelModeBitmap) {
        int borderSize = 12;
        int startPoint = 48;
        Paint raitingBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        raitingBorder.setColor(raitingColor);
        raitingBorder.setStyle(Paint.Style.STROKE);
        raitingBorder.setStrokeWidth(borderSize);

        Paint backgroundCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundCircle.setColor(backgroundColor);
        backgroundCircle.setStyle(Paint.Style.FILL);

        Bitmap bmpWithBorder = Bitmap.createBitmap(bitmap.getWidth() + startPoint * 2 + borderSize, bitmap.getHeight() + startPoint * 2 + borderSize, bitmap.getConfig());

        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawCircle(bitmap.getWidth() / 2 + startPoint, bitmap.getHeight() / 2 + startPoint, bitmap.getHeight() / 2 + borderSize, backgroundCircle);
        canvas.drawBitmap(bitmap, startPoint, startPoint, null);
        canvas.drawCircle(bitmap.getWidth() / 2 + startPoint, bitmap.getHeight() / 2 + startPoint, bitmap.getHeight() / 2 + borderSize, raitingBorder);
        canvas.drawBitmap(travelModeBitmap, bitmap.getWidth() / 2 + startPoint / 2, bitmap.getHeight() + startPoint + borderSize, null);

        return bmpWithBorder;
    }

    private static int getRatingColor(Rating rating, Activity activity) {
        if (rating.getLikes() >= 100) {
            return activity.getResources().getColor(R.color.top_raiting_color);
        } else if (rating.getLikes() < 100L && rating.getLikes() >= 95L) {
            return activity.getResources().getColor(R.color.middle_raiting_color);
        } else {
            return activity.getResources().getColor(R.color.low_raiting_color);
        }
    }

    private static Bitmap getIconBitmap(Activity activity, int noxboxType) {
        return BitmapFactory.decodeResource(activity.getResources(), noxboxType);
    }

    private static Bitmap getIconTravelModeBitmap(Activity activity, int travelMode) {
        return BitmapFactory.decodeResource(activity.getResources(), travelMode);
    }

}
