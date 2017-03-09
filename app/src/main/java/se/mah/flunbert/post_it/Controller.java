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
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;

import java.io.IOException;

/**
 * Controller class.
 *
 * @author Joakim Persson
 * @since 25/2/2017
 */
public class Controller implements SurfaceHolder.Callback {
    private boolean cameraIsOn, weatherFetched, locationFetched;
    private static final String TAG = "Controller";
    private Camera.ShutterCallback shutterCallback;
    private Camera.PictureCallback jpegCallback;
    private Camera.PictureCallback rawCallback;
    private SensorController sensorController;
    private APIController apiController;
    private SurfaceHolder surfaceHolder;
    private MainActivity mainActivity;
    private Bitmap pictureTaken;
    private byte currentCamera, refreshRate;
    private Camera camera;
    private View[] views;

    //TODO: Should these exist?
    private SurfaceView surfaceView;
    private Button btnSnap;
    private RelativeLayout defaultView;
    private RelativeLayout cameraView;
    private TextView locationView;
    private TextView weatherView;
    private Button btnSelfie;
    private Button btnSend;
    private ImageView visualHeight;
    private RelativeLayout assistanceView;
    private Switch assistanceSwitch, twitterSwitch, facebookSwitch;
    private TwitterLoginButton twitterLoginButton;
    private Button twitterLogoutButton;

