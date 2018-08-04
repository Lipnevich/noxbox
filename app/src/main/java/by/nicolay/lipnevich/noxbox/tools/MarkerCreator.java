package by.nicolay.lipnevich.noxbox.tools;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.animation.AccelerateDecelerateInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import by.nicolay.lipnevich.noxbox.R;
import by.nicolay.lipnevich.noxbox.model.Noxbox;
import by.nicolay.lipnevich.noxbox.model.Profile;

public class MarkerCreator {
    public static int bitmapCenter;
    public static Marker createCustomMarker(Noxbox noxbox, Profile profile, GoogleMap googleMap, Activity activity) {
        switch (noxbox.getRole()) {
            case supply:
                Marker marker = googleMap.addMarker(new MarkerOptions()
                        .position(noxbox.getPosition().toLatLng())
                        .icon(BitmapDescriptorFactory.fromBitmap(drawImage(
                                getIconBitmap(activity, noxbox.getType().getImage()),
                                getRatingColor(noxbox.getOwner().ratingToPercentage(), activity,noxbox),
                                activity.getResources().getColor(R.color.icon_background),
                                getIconTravelModeBitmap(activity, noxbox.getOwner().getTravelMode().getImage())
                )))
                .anchor(0.5f, 1f));

                marker.setTag(noxbox);
                return marker;
            case demand:
                Marker demand = googleMap.addMarker(new MarkerOptions()
                        .position(noxbox.getPosition().toLatLng())
                        .icon(BitmapDescriptorFactory.fromBitmap(drawImage(
                                getIconBitmap(activity, noxbox.getType().getImage()),
                                getRatingColor(noxbox.getOwner().ratingToPercentage(), activity,noxbox),
                                activity.getResources().getColor(R.color.icon_background),
                                getIconTravelModeBitmap(activity, noxbox.getOwner().getTravelMode().getImage())
                        )))
                        .anchor(0.5f, 1f));

                demand.setTag(noxbox);
                return demand;
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

    private static int getRatingColor(int percentage, Activity activity,Noxbox noxbox) {
//        if(TimeManager.compareTime(noxbox.getWorkSchedule().getStartInHours(),noxbox.getWorkSchedule().getStartInMinutes(),activity)){
//            return activity.getResources().getColor(R.color.divider);
//        }
        if (percentage == 100) {
            return activity.getResources().getColor(R.color.top_rating_color);
        } else if (percentage < 100 && percentage >= 95) {
            return activity.getResources().getColor(R.color.middle_rating_color);
        } else {
            return activity.getResources().getColor(R.color.low_rating_color);
        }
    }

    private static Bitmap getIconBitmap(Activity activity, int noxboxType) {
        return BitmapFactory.decodeResource(activity.getResources(), noxboxType);
    }

    private static Bitmap getIconTravelModeBitmap(Activity activity, int travelMode) {
        return BitmapFactory.decodeResource(activity.getResources(), travelMode);
    }


    public static void addPulsatingEffect(GoogleMap googleMap, Profile profile,Activity activity){
//        MapRipple mapRipple = new MapRipple(googleMap, profile.getPosition().toLatLng(), activity)
//                .withNumberOfRipples(3)
//                .withFillColor(R.color.primary)
//                .withStrokeColor(R.color.secondary)
//                .withStrokewidth(10)      // 10dp
//                .withDistance(2000)      // 2000 metres radius
//                .withRippleDuration(12000)    //12000ms
//                .withTransparency(0.5f);
//        mapRipple.startRippleMapAnimation();
        final Circle circle = googleMap.addCircle(new CircleOptions().center(profile.getCurrent().getPosition().toLatLng())
                .strokeWidth(12)
                .strokeColor(Color.GREEN)
                .radius(250));

        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
        valueAnimator.setRepeatMode(ValueAnimator.RESTART);  /* PULSE */
        valueAnimator.setIntValues(0, 250);
        valueAnimator.setDuration(1000);
        valueAnimator.setEvaluator(new IntEvaluator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                circle.setRadius(animatedFraction * 250);
            }
        });
        valueAnimator.start();
    }
}
