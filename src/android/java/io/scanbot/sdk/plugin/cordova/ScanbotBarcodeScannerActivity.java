/*
    Scanbot SDK Cordova Plugin

    Copyright (c) 2018 doo GmbH. All rights reserved.
 */
package io.scanbot.sdk.plugin.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.client.result.ParsedResult;
import com.google.zxing.client.result.ResultParser;

import net.doo.snap.ScanbotSDK;
import net.doo.snap.camera.BarcodeDetectorFrameHandler;
import net.doo.snap.camera.CameraOpenCallback;
import net.doo.snap.camera.ScanbotCameraView;

import java.util.ArrayList;

import io.scanbot.sdk.plugin.cordova.utils.JsonArgs;
import io.scanbot.sdk.plugin.cordova.utils.LogUtils;
import io.scanbot.sdk.plugin.cordova.utils.ResourcesUtils;

import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.ACTION_DISMISS_SB_BARCODE_SCANNER;
import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.EXTRAS_ARG_BARCODE_FORMATS;
import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.EXTRAS_ARG_BARCODE_JSON_RESULT;
import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.EXTRAS_ARG_FLASH_ENABLED;
import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.EXTRAS_ARG_PLAY_TONE;
import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.EXTRAS_ARG_VIBRATE;
import static io.scanbot.sdk.plugin.cordova.ScanbotConstants.RESULT_SB_BARCODE_SCANNER;


/**
 * Scanbot SDK Barcode Scanner Activity.
 */
