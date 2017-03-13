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

    public void onPause() {
        sensorManager.unregisterListener(this);
    }

    public void onResume() {
        initSensors();
    }

    public enum Sensors {
        proximity, rotation
    }

    /**
     * Constructor.
     * Initializes variables.
     *
     * @param controller: The controller to be used
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
                //TODO: Accelerometer has been fired
                float[] values = {sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]};
                controller.sensorTriggered(Sensors.rotation, values);
                break;
            case Sensor.TYPE_PROXIMITY:
                //TODO: Proximity sensor has been fired
                Log.d(TAG, "onSensorChanged: proximity fired");
                controller.sensorTriggered(Sensors.proximity, null);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}