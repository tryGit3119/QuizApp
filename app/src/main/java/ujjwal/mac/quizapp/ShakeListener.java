package ujjwal.mac.quizapp;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;

public class ShakeListener implements SensorEventListener {

    private static final float SHAKE_THRESHOLD_GRAVITY = 2.7F;
    private static final int SHAKE_SLOP_TIME_MS = 500;
    private static final int SHAKE_COUNT_RESET_TIME_MS = 3000;


    private long mShakeTimestamp;
    private SensorManager mSensorMgr;
    private OnShakeListener mShakeListener;
    private Context mContext;
    private int mShakeCount = 0;

    public interface OnShakeListener {
        public void onShake();
    }

    public ShakeListener(Context context) {
        mContext = context;
        mSensorMgr = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        resume();
    }

    public void setOnShakeListener(OnShakeListener listener) {
        mShakeListener = listener;
    }

    public void resume() {
        if (mSensorMgr == null) {
            throw new UnsupportedOperationException("Sensors not supported");
        }
        boolean supported = mSensorMgr.registerListener(this, mSensorMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        if (!supported) {
            Toast.makeText(mContext, "Accelerometer not found", Toast.LENGTH_SHORT).show();
        }
    }

    public void pause() {
        if (mSensorMgr != null) {
            mSensorMgr.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (mShakeListener != null) {

            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            float gX = x / SensorManager.GRAVITY_EARTH;
            float gY = y / SensorManager.GRAVITY_EARTH;
            float gZ = z / SensorManager.GRAVITY_EARTH;

            // gForce will be close to 1 when there is no movement.

            float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

            if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                final long now = System.currentTimeMillis();
                // ignore shake events too close to each other (500ms)
                if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
                    return;
                }

                // reset the shake count after 3 seconds of no shakes
                if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now) {
                    mShakeCount = 0;
                }

                mShakeTimestamp = now;
                mShakeCount++;

                mShakeListener.onShake();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
