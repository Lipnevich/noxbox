package by.nicolay.lipnevich.noxbox.experiments;

import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;

import by.nicolay.lipnevich.noxbox.R;

public class MapRipple {
    private GoogleMap googleMap;
    public void withLatLng(LatLng latLng) {
        this.latLng = latLng;
    }
    private volatile LatLng latLng;
    private Bitmap backgroundImage;                     //ripple image.
    private float transparency = 0.5f;                    //transparency of image.
    private volatile double distance = 2000;                       //distance to which ripple should be shown in metres
    private int numberOfRipples = 1;                      //number of ripples to show, max = 4
    private volatile int fillColor = Color.TRANSPARENT;           //fillcolor of circle
    private volatile int strokeColor = Color.BLACK;               //border color of circle
    private volatile int strokewidth = 10;                          //border width of circle
    private long durationBetweenTwoRipples = 4000;      //in microseconds.
    private long rippleDuration = 12000;                //in microseconds
    private ValueAnimator vAnimators[];
    private Handler handlers[];
    private GroundOverlay gOverlays[];
    private GradientDrawable drawable;
    private boolean isAnimationRunning = false;
    public MapRipple(GoogleMap googleMap, LatLng latLng, Context context) {
        this.googleMap = googleMap;
        this.latLng = latLng;
        drawable = (GradientDrawable) context.getResources().getDrawable(R.drawable.request_circle);
        vAnimators = new ValueAnimator[4];
        handlers = new Handler[4];
        gOverlays = new GroundOverlay[4];
    }
    public void OverLay(final GroundOverlay groundOverlay, int i) {
        vAnimators[i] = ValueAnimator.ofInt(0, (int) distance);
        vAnimators[i].setRepeatCount(ValueAnimator.INFINITE);
        vAnimators[i].setRepeatMode(ValueAnimator.RESTART);
        vAnimators[i].setDuration(rippleDuration);
        vAnimators[i].setEvaluator(new IntEvaluator());
        vAnimators[i].setInterpolator(new LinearInterpolator());
        vAnimators[i].addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final Integer i = (Integer) valueAnimator.getAnimatedValue();
                groundOverlay.setDimensions(i);
            }
        });
        vAnimators[i].start();
    }
    private Bitmap drawableToBitmap(Drawable drawable) {
        Bitmap bitmap = null;
        if (drawable instanceof BitmapDrawable) {
            BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
            if (bitmapDrawable.getBitmap() != null) {
                return bitmapDrawable.getBitmap();
            }
        }
        if (drawable.getIntrinsicWidth() <= 0 || drawable.getIntrinsicHeight() <= 0) {
            bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888); // Single color bitmap will be created of 1x1 pixel
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        }
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bitmap;
    }
    public MapRipple withTransparency(float transparency) {
        this.transparency = transparency;
        return this;
    }
    public MapRipple withDistance(double distance) {
        if (distance < 200)
            distance = 200;
        this.distance = distance;
        return this;
    }
    public MapRipple withNumberOfRipples(int numberOfRipples) {
        if (numberOfRipples > 4 || numberOfRipples < 1)
            numberOfRipples = 4;
        this.numberOfRipples = numberOfRipples;
        return this;
    }
    public MapRipple withFillColor(int fillColor) {
        this.fillColor = fillColor;
        return this;
    }
    public MapRipple withStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
        return this;
    }
    public MapRipple withStrokewidth(int strokewidth) {
        this.strokewidth = strokewidth;
        return this;
    }
    public MapRipple withDurationBetweenTwoRipples(long durationBetweenTwoRipples) {
        this.durationBetweenTwoRipples = durationBetweenTwoRipples;
        return this;
    }
    public boolean isAnimationRunning() {
        return isAnimationRunning;

    }
    public MapRipple withRippleDuration(long rippleDuration) {
        this.rippleDuration = rippleDuration;
        return this;
    }
    public void startRippleMapAnimation() {
        drawable.setColor(fillColor);
        float d = Resources.getSystem().getDisplayMetrics().density;
        int width = (int) (strokewidth * d); // margin in pixels
        drawable.setStroke(width, strokeColor);
        backgroundImage = drawableToBitmap(drawable);
        for (int i = 0; i < numberOfRipples; i++) {
            if (i == 0) {
                handlers[i] = new Handler();
                handlers[i].postDelayed(runnable1, durationBetweenTwoRipples * i);
            }
            if (i == 1) {
                handlers[i] = new Handler();
                handlers[i].postDelayed(runnable2, durationBetweenTwoRipples * i);
            }
            if (i == 2) {
                handlers[i] = new Handler();
                handlers[i].postDelayed(runnable3, durationBetweenTwoRipples * i);
            }
            if (i == 3) {
                handlers[i] = new Handler();
                handlers[i].postDelayed(runnable4, durationBetweenTwoRipples * i);
            }
        }
        isAnimationRunning = true;
    }
    final Runnable runnable1 = new Runnable() {
        @Override
        public void run() {
            gOverlays[0] = googleMap.addGroundOverlay(new
                    GroundOverlayOptions()
                    .position(latLng, (int) distance)
                    .transparency(transparency)
                    .image(BitmapDescriptorFactory.fromBitmap(backgroundImage)));
            OverLay(gOverlays[0], 0);
        }
    };
    final Runnable runnable2 = new Runnable() {
        @Override
        public void run() {
            gOverlays[1] = googleMap.addGroundOverlay(new
                    GroundOverlayOptions()
                    .position(latLng, (int) distance)
                    .transparency(transparency)
                    .image(BitmapDescriptorFactory.fromBitmap(backgroundImage)));
            OverLay(gOverlays[1], 1);
        }
    };
    final Runnable runnable3 = new Runnable() {
        @Override
        public void run() {
            gOverlays[2] = googleMap.addGroundOverlay(new
                    GroundOverlayOptions()
                    .position(latLng, (int) distance)
                    .transparency(transparency)
                    .image(BitmapDescriptorFactory.fromBitmap(backgroundImage)));
            OverLay(gOverlays[2], 2);
        }
    };
    final Runnable runnable4 = new Runnable() {
        @Override
        public void run() {
            gOverlays[3] = googleMap.addGroundOverlay(new
                    GroundOverlayOptions()
                    .position(latLng, (int) distance)
                    .transparency(transparency)
                    .image(BitmapDescriptorFactory.fromBitmap(backgroundImage)));
            OverLay(gOverlays[3], 3);
        }
    };
    public void stopRippleMapAnimation() {
        try {
            for (int i = 0; i < numberOfRipples; i++) {
                if (i == 0) {
                    handlers[i].removeCallbacks(runnable1);
                    vAnimators[i].cancel();
                    gOverlays[i].remove();
                }
                if (i == 1) {
                    handlers[i].removeCallbacks(runnable2);
                    vAnimators[i].cancel();
                    gOverlays[i].remove();
                }
                if (i == 2) {
                    handlers[i].removeCallbacks(runnable3);
                    vAnimators[i].cancel();
                    gOverlays[i].remove();
                }
                if (i == 3) {
                    handlers[i].removeCallbacks(runnable4);
                    vAnimators[i].cancel();
                    gOverlays[i].remove();
                }
            }
        } catch (Exception e) {
        }
        isAnimationRunning = false;
    }
}