    /**
     * Constructor.
     * Initializes variables.
     *
     * @param views: An array containing all view objects
     */
    public Controller(MainActivity mainActivity, View[] views) {
        this.mainActivity = mainActivity;
        this.views = views;
        apiController = new APIController(mainActivity);
        sensorController = new SensorController(this, mainActivity);
        cameraIsOn = false;
        currentCamera = 0;
        refreshRate = 0;
        weatherFetched = false;
        locationFetched = false;
        pictureTaken = null;

        initViews();
        initListeners();
        initPermissions();
        initCallbacks();

        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);


    }

    /**
     * Initializes permissions.
     */
    private void initPermissions() {
        //TODO: Maybe move to sendImage()
        if (mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            mainActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
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
        locationView = (TextView) views[4];
        weatherView = (TextView) views[5];
        btnSelfie = (Button) views[6];
        btnSend = (Button) views[7];
        visualHeight = (ImageView) views[8];
        assistanceView = (RelativeLayout) views[9];
        assistanceSwitch = (Switch) views[10];
        twitterSwitch = (Switch) views[11];
        facebookSwitch = (Switch) views[12];
        twitterLoginButton = (TwitterLoginButton) views[13];
        twitterLogoutButton = (Button) views[14];

    }

    /**
     * Initializes listeners.
     */
    private void initListeners() {
        //TODO: Check if change is needed (maybe move to constructor?)
        ButtonListener buttonListener = new ButtonListener();
        btnSnap.setOnClickListener(buttonListener);
        btnSelfie.setOnClickListener(buttonListener);
        btnSend.setOnClickListener(buttonListener);
        assistanceSwitch.setOnCheckedChangeListener(buttonListener);
        twitterLogoutButton.setOnClickListener(buttonListener);
    }

    /**
     * Initialize callbacks from camera
     */
    private void initCallbacks() {
        shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
                //TODO: Make this private or hide in a method
            }
        };
        rawCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                //TODO: Make this private or hide in a method
            }
        };
        jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                //TODO: Make this private or hide in a method
                try {
                    pauseCamera();
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
                    btnSend.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    Log.e(TAG, "onPictureTaken: ", e);
                }
            }
        };
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                twitterSwitch.setChecked(true);
                Toast.makeText(mainActivity, "Connected to twitter!", Toast.LENGTH_LONG).show();
                twitterLoginButton.setVisibility(View.GONE);
                twitterLogoutButton.setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(mainActivity, "Failure to connect", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Saves image to the phones gallery
     */
    private void sendImage() {
        //TODO: Fix variable names
        btnSend.setVisibility(View.INVISIBLE);
        assistanceView.setVisibility(View.INVISIBLE);
        defaultView.setDrawingCacheEnabled(true);
        defaultView.buildDrawingCache(true);
        Bitmap image = Bitmap.createBitmap(defaultView.getDrawingCache());
        String path = MediaStore.Images.Media.insertImage(mainActivity.getContentResolver(), image, "", "");
        defaultView.setDrawingCacheEnabled(false);
        btnSend.setVisibility(View.VISIBLE);
        assistanceView.setVisibility(View.VISIBLE);
        if (twitterSwitch.isChecked()) {
            //TODO: Send to twitter
            apiController.sendToAPI(APIController.APIs.twitter, path);
        }
        if (facebookSwitch.isChecked()) {
            //TODO: Send to facebook
        }
        clearScreen();
    }

    /**
     * Resumes/starts camera.
     */
    public void startCamera() {
        if (mainActivity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            mainActivity.requestPermissions(new String[]{Manifest.permission.CAMERA}, 1);
        } else {
            try {
                defaultView.setVisibility(View.INVISIBLE);
                cameraView.setVisibility(View.VISIBLE);
                camera = Camera.open(currentCamera);
                camera.setPreviewDisplay(surfaceHolder);
                if (currentCamera == 0) {
                    Camera.Parameters params = camera.getParameters();
                    params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                    camera.setParameters(params);
                    camera.setDisplayOrientation(90);
                } else {
                    camera.setDisplayOrientation(270);
                }
                camera.startPreview();
                //TODO: Perhaps a dialogframe instead of a Toast?
            } catch (IOException e) {
                e.printStackTrace();
            }
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

    /**
     * TODO
     *
     * @param sensor
     * @param value
     */
    public void sensorTriggered(SensorController.Sensors sensor, float[] value) {
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
     * TODO
     */
    private void switchCamera() {
        Log.d(TAG, "switchCamera:");
        if (currentCamera == 0) {
            currentCamera = 1;
            pauseCamera();
            startCamera();
        } else {
            currentCamera = 0;
            pauseCamera();
            startCamera();
        }
    }

    /**
     * Resets the screen.
     */
    private void clearScreen() {
        //TODO: Clear screen
        Log.d(TAG, "clearScreen: clearing screen");
        defaultView.setBackground(mainActivity.getResources().getDrawable(R.drawable.default_background));
        locationView.setVisibility(View.INVISIBLE);
        weatherView.setVisibility(View.INVISIBLE);
        pictureTaken = null;
        weatherFetched = false;
        locationFetched = false;
        btnSend.setVisibility(View.INVISIBLE);
    }

    /**
     * If device is held up in front of user, start camera.
     * If device is at another angle and camera is on, stop camera.
     * If device is held up above user, fetch weather from API.
     * If device is held down towards the ground, fetch location form API.
     *
     * @param value: Values from the accelerometer sensor
     */
    private void accelerometerChecks(float[] value) {
        if (refreshRate == 3) {
            visualHeight.setTop(Math.round(value[2] * 50));
            refreshRate = 0;
        } else
            refreshRate++;

        if (value[1] > 9.3 && !cameraIsOn && pictureTaken == null) {
            cameraIsOn = true;
            //TODO: Check if should start camera
            startCamera();
            Log.d(TAG, "sensorTriggered: start camera");
        } else if (value[1] < 9 && cameraIsOn) {
            //TODO: Check if should close camera
            cameraIsOn = false;
            pauseCamera();
            Log.d(TAG, "sensorTriggered: pause camera");
        } else if (value[0] < 1 && value[1] < 1 && value[2] <= -9 && !weatherFetched) {
            //TODO: Check if should fetch weather
            weatherFetched = true;
            Log.d(TAG, "sensorTriggered: fetching weather");
            weatherView.setVisibility(View.VISIBLE);
            apiController.fetchAPI(APIController.APIs.weather, weatherView);
        } else if (value[0] < 1 && value[1] < 1 && value[2] >= 9 && !locationFetched) {
            //TODO: Check if should fetch location
            locationFetched = true;
            Log.d(TAG, "sensorTriggered: fetching location");
            locationView.setVisibility(View.VISIBLE);
            apiController.fetchAPI(APIController.APIs.location, locationView);
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

    public void onPause() {
        sensorController.onPause();
    }

    public void onResume() {
        sensorController.onResume();
    }

    /**
     * Listener class for buttons.
     */
    private class ButtonListener implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        @Override
        public void onClick(View view) {
            //TODO: Dialogframe instead of Toast mayhaps?
            if (view == btnSnap && cameraIsOn) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            } else if (view == btnSelfie) {
                switchCamera();
            }else if (view == btnSend) {
                sendImage();
            }else if(view == twitterLogoutButton){
                apiController.deregisterAPI(APIController.APIs.twitter);
                twitterLogoutButton.setVisibility(View.GONE);
                twitterLoginButton.setVisibility(View.VISIBLE);
                twitterSwitch.setChecked(false);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (compoundButton == assistanceSwitch) {
                if (assistanceSwitch.isChecked())
                    assistanceView.setVisibility(View.VISIBLE);
                else
                    assistanceView.setVisibility(View.INVISIBLE);
            }
        }
    }
}