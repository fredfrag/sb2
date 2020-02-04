/*
    Scanbot SDK Cordova Plugin

    Copyright (c) 2017 doo GmbH. All rights reserved.
 */
package io.scanbot.sdk.plugin.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.*;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.*;

import io.scanbot.sdk.plugin.cordova.utils.ImageUtils;
import io.scanbot.sdk.plugin.cordova.utils.LogUtils;
import io.scanbot.sdk.plugin.cordova.utils.ResourcesUtils;
import io.scanbot.sdk.plugin.cordova.widget.ShutterDrawable;

import net.doo.snap.camera.AutoSnappingController;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ContourDetectorFrameHandler;
import net.doo.snap.camera.PictureCallback;
import net.doo.snap.camera.ScanbotCameraView;
import net.doo.snap.lib.detector.DetectionResult;
import net.doo.snap.ui.PolygonView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


/**
 * Scanbot SDK Camera Activity for Document Scanning UI with user guidance.
 */
public class ScanbotCameraActivity extends AppCompatActivity implements PictureCallback,
        ContourDetectorFrameHandler.ResultHandler {

    private static final String LOG_TAG = ScanbotCameraActivity.class.getSimpleName();

    private static final float SCALE_DEFAULT = 1f;
    private static final float TAKE_PICTURE_PRESSED_SCALE = 0.8f;
    private static final float TAKE_PICTURE_OVERSHOOT_TENSION = 8f;

    private static final long CAMERA_OPEN_DELAY_MS = 400L;

    private final Executor executor = Executors.newSingleThreadExecutor();

    private ScanbotSdkWrapper sdkWrapper;

    private int screenOrientation;

    private ImageButton cancelButton;

    private ScanbotCameraView cameraView;
    private ContourDetectorFrameHandler contourDetectorFrameHandler;
    private PolygonView polygonView;
    private AutoSnappingController autoSnappingController;
    private TextView userGuidanceHint;
    private long lastUserGuidanceHintTs = 0L;
    private ImageButton snapImageButton;
    private ShutterDrawable shutterDrawable;
    private CheckBox flashToggle;
    private CheckBox autosnapToggle;

    private ProgressBar processPictureProgressBar;

    // Optional text resources bundle, passed as a simple string map from the client/app.
    private Bundle textResBundle;

    private int edgeColor = DEFAUL_EDGE_COLOR;

    private int jpgQuality = ImageUtils.JPEG_QUALITY;

    private int sampleSize = 1; // 1 means original size (no downscale)

    private boolean autoSnappingEnabled = true;
    
    private double autoSnappingSensitivity = DEFAULT_AUTOSNAPPING_SENSITIVITY;

    private ScanbotCameraBroadcastReceiver broadcastReceiver;


    private View findViewById(final String id) {
        final int idInt = ResourcesUtils.getResId("id", id, this);
        return findViewById(idInt);
    }

    protected void setupUIButtons() {
        cancelButton = (ImageButton) findViewById("cancelCameraButton");
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        snapImageButton = (ImageButton) findViewById("snapImageButton");
        snapImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.takePicture(false);
            }
        });
        snapImageButton.setOnTouchListener(new View.OnTouchListener() {

            private final Interpolator downInterpolator = new DecelerateInterpolator();
            private final Interpolator upInterpolator = new OvershootInterpolator(TAKE_PICTURE_OVERSHOOT_TENSION);

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        snapImageButton.animate()
                                .scaleX(TAKE_PICTURE_PRESSED_SCALE)
                                .scaleY(TAKE_PICTURE_PRESSED_SCALE)
                                .setInterpolator(downInterpolator)
                                .start();
                        break;
                    case MotionEvent.ACTION_CANCEL:
                    case MotionEvent.ACTION_UP:
                        snapImageButton.animate()
                                .scaleX(SCALE_DEFAULT)
                                .scaleY(SCALE_DEFAULT)
                                .setInterpolator(upInterpolator)
                                .start();
                        break;
                }

                return false;
            }
        });

        shutterDrawable = new ShutterDrawable(this);
        snapImageButton.setImageDrawable(shutterDrawable);

        flashToggle = (CheckBox) findViewById("flashToggle");
        flashToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                setFlashEnabled(checked);
            }
        });

        autosnapToggle = (CheckBox) findViewById("autosnapToggle");
        autosnapToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                autoSnappingEnabled = checked;
                setAutoSnapEnabled(autoSnappingEnabled);
            }
        });
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(ResourcesUtils.getResId("layout", "scanbot_camera_view", this));

        broadcastReceiver = new ScanbotCameraBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_DISMISS_SB_CAMERA));

        sdkWrapper = new ScanbotSdkWrapper(this);

        getSupportActionBar().hide();

        textResBundle = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_TEXT_RES_MAP) ?
                savedInstanceState.getBundle(EXTRAS_ARG_TEXT_RES_MAP) :
                getIntent().getExtras().getBundle(EXTRAS_ARG_TEXT_RES_MAP));

        edgeColor = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_EDGE_COLOR) ?
                savedInstanceState.getInt(EXTRAS_ARG_EDGE_COLOR) :
                getIntent().getExtras().getInt(EXTRAS_ARG_EDGE_COLOR));

        jpgQuality = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_JPG_QUALITY) ?
                savedInstanceState.getInt(EXTRAS_ARG_JPG_QUALITY) :
                getIntent().getExtras().getInt(EXTRAS_ARG_JPG_QUALITY));

        autoSnappingEnabled = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_AUTOSNAPPING_ENABLED) ?
                savedInstanceState.getBoolean(EXTRAS_ARG_AUTOSNAPPING_ENABLED) :
                getIntent().getExtras().getBoolean(EXTRAS_ARG_AUTOSNAPPING_ENABLED));

        autoSnappingSensitivity = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_AUTOSNAPPING_SENSITIVITY) ?
                savedInstanceState.getDouble(EXTRAS_ARG_AUTOSNAPPING_SENSITIVITY) :
                getIntent().getExtras().getDouble(EXTRAS_ARG_AUTOSNAPPING_SENSITIVITY));

        sampleSize = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_SAMPLE_SIZE) ?
                savedInstanceState.getInt(EXTRAS_ARG_SAMPLE_SIZE) :
                getIntent().getExtras().getInt(EXTRAS_ARG_SAMPLE_SIZE));

        setupUIButtons();

        cameraView = (ScanbotCameraView) findViewById("scanbotCameraView");
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.continuousFocus();
                    }
                }, CAMERA_OPEN_DELAY_MS);
            }
        });

        contourDetectorFrameHandler = ContourDetectorFrameHandler.attach(cameraView);
        contourDetectorFrameHandler.setAcceptedAngleScore(65);
        contourDetectorFrameHandler.setAcceptedSizeScore(75);

        polygonView = (PolygonView) findViewById("scanbotPolygonView");
        polygonView.setStrokeColor(edgeColor);
        polygonView.setStrokeColorOK(edgeColor);
        polygonView.setStrokeWidth(7.0f);

        contourDetectorFrameHandler.addResultHandler(polygonView);
        contourDetectorFrameHandler.addResultHandler(this);

        autoSnappingController = AutoSnappingController.attach(cameraView, contourDetectorFrameHandler);
        autoSnappingController.setSensitivity((float) autoSnappingSensitivity);

        cameraView.addPictureCallback(this);

        userGuidanceHint = (TextView) findViewById("userGuidanceHint");

        screenOrientation = getResources().getConfiguration().orientation;

        processPictureProgressBar = (ProgressBar) findViewById("processPictureProgressBar");

        setAutoSnapEnabled(autoSnappingEnabled);
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        if (textResBundle != null) {
            outState.putBundle(EXTRAS_ARG_TEXT_RES_MAP, textResBundle);
        }
        outState.putInt(EXTRAS_ARG_EDGE_COLOR, edgeColor);
        outState.putInt(EXTRAS_ARG_JPG_QUALITY, jpgQuality);
        outState.putInt(EXTRAS_ARG_SAMPLE_SIZE, sampleSize);
        outState.putBoolean(EXTRAS_ARG_AUTOSNAPPING_ENABLED, autoSnappingEnabled);
        outState.putDouble(EXTRAS_ARG_AUTOSNAPPING_SENSITIVITY, autoSnappingSensitivity);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public void onPictureTaken(byte[] image, int imageOrientation) {
        debugLog("Picture was taken. imageOrientation = " + imageOrientation);

        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                lockScreenOrientation();
                processPictureProgressBar.setVisibility(View.VISIBLE);
                userGuidanceHint.setText("");
                userGuidanceHint.setVisibility(View.GONE);
                flashToggle.setVisibility(View.GONE);
                shutterDrawable.setActive(false);
                snapImageButton.setVisibility(View.GONE);
                cancelButton.setVisibility(View.GONE);
                autosnapToggle.setVisibility(View.GONE);
                polygonView.setVisibility(View.GONE);
            }
        });

        new ProcessTakenPictureTask().executeOnExecutor(executor, image, imageOrientation);
    }

    private void lockScreenOrientation() {
        if (screenOrientation == Configuration.ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    @Override
    public boolean handleResult(final ContourDetectorFrameHandler.DetectedFrame result) {
        userGuidanceHint.post(new Runnable() {
            @Override
            public void run() {
                showUserGuidance(result.detectionResult);
            }
        });

        return false;
    }

    private void showUserGuidance(final DetectionResult result) {
        if (!autoSnappingEnabled) {
            return;
        }

        if (System.currentTimeMillis() - lastUserGuidanceHintTs < 400) {
            return;
        }

        shutterDrawable.setActive(false);
        userGuidanceHint.setText("");
        userGuidanceHint.setVisibility(View.VISIBLE);

        switch (result) {
            case OK:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_do_not_move", "Don't move"));
                shutterDrawable.setActive(true);
                break;
            case OK_BUT_BAD_ASPECT_RATIO:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_bad_aspect_ratio", "Wrong aspect ratio.\nRotate your device."));
                break;
            case OK_BUT_TOO_SMALL:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_move_closer", "Move closer"));
                break;
            case OK_BUT_BAD_ANGLES:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_bad_angles", "Perspective"));
                break;
            case ERROR_NOTHING_DETECTED:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_nothing_detected", "No Document"));
                break;
            case ERROR_TOO_NOISY:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_too_noisy", "Background too noisy"));
                break;
            case ERROR_TOO_DARK:
                userGuidanceHint.setText(getTextResValue("autosnapping_hint_too_dark", "Poor light"));
                break;
            default:
                userGuidanceHint.setVisibility(View.GONE);
                break;
        }

        lastUserGuidanceHintTs = System.currentTimeMillis();
    }

    private String getTextResValue(final String key, final String defaultValue) {
        return (textResBundle != null && textResBundle.containsKey(key) ?
                textResBundle.getString(key) : defaultValue);
    }

    private void setAutoSnapEnabled(boolean enabled) {
        autoSnappingController.setEnabled(enabled);
        contourDetectorFrameHandler.setEnabled(enabled);

        int image_resid = ResourcesUtils.getResId("drawable", "ui_scan_automatic_active", this);
        if (enabled) {
            shutterDrawable.startAnimation();
            polygonView.setVisibility(View.VISIBLE);
        } else {
            shutterDrawable.stopAnimation();
            polygonView.setVisibility(View.GONE);
            image_resid = ResourcesUtils.getResId("drawable", "ui_scan_automatic", this);
            userGuidanceHint.setVisibility(View.GONE);
        }
        autosnapToggle.setBackgroundResource(image_resid);
    }

    protected void setFlashEnabled(final boolean enabled) {
        cameraView.useFlash(enabled);
    }


    private void debugLog(final String msg) {
        LogUtils.debugLog(LOG_TAG, msg);
    }

    private void errorLog(final String msg) {
        LogUtils.errorLog(LOG_TAG, msg);
    }

    private void errorLog(final String msg, final Throwable e) {
        LogUtils.errorLog(LOG_TAG, msg, e);
    }


    class ProcessTakenPictureTask extends AsyncTask<Object, Void, ProcessTakenPictureResult> {

        @Override
        protected ProcessTakenPictureResult doInBackground(Object... params) {
            final byte[] image = (byte[]) params[0];
            final int imageOrientation = (Integer) params[1];
            final int quality = jpgQuality;
            final int inSampleSize = sampleSize;

            DetectionResult sdkDetectionResult = DetectionResult.ERROR_NOTHING_DETECTED;
            List<PointF> polygonF = Collections.emptyList();
            Uri originalImgUri = null, documentImgUri = null;
            try {
                // decode original image:
                final BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = inSampleSize;
                Bitmap originalBitmap = BitmapFactory.decodeByteArray(image, 0, image.length, options);

                // rotate original image if required:
                if (imageOrientation > 0) {
                    debugLog("Rotating original picture ...");
                    final Matrix matrix = new Matrix();
                    matrix.setRotate(imageOrientation, originalBitmap.getWidth() / 2f, originalBitmap.getHeight() / 2f);
                    originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, false);
                }

                // store original image:
                originalImgUri = sdkWrapper.storeImage(originalBitmap, quality);
                debugLog("Original picture was stored as file: " + originalImgUri);

                debugLog("Processing document detection ...");
                final ScanbotSdkWrapper.DocumentDetectionResult docDetectionResult = sdkWrapper.documentDetection(originalBitmap, true);
                sdkDetectionResult = docDetectionResult.sdkDetectionResult;
                polygonF = docDetectionResult.polygon;

                if (docDetectionResult.documentImage != null) {
                    documentImgUri = sdkWrapper.storeImage(docDetectionResult.documentImage, quality);
                    debugLog("Detected and cropped document picture was stored as file: " + documentImgUri);
                    docDetectionResult.documentImage.recycle();
                }
            }
            catch(final Exception e) {
                errorLog("Could not process image: " + e.getMessage(), e);
            }

            return new ProcessTakenPictureResult(originalImgUri, imageOrientation,
                    documentImgUri, sdkDetectionResult, polygonF);
        }

        @Override
        protected void onPostExecute(final ProcessTakenPictureResult result) {
            if (!isCancelled()) {
                final Bundle extras = new Bundle();
                extras.putString(EXTRAS_ARG_ORIGINAL_IMAGE_FILE_URI, result.originalImgUri.toString());
                extras.putInt(EXTRAS_ARG_IMAGE_ORIENTATION, result.imageOrientation);
                extras.putString(EXTRAS_ARG_IMAGE_FILE_URI, result.documentImgUri.toString());
                extras.putParcelableArrayList(EXTRAS_ARG_DETECTED_POLYGON, new ArrayList<PointF>(result.polygonF));
                extras.putString(EXTRAS_ARG_DETECTION_RESULT, sdkWrapper.sdkDocDetectionResultToJsString(result.sdkDetectionResult));
                final Intent intent = new Intent();
                intent.putExtras(extras);
                setResult(RESULT_SB_CAM_PICTURE_TAKEN, intent);
                finish();
            }
        }
    }


    class ProcessTakenPictureResult {
        final Uri originalImgUri;
        final int imageOrientation;
        final Uri documentImgUri;
        final DetectionResult sdkDetectionResult;
        final List<PointF> polygonF;

        ProcessTakenPictureResult(final Uri originalImgUri,
                                  final int imageOrientation,
                                  final Uri documentImgUri,
                                  final DetectionResult sdkDetectionResult,
                                  final List<PointF> polygonF) {
            this.originalImgUri = originalImgUri;
            this.imageOrientation = imageOrientation;
            this.documentImgUri = documentImgUri;
            this.sdkDetectionResult = sdkDetectionResult;
            this.polygonF = polygonF;
        }
    }


    class ScanbotCameraBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_DISMISS_SB_CAMERA)) {
                ScanbotCameraActivity.this.finish();
            }
        }
    }

}
