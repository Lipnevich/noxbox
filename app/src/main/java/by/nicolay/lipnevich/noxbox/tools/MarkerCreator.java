package by.nicolay.lipnevich.noxbox.tools;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;
import by.nicolay.lipnevich.noxbox.model.Rating;
import by.nicolay.lipnevich.noxbox.model.TravelMode;

public class MarkerCreator {

    public static Marker createCustomMarker(Noxbox noxbox, Profile profile, GoogleMap googleMap, Activity activity) {
        if (noxbox.getPayer().getId().equals(profile.getId())) {
            //Noxbox.Perfomer
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .title(noxbox.getType().name())
                    .snippet("Description for noxbox type")
                    .position(noxbox.getPosition().toLatLng())
                    .icon(BitmapDescriptorFactory.fromBitmap(drawImage(
                            getIconBitmap(activity, noxbox.getType().getImage()),
                            getRatingColor(noxbox.getPerformer().getRating().getReceived()),
                            noxbox.getPrice(),
                            Color.GREEN,
                            noxbox.getEstimationTime(),
                            getEstimationColor(noxbox.getPerformer().getTravelMode()),
                            getIconTravelModeBitmap(activity,noxbox.getPerformer().getTravelMode().getImage())
                    )))
                    .anchor(0.5f, 1f));

            marker.setTag(noxbox);
            return marker;
        } else {
            //marker for Noxbox.Payer
        }
        return null;
    }

    private static Bitmap drawImage(Bitmap bitmap, int raitingColor, String price, int priceColor, String estimationTime, int timeColor, Bitmap trevalModeBitmap) {
        int borderSize = 24;
        int startPoint = 48;
        Paint raitingBorder = new Paint(Paint.ANTI_ALIAS_FLAG);
        raitingBorder.setColor(raitingColor);
        raitingBorder.setPathEffect(new DashPathEffect(getColorInterpretation(raitingColor), 0));
        raitingBorder.setStyle(Paint.Style.STROKE);
        raitingBorder.setStrokeWidth(borderSize);

        Paint priceCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        priceCircle.setColor(priceColor);
        priceCircle.setStyle(Paint.Style.FILL);
        priceCircle.setTextAlign(Paint.Align.CENTER);
        priceCircle.setTextSize(50);

        Paint timeCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        timeCircle.setColor(timeColor);
        timeCircle.setStyle(Paint.Style.FILL);
        timeCircle.setTextAlign(Paint.Align.CENTER);
        timeCircle.setTextSize(50);

        Bitmap bmpWithBorder = Bitmap.createBitmap(bitmap.getWidth() + borderSize * 4, bitmap.getHeight() + borderSize * 4, bitmap.getConfig());

        Canvas canvas = new Canvas(bmpWithBorder);
        canvas.drawBitmap(bitmap, startPoint, startPoint, null);
        canvas.drawCircle(bitmap.getWidth() / 2 + startPoint, bitmap.getHeight() / 2 + startPoint, bitmap.getHeight() / 2 + borderSize / 2, raitingBorder);
        canvas.drawCircle(startPoint, bitmap.getHeight() / 2 + startPoint, bitmap.getHeight() / 6, priceCircle);
        priceCircle.setColor(Color.WHITE);
        canvas.drawText(price, startPoint, bitmap.getHeight() / 2 + startPoint, priceCircle);
        canvas.drawCircle(startPoint + bitmap.getWidth(), bitmap.getHeight() / 2 + startPoint, bitmap.getHeight() / 6, timeCircle);
        timeCircle.setColor(Color.WHITE);
        canvas.drawText(estimationTime, startPoint + bitmap.getWidth(), bitmap.getHeight() / 2 + startPoint, timeCircle);

        canvas.drawBitmap(trevalModeBitmap, bitmap.getWidth() / 2, bitmap.getHeight(), null);
        return bmpWithBorder;
    }

    private static float[] getColorInterpretation(int color) {
        switch (color) {
            case Color.RED:
                return new float[]{30, 10};
            case Color.YELLOW:
                return new float[]{20, 10};
            default:
                return new float[]{10, 10};//Color.GREEN
        }
    }

    private static int getRatingColor(Rating rating) {
        if (rating.getLikes() >= 100) {
            return Color.GREEN;
        } else if (rating.getLikes() < 100L && rating.getLikes() >= 95L) {
            return Color.YELLOW;
        } else {
            return Color.RED;
        }
    }

    private static Bitmap getIconBitmap(Activity activity, int noxboxType) {
        return BitmapFactory.decodeResource(activity.getResources(), noxboxType);
    }

    private static Bitmap getIconTravelModeBitmap(Activity activity, int travelMode) {
        return BitmapFactory.decodeResource(activity.getResources(), travelMode);
    }

    private static int getEstimationColor(TravelMode travelMode) {
        if (travelMode != TravelMode.none) {
            return Color.RED;
        }
        return Color.GREEN;
    }


}