public class ScanbotBarcodeScannerActivity extends AppCompatActivity implements
        BarcodeDetectorFrameHandler.ResultHandler {

    private static final String LOG_TAG = ScanbotBarcodeScannerActivity.class.getSimpleName();

    private static final long CAMERA_OPEN_DELAY_MS = 400L;

    private ScanbotCameraView cameraView;
    private ImageButton cancelButton;
    private CheckBox flashToggle;

    private ScanbotFeedbackGenerator feedbackGenerator;

    private ScanbotBarcodeScannerBroadcastReceiver broadcastReceiver;

    private boolean flashEnabled = false;
    private boolean playTone = true;
    private boolean vibrate = true;
    // BarcodeFormats to accept/to trigger on. If no barcodeFormats were defined, we accept all.
    // If only some special barcode formats were defined, we accept/trigger only those.
    private ArrayList<String> barcodeFormats;


    private View findViewById(final String id) {
        final int idInt = ResourcesUtils.getResId("id", id, this);
        return findViewById(idInt);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(ResourcesUtils.getResId("layout", "scanbot_barcode_scanner_view", this));

        getSupportActionBar().hide();

        broadcastReceiver = new ScanbotBarcodeScannerBroadcastReceiver();
        registerReceiver(broadcastReceiver, new IntentFilter(ACTION_DISMISS_SB_BARCODE_SCANNER));

        flashEnabled = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_FLASH_ENABLED) ?
                savedInstanceState.getBoolean(EXTRAS_ARG_FLASH_ENABLED) :
                getIntent().getExtras().getBoolean(EXTRAS_ARG_FLASH_ENABLED));
        playTone = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_PLAY_TONE) ?
                savedInstanceState.getBoolean(EXTRAS_ARG_PLAY_TONE) :
                getIntent().getExtras().getBoolean(EXTRAS_ARG_PLAY_TONE));
        vibrate = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_VIBRATE) ?
                savedInstanceState.getBoolean(EXTRAS_ARG_VIBRATE) :
                getIntent().getExtras().getBoolean(EXTRAS_ARG_VIBRATE));

        initBarcodeFormats(savedInstanceState);

        feedbackGenerator = new ScanbotFeedbackGenerator(this, playTone, vibrate);

        setupCamera();
        setupUIButtons();
    }

    protected void setupCamera() {
        cameraView = (ScanbotCameraView) findViewById("scanbotBarcodeScannerCameraView");
        cameraView.setCameraOpenCallback(new CameraOpenCallback() {
            @Override
            public void onCameraOpened() {
                cameraView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        cameraView.continuousFocus();
                        cameraView.useFlash(flashEnabled);
                    }
                }, CAMERA_OPEN_DELAY_MS);
            }
        });

        final BarcodeDetectorFrameHandler barcodeDetectorFrameHandler =
                BarcodeDetectorFrameHandler.attach(cameraView, new ScanbotSDK(this));
        barcodeDetectorFrameHandler.setDetectionInterval(2000); // TODO detectionInterval via arg?
        barcodeDetectorFrameHandler.addResultHandler(this);
    }

    protected void setupUIButtons() {
        cancelButton = (ImageButton) findViewById("cancelBarcodeScannerButton");
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        flashToggle = (CheckBox) findViewById("barcodeScannerFlashToggle");
        flashToggle.setChecked(flashEnabled);
        flashToggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                flashEnabled = checked;
                cameraView.useFlash(flashEnabled);
            }
        });
    }

    protected void initBarcodeFormats(final Bundle savedInstanceState) {
        final ArrayList<String> formatsArgs = (savedInstanceState != null && savedInstanceState.containsKey(EXTRAS_ARG_BARCODE_FORMATS) ?
                savedInstanceState.getStringArrayList(EXTRAS_ARG_BARCODE_FORMATS) :
                getIntent().getExtras().getStringArrayList(EXTRAS_ARG_BARCODE_FORMATS));
        barcodeFormats = new ArrayList<String>();
        if (formatsArgs != null) {
            for (final String f: formatsArgs) {
                try {
                    this.barcodeFormats.add(BarcodeFormat.valueOf(f).name());
                }
                catch (final IllegalArgumentException e) {
                    // just a warning error log here
                    errorLog("Invalid/unsupported barcode format argument value: " + f);
                }
            }
        }
    }

    @Override
    protected void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRAS_ARG_FLASH_ENABLED, flashEnabled);
        outState.putBoolean(EXTRAS_ARG_PLAY_TONE, playTone);
        outState.putBoolean(EXTRAS_ARG_VIBRATE, vibrate);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cameraView.onResume();
        cameraView.useFlash(flashEnabled);
    }

    @Override
    protected void onPause() {
        super.onPause();
        cameraView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        feedbackGenerator.release();
        if (broadcastReceiver != null) {
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    public boolean handleResult(final Result rawResult) {
        if (rawResult == null) {
            return false;
        }

        if (barcodeFormats.isEmpty() || barcodeFormats.contains(rawResult.getBarcodeFormat().name())) {
            final ScanbotBarcodeScannerResult barcodeResult = new ScanbotBarcodeScannerResult(rawResult);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    feedbackGenerator.playBeepToneAndVibrate();

                    final Bundle extras = new Bundle();
                    extras.putString(EXTRAS_ARG_BARCODE_JSON_RESULT, barcodeResult.getResultAsJSONString());
                    final Intent intent = new Intent();
                    intent.putExtras(extras);
                    setResult(RESULT_SB_BARCODE_SCANNER, intent);
                    finish();
                }
            });
        }

        return false;
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



    class ScanbotBarcodeScannerBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            if (intent.getAction().equals(ACTION_DISMISS_SB_BARCODE_SCANNER)) {
                ScanbotBarcodeScannerActivity.this.finish();
            }
        }
    }


    class ScanbotBarcodeScannerResult {
        final Result rawResult;
        final ParsedResult parsedResult;

        ScanbotBarcodeScannerResult(final Result rawResult) {
            this.rawResult = rawResult;
            this.parsedResult = ResultParser.parseResult(rawResult);
        }

        String getResultAsJSONString() {
            return new JsonArgs()
                    .put("barcodeFormat", rawResult.getBarcodeFormat().name())
                    .put("textValue", rawResult.getText())
                    //.put("rawBytes", new String(rawResult.getRawBytes())) // TODO in Android and iOS
                    //.put("parsedType", parsedResult.getType().name()) // TODO in iOS
                    // TODO add parsed fields for QR Codes, etc.
                    .jsonObj()
                    .toString();
        }
    }

}
