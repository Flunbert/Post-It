package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.ArrayList;

/**
 * Controller class for sensors.
 * Registers sensors and listens for results.
 * Formats results from sensors into usable data.
 *
 * @author Joakim Persson
 * @since 25/2/2017
 */
public class SensorController implements SensorEventListener {
    private static final String TAG = "SensorController";
    private ArrayList<Sensor> sensorList;
    private SensorManager sensorManager;
    private Controller controller;
    private Activity activity;

    /**
     * Unregisters sensors.
     */
    public void onPause() {
        sensorManager.unregisterListener(this);
        sensorList = null;
        sensorManager = null;
    }

    /**
     * Re-registers sensors.
     */
    public void onResume() {
        initSensors();
    }

    /**
     * Enum of sensors
     */
    public enum Sensors {
        proximity, rotation
    }

    /**
     * Constructor.
     * Initializes variables.
     *
     * @param controller: The controller to be used
     * @param activity:   Current Activity
     */
    public SensorController(Controller controller, Activity activity) {
        this.controller = controller;
        this.activity = activity;

        initSensors();
    }

    /**
     * Initializes and registers sensors.
     */
    private void initSensors() {
        sensorList = new ArrayList<>();
        sensorManager = (SensorManager) activity.getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (sensor != null) sensorList.add(sensor);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (sensor != null) sensorList.add(sensor);

        for (Sensor temp : sensorList)
            sensorManager.registerListener(this, temp, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                float[] values = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};
                float[] qtnValues = new float[4];
                SensorManager.getQuaternionFromVector(qtnValues, values);

                double ang = Math.atan2(2 * (qtnValues[2] * qtnValues[3] + qtnValues[0] * qtnValues[1]),
                        qtnValues[0] * qtnValues[0] - qtnValues[1] * qtnValues[1] - qtnValues[2] * qtnValues[2] + qtnValues[3] * qtnValues[3]);
                double angle = Math.toDegrees(ang);

                controller.sensorTriggered(Sensors.rotation, angle);
                break;
            case Sensor.TYPE_PROXIMITY:
                if (sensorEvent.values[0] == 0) {
                    Log.d(TAG, "onSensorChanged: proximity fired");
                    controller.sensorTriggered(Sensors.proximity, 0);
                }
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}