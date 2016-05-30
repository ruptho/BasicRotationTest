
package rup.tho.basicrotationtest;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private SensorManager mSensorManager;
    private Sensor mRotation;
    private TextView mTvX;
    private TextView mTvY;
    private TextView mTvZ;
    private TextView mTvDirection;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialise ui
        mTvX = (TextView) findViewById(R.id.tvX);
        mTvY = (TextView) findViewById(R.id.tvY);
        mTvZ = (TextView) findViewById(R.id.tvZ);
        mTvDirection = (TextView) findViewById(R.id.tvDirection);

        // initalise sensor stuff
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        if (mRotation == null) {
            Log.e(TAG, "Acceleration Sensor not available");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // this if not necessarly needed, but if you are planning to use more than one sensor,
        // you should include this line.
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            processRotation(event.values);
        } // else if(otherSensorEvents)...
    }


    private void processRotation(float[] rotationValues) {
        // a 3x3 rotation matrix, indexes as follows: (0,1,2; 3,4,5; 6,7,8)
        float matrixR[] = new float[9];
        // rotations around axis will be stored here
        // Attention: NOTICE THE NEGATIVES OF X AND Z HERE!
        // 0 = -Z, from -pi to pi (Z = axis towards the sky)
        // 1 = -X, from -pi/2 to pi/2 (X = axis defined as vector product Y.Z, roughly points east)
        // 2 = Y, from -pi to pi (Y = tangential to ground, points to the magnetic north pole)
        float orientationResult[] = new float[3];

        // use androids provided functions to calculate the orientation matrix as mentioned above
        SensorManager.getRotationMatrixFromVector(matrixR, rotationValues);
        SensorManager.getOrientation(matrixR, orientationResult);

        // calculate to degrees for more comfortable processing
        double orientationDegrees[] = {(Math.toDegrees(orientationResult[0]) + 360) % 360,
                (Math.toDegrees(orientationResult[1]) + 360) % 360,
                (Math.toDegrees(orientationResult[2]) + 360) % 360,
        };

        // format the text, rad|degrees
        mTvX.setText(String.format("x:%.3f|%.2f", orientationResult[1], orientationDegrees[1]));
        mTvY.setText(String.format("y:%.3f|%.2f", orientationResult[2], orientationDegrees[2]));
        mTvZ.setText(String.format("z:%.3f|%.2f", orientationResult[0], orientationDegrees[0]));

        // additional comfort, display direction by using the rotation around the z axis
        mTvDirection.setText(getCompassText(orientationDegrees[0]));
    }


    private String getCompassText(double z) {
        String compassText = getString(R.string.main_error);
        if (z > 315 + 22.5 || z <= 22.5) {
            compassText = getString(R.string.main_direction_north);
        } else if (z > 270 + 22.5 && z <= 315 + 22.5) {
            compassText = getString(R.string.main_direction_northwest);
        } else if (z > 225 + 22.5 && z <= 270 + 22.5) {
            compassText = getString(R.string.main_direction_west);
        } else if (z > 180 + 22.5 && z <= 225 + 22.5) {
            compassText = getString(R.string.main_direction_southwest);
        } else if (z > 135 + 22.5 && z <= 180 + 22.5) {
            compassText = getString(R.string.main_direction_south);
        } else if (z > 90 + 22.5 && z <= 135 + 22.5) {
            compassText = getString(R.string.main_direction_southeast);
        } else if (z > 45 + 22.5 && z <= 90 + 22.5) {
            compassText = getString(R.string.main_direction_east);
        } else if (z > 22.5 && z <= 45 + 22.5) {
            compassText = getString(R.string.main_direction_northeast);
        }

        return compassText;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // nothing to do here.
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Register the sensor listeners here
        // Normal Sensor delay (this is the slowest)
        // going from fastest to slowest (in milliseconds), you would expect the delay to be:
        // SENSOR_DELAY_FASTEST < SENSOR_DELAY_GAME < SENSOR_DELAY_UI < SENSOR_DELAY_NORMAL
        //     ~0-20                   ~40                ~80              ~200
        // note that the exact values depend on the device used, those here are an approximation
        mSensorManager.registerListener(this, mRotation, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Unregister sensor listeners here when you pause your app, or you'll drain battery
        mSensorManager.unregisterListener(this);
    }
}
