package se.mah.flunbert.post_it;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Controller class for using the new camera API (camera2).
 *
 * @author Joakim Persson
 * @since 12/3/2017
 */
public class CameraController {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    private CameraCaptureSession.CaptureCallback captureCallback;
    private static final String TAG = "CameraController";
    private CameraDevice.StateCallback stateCallback;
    private CaptureRequest.Builder captureBuilder;
    private CameraCaptureSession captureSession;
    private CaptureRequest captureRequest;
    private RelativeLayout defaultView;
    private HandlerThread cameraThread;
    private CameraDevice cameraDevice;
    private Handler cameraHandler;
    private Surface cameraSurface;
    private ImageReader reader;
    private Activity activity;
    private String cameraID;
    private Bitmap picture;

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    public CameraController(Activity activity, Surface cameraSurface, RelativeLayout defaultView) {
        this.activity = activity;
        this.cameraSurface = cameraSurface;
        this.defaultView = defaultView;
        initCallbacks();
    }

    public void startCameraListener() {
        startBackgroundThread();
    }

    public void stopCameraListener() {
        stopBackgroundThread();
    }

    public void showCamera() {
        openCamera();
    }

    public void hideCamera() {
        closeCamera();
    }

    public void snapPicture() {
        takePicture();
    }

    private void initCallbacks() {
        stateCallback = new CameraDevice.StateCallback() {
            @Override
            public void onOpened(CameraDevice cameraDevice) {
                Log.d(TAG, "Camera opened");
                CameraController.this.cameraDevice = cameraDevice;
                createCameraPreview();
            }

            @Override
            public void onDisconnected(CameraDevice cameraDevice) {
                cameraDevice.close();
            }

            @Override
            public void onError(CameraDevice cameraDevice, int i) {
                CameraController.this.cameraDevice.close();
                CameraController.this.cameraDevice = null;
            }
        };
        captureCallback = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                createCameraPreview();
            }
        };
    }

    private void startBackgroundThread() {
        cameraThread = new HandlerThread("Camera thread");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
    }

    private void stopBackgroundThread() {
        cameraThread.quitSafely();
        try {
            cameraThread.join();
            cameraThread = null;
            cameraHandler = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        if (cameraDevice == null) {
            Log.e(TAG, "Cameradevice is null");
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }
            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<Surface>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(cameraSurface);
            int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation));
//            final File file = new File(Environment.getExternalStorageDirectory() + "/pic.jpg");
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {
                    Log.d(TAG, "onImageAvailable: ");
                    Image image = imageReader.acquireNextImage();
                    try {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        image.close();
                        save(bytes);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            };
            reader.setOnImageAvailableListener(readerListener, cameraHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
//                    Toast.makeText(activity, "Saved:" + file, Toast.LENGTH_SHORT).show();
                    createCameraPreview();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        CameraController.this.captureSession = session;
                        CameraController.this.captureSession.capture(captureBuilder.build(), captureListener, cameraHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, cameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void save(byte[] bytes) {
        Log.d(TAG, "Picture taken");
        Bitmap temp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        final Drawable drawable = new BitmapDrawable(activity.getResources(), temp);
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                defaultView.setBackground(drawable);
            }
        });
    }

    public Bitmap getPicture() {
        return picture;
    }

    public void reset() {
        picture = null;
    }

    private void openCamera() {
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
            cameraID = manager.getCameraIdList()[0];
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                activity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return;
            }
            manager.openCamera(cameraID, stateCallback, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createCameraPreview() {
        try {
            captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureBuilder.addTarget(cameraSurface);
            cameraDevice.createCaptureSession(Arrays.asList(cameraSurface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession cameraCaptureSession) {
                    if (cameraDevice == null) {
                        return;
                    }
                    captureSession = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updatePreview() {
        if (null == cameraDevice) {
            Log.e(TAG, "updatePreview error, return");
        }
        captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            captureSession.setRepeatingRequest(captureBuilder.build(), null, cameraHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void closeCamera() {
        Log.d(TAG, "closeCamera: Close camera");
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
    }
}