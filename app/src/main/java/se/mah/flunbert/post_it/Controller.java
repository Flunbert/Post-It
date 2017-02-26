package se.mah.flunbert.post_it;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;

/**
 * Controller class.
 *
 * @author Joakim Persson
 * @since 25/2/2017
 */
public class Controller implements SurfaceHolder.Callback {
    private static final String TAG = "Controller";
    private MainActivity mainActivity;
    private Bitmap pictureTaken;
    private Camera camera;
    private View[] views;
    private SurfaceHolder surfaceHolder;
    private boolean canSavePictures, canTakePictures, cameraIsOn, weatherFetched, locationFetched;
    private SensorController sensorController;

    //TODO: Should these exist?
    private SurfaceView surfaceView;
    private Button btnSnap;
    private RelativeLayout defaultView;
    private RelativeLayout cameraView;
    private ImageView locationView;
    private ImageView weatherView;

    /**
     * Constructor.
     * Initializes variables.
     *
     * @param views: An array containing all view objects
     */
    public Controller(MainActivity mainActivity, View[] views) {
        this.mainActivity = mainActivity;
        this.views = views;
        sensorController = new SensorController(this, mainActivity);
        cameraIsOn = false;
        weatherFetched = false;
        locationFetched = false;
        pictureTaken = null;

        initViews();
        initListeners();
        initPermissions();

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    /**
     * Initializes permissions.
     */
    private void initPermissions() {
        //TODO: Maybe move to saveImageToGallery()
        if (mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            canSavePictures = false;
        } else canSavePictures = true;

        //TODO: Maybe move to startCamera()
        if (mainActivity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
            canTakePictures = false;
        } else canTakePictures = true;
    }

    /**
     * Initializes views.
     */
    private void initViews() {
        //TODO: Correct this
        btnSnap = (Button) views[0];
        surfaceView = (SurfaceView) views[1];
        defaultView = (RelativeLayout) views[2];
        cameraView = (RelativeLayout) views[3];
        locationView = (ImageView) views[4];
        weatherView = (ImageView) views[5];
    }

    /**
     * Initializes listeners.
     */
    private void initListeners() {
        //TODO: Check if change is needed (maybe move to constructor?)
        ButtonListener buttonListener = new ButtonListener();
        btnSnap.setOnClickListener(buttonListener);
    }

    /**
     * Saves image to the phones gallery
     */
    private void saveImageToGallery() {
        //TODO: Fix variable names
        MediaStore.Images.Media.insertImage(mainActivity.getContentResolver(), pictureTaken, "Test", "Test here also");
    }

    /**
     * Shutter callback
     */
    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            //TODO: Make this private or hide in a method
        }
    };
    /**
     * Raw image callback
     */
    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            //TODO: Make this private or hide in a method
        }
    };
    /**
     * Jpeg image callback
     */
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {
            //TODO: Make this private or hide in a method
            try {
                Bitmap picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                Bitmap scaled = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
                pictureTaken = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
                //TODO: Change name perhaps?
                Drawable drawable = new BitmapDrawable(mainActivity.getResources(), pictureTaken);
                defaultView.setBackground(drawable);
                Log.d(TAG, "Picture has been taken, scaled, and rotated");
                //TODO: Show wait symbol
            } catch (Exception e) {
                Log.e(TAG, "onPictureTaken: ", e);
            }
        }
    };

    /**
     * Resumes/starts camera.
     */
    public void startCamera() {
        try {
            if (canTakePictures) {
                defaultView.setVisibility(View.INVISIBLE);
                cameraView.setVisibility(View.VISIBLE);
                camera = Camera.open();
                camera.setPreviewDisplay(surfaceHolder);
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                camera.setParameters(params);
                camera.setDisplayOrientation(90);
                camera.startPreview();
            } else
                Toast.makeText(mainActivity, "Please give the application permission to use the camera", Toast.LENGTH_SHORT).show();
            //TODO: Perhaps a dialogframe instead of a Toast?
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                Log.d(TAG, "surfaceCreated: ");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
    }

    public void sensorTriggered(SensorController.Sensors sensor, int[] value) {
        switch (sensor) {
            case accelerometer:
                accelerometerChecks(value);
                break;
            case proximity:
                clearScreen();
                break;
        }
    }

    /**
     * Resets the screen.
     */
    private void clearScreen() {
        //TODO: Clear screen
        Log.d(TAG, "clearScreen: clearing screen");
        defaultView.setBackground(mainActivity.getResources().getDrawable(R.drawable.galleri));
        locationView.setVisibility(View.INVISIBLE);
        weatherView.setVisibility(View.INVISIBLE);
        pictureTaken = null;
        weatherFetched = false;
        locationFetched = false;
    }

    /**
     * If device is held up in front of user, start camera.
     * If device is at another angle and camera is on, stop camera.
     * If device is held up above user, fetch weather from API.
     * If device is held down towards the ground, fetch location form API.
     *
     * @param value: Values from the accelerometer sensor
     */
    private void accelerometerChecks(int[] value) {
        if (value[1] > 9 && !cameraIsOn && pictureTaken == null) {
            cameraIsOn = true;
            //TODO: Check if should start camera
            startCamera();
            Log.d(TAG, "sensorTriggered: start camera");
        } else if (value[1] < 8 && cameraIsOn) {
            //TODO: Check if should close camera
            cameraIsOn = false;
            pauseCamera();
            Log.d(TAG, "sensorTriggered: pause camera");
        } else if (value[0] == 0 && value[1] == 0 && value[2] <= -9 && !weatherFetched) {
            //TODO: Check if should fetch weather
            weatherFetched = true;
            Log.d(TAG, "sensorTriggered: fetching weather");
            weatherView.setVisibility(View.VISIBLE);
            APIController.FetchAPI(APIController.APIs.weather);
        } else if (value[0] == 0 && value[1] == 0 && value[2] >= 9 && !locationFetched) {
            //TODO: Check if should fetch location
            locationFetched = true;
            Log.d(TAG, "sensorTriggered: fetching location");
            locationView.setVisibility(View.VISIBLE);
            APIController.FetchAPI(APIController.APIs.location);
        }
    }

    /**
     * Pauses camera.
     */
    public void pauseCamera() {
        if (camera != null) {
            defaultView.setVisibility(View.VISIBLE);
            cameraView.setVisibility(View.INVISIBLE);
            camera.setPreviewCallback(null);
            camera.stopPreview();
            surfaceView.getHolder().removeCallback(this);
            camera.release();
            camera = null;
        }
    }

    /**
     * Listener class for buttons.
     */
    private class ButtonListener implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            //TODO: Dialogframe instead of Toast mayhaps?
            if (canSavePictures && cameraIsOn)
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            else
                Toast.makeText(mainActivity, "Please give the application permission to save pictures", Toast.LENGTH_SHORT).show();
        }
    }
}
