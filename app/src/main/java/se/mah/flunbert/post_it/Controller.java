package se.mah.flunbert.post_it;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.provider.MediaStore;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.larswerkman.holocolorpicker.ColorPicker;
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
    private ImageView visualHeight, settingsButton, colourButton, btnSelfie, btnSnap, btnSend;
    private Switch assistanceSwitch, twitterSwitch, facebookSwitch;
    private RelativeLayout assistanceView, defaultView, cameraView;
    private boolean cameraIsOn, weatherFetched, locationFetched;
    private static final String TAG = "Controller";
    private Camera.ShutterCallback shutterCallback;
    private TwitterLoginButton twitterLoginButton;
    private LinearLayout settingsView, colourView;
    private Camera.PictureCallback jpegCallback;
    private Camera.PictureCallback rawCallback;
    private TextView locationView, weatherView, tvPreviewColour;
    private SensorController sensorController;
    private byte currentCamera, refreshRate;
    private APIController apiController;
    private SurfaceHolder surfaceHolder;
    private Button twitterLogoutButton;
    private MainActivity mainActivity;
    private ColorPicker colourPicker;
    private SurfaceView surfaceView;
    private Bitmap pictureTaken;
    private DrawerLayout drawer;
    private Camera camera;

    /**
     * Constructor.
     * Initializes components.
     *
     * @param mainActivity: Reference to the MainActivity
     */
    public Controller(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
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
        if (mainActivity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)
            mainActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
    }

    /**
     * Initializes views.
     */
    private void initViews() {
        surfaceView = (SurfaceView) mainActivity.findViewById(R.id.cameraHolder);
        defaultView = (RelativeLayout) mainActivity.findViewById(R.id.defaultView);
        cameraView = (RelativeLayout) mainActivity.findViewById(R.id.cameraView);
        weatherView = (TextView) mainActivity.findViewById(R.id.ivWeather);
        locationView = (TextView) mainActivity.findViewById(R.id.ivLocation);
        btnSnap = (ImageView) mainActivity.findViewById(R.id.btnSnap);
        btnSelfie = (ImageView) mainActivity.findViewById(R.id.btnSelfie);
        btnSend = (ImageView) mainActivity.findViewById(R.id.btnSend);
        visualHeight = (ImageView) mainActivity.findViewById(R.id.visualHeight);
        assistanceView = (RelativeLayout) mainActivity.findViewById(R.id.assistanceView);
        assistanceSwitch = (Switch) mainActivity.findViewById(R.id.assistance_switch);
        twitterSwitch = (Switch) mainActivity.findViewById(R.id.twitter_switch);
        facebookSwitch = (Switch) mainActivity.findViewById(R.id.facebook_switch);
        drawer = (DrawerLayout) mainActivity.findViewById(R.id.drawer);
        settingsView = (LinearLayout) mainActivity.findViewById(R.id.settingsView);
        colourView = (LinearLayout) mainActivity.findViewById(R.id.colourView);
        settingsButton = (ImageView) mainActivity.findViewById(R.id.settingsButton);
        colourButton = (ImageView) mainActivity.findViewById(R.id.colourButton);
        twitterLoginButton = (TwitterLoginButton) mainActivity.findViewById(R.id.twitter_login_button);
        twitterLogoutButton = (Button) mainActivity.findViewById(R.id.twitter_logout_button);
        colourPicker = (ColorPicker) mainActivity.findViewById(R.id.colourPicker);
        tvPreviewColour = (TextView) mainActivity.findViewById(R.id.tvPreviewColour);

        colourPicker.setOldCenterColor(Color.WHITE);
    }

    /**
     * Initializes listeners.
     */
    private void initListeners() {
        ButtonListener buttonListener = new ButtonListener();
        btnSnap.setOnClickListener(buttonListener);
        btnSelfie.setOnClickListener(buttonListener);
        btnSend.setOnClickListener(buttonListener);
        assistanceSwitch.setOnCheckedChangeListener(buttonListener);
        twitterLogoutButton.setOnClickListener(buttonListener);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(settingsView);
            }
        });
        colourButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawer.openDrawer(colourView);
            }
        });
        colourPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                weatherView.setTextColor(colourPicker.getColor());
                locationView.setTextColor(colourPicker.getColor());
                tvPreviewColour.setTextColor(colourPicker.getColor());
            }
        });
    }

    /**
     * Initialize callbacks from camera
     */
    private void initCallbacks() {
        shutterCallback = new Camera.ShutterCallback() {
            @Override
            public void onShutter() {
            }
        };
        rawCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
            }
        };
        jpegCallback = new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] bytes, Camera camera) {
                try {
                    pauseCamera();
                    Bitmap picture = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    Matrix matrix = new Matrix();
                    matrix.postRotate(90);
                    Bitmap scaled = Bitmap.createScaledBitmap(picture, picture.getWidth(), picture.getHeight(), true);
                    pictureTaken = Bitmap.createBitmap(scaled, 0, 0, scaled.getWidth(), scaled.getHeight(), matrix, true);
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
        btnSend.setVisibility(View.INVISIBLE);
        settingsButton.setVisibility(View.INVISIBLE);
        colourButton.setVisibility(View.INVISIBLE);
        assistanceView.setVisibility(View.INVISIBLE);
        defaultView.setDrawingCacheEnabled(true);
        defaultView.buildDrawingCache(true);
        Bitmap image = Bitmap.createBitmap(defaultView.getDrawingCache());
        String path = MediaStore.Images.Media.insertImage(mainActivity.getContentResolver(), image, "", "");
        defaultView.setDrawingCacheEnabled(false);
        btnSend.setVisibility(View.VISIBLE);
        assistanceView.setVisibility(View.VISIBLE);
        settingsButton.setVisibility(View.VISIBLE);
        colourButton.setVisibility(View.VISIBLE);
        if (twitterSwitch.isChecked())
            apiController.sendTweet(path);
        if (facebookSwitch.isChecked())
            apiController.sendToFacebook(image);
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
                } else
                    camera.setDisplayOrientation(270);
                camera.startPreview();
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
     * Gets called when a sensor has been triggered.
     * Checks which sensor it is and calls methods accordingly.
     *
     * @param sensor: The sensor that was triggered
     * @param value:  The formatted value from the sensor
     */
    public void sensorTriggered(SensorController.Sensors sensor, double value) {
        switch (sensor) {
            case rotation:
                accelerometerChecks(value);
                break;
            case proximity:
                clearScreen();
                break;
        }
    }

    /**
     * Switches camera between front and back
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
     * @param angle: Value from the rotation sensor
     */
    private void accelerometerChecks(double angle) {
        if (refreshRate == 3) {
            visualHeight.setTop((int) Math.round(angle));
            refreshRate = 0;
        } else
            refreshRate++;

        if (angle >= 60 && angle < 120 && !cameraIsOn && pictureTaken == null) {
            cameraIsOn = true;
            startCamera();
            Log.d(TAG, "sensorTriggered: start camera");
        } else if ((angle < 60 || angle >= 120) && cameraIsOn) {
            cameraIsOn = false;
            pauseCamera();
            Log.d(TAG, "sensorTriggered: pause camera");
        } else if (angle >= 150 && !weatherFetched && angle > 0) {
            weatherFetched = true;
            Log.d(TAG, "sensorTriggered: fetching weather");
            weatherView.setVisibility(View.VISIBLE);
            apiController.fetchAPI(APIController.APIs.weather, weatherView);
        } else if (angle < 30 && !locationFetched && angle > 0) {
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

    /**
     * Unregisters sensors.
     */
    public void onPause() {
        sensorController.onPause();
    }

    /**
     * Re-registers sensors.
     */
    public void onResume() {
        sensorController.onResume();
    }

    /**
     * Listener class for buttons.
     */
    private class ButtonListener implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {
        @Override
        public void onClick(View view) {
            if (view == btnSnap && cameraIsOn) {
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            } else if (view == btnSelfie) {
                switchCamera();
            } else if (view == btnSend) {
                sendImage();
            } else if (view == twitterLogoutButton) {
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