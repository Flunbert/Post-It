package se.mah.flunbert.post_it;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.widget.Toast;

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

    public enum Sensors {
        proximity, accelerometer
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

        for (Sensor temp : sensorList) {
            sensorManager.registerListener(this, temp, SensorManager.SENSOR_DELAY_UI);
            Toast.makeText(activity, temp.getName() + " sensor registered", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        switch (sensorEvent.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                //TODO: Accelerometer has been fired
                int[] values = {Math.round(sensorEvent.values[0]), Math.round(sensorEvent.values[1]), Math.round(sensorEvent.values[2])};
                controller.sensorTriggered(Sensors.accelerometer, values);
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