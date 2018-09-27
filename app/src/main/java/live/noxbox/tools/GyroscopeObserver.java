package live.noxbox.tools;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.util.LinkedList;

public class GyroscopeObserver implements SensorEventListener {
    private SensorManager mSensorManager;

    // For translate nanosecond to second.
    private static final float NS2S = 1.0f / 1000000000.0f;


    // The time in nanosecond when last sensor event happened.
    private long mLastTimestamp;

    // The radian the device already rotate along y-axis.
    private double mRotateRadianY;

    // The radian the device already rotate along x-axis.
    private double mRotateRadianX;

    // The maximum radian that the device can rotate along x-axis and y-axis.
    // The value must between (0, π/2].
    private double mMaxRotateRadian = Math.PI / 9;

    // The PanoramaImageViews to be notified when the device rotate.
    private LinkedList<PanoramaImageView> mViews = new LinkedList<>();

    public void register(Context context) {
        if (mSensorManager == null) {
            mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        }
        Sensor mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);

        mLastTimestamp = 0;
        mRotateRadianY = mRotateRadianX = 0;
    }

    public void unregister() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }
    }

    public void addPanoramaImageView(PanoramaImageView view) {
        if (view != null && !mViews.contains(view)) {
            mViews.addFirst(view);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float rotateX = event.values[0];
        float rotateY = event.values[1];


        if (mLastTimestamp == 0) {
            mLastTimestamp = event.timestamp;
            return;
        }

        final float dT = (event.timestamp - mLastTimestamp) * NS2S;
        mRotateRadianY += rotateY * dT;
        mRotateRadianX += rotateX * dT;

        mRotateRadianY = Math.max(Math.min(mRotateRadianY, mMaxRotateRadian), -mMaxRotateRadian);
        mRotateRadianX = Math.max(Math.min(mRotateRadianX, mMaxRotateRadian), -mMaxRotateRadian);

        for (PanoramaImageView view : mViews) {
            if (view != null) {
                view.updateProgress((float) (mRotateRadianX / mMaxRotateRadian), (float) (mRotateRadianY / mMaxRotateRadian));
            }
        }

        mLastTimestamp = event.timestamp;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void setMaxRotateRadian(double maxRotateRadian) {
        if (maxRotateRadian <= 0 || maxRotateRadian > Math.PI / 2) {
            throw new IllegalArgumentException("The maxRotateRadian must be between (0, π/2].");
        }
        this.mMaxRotateRadian = maxRotateRadian;
    }
}
