/*
    Scanbot SDK Cordova Plugin

    Copyright (c) 2017 doo GmbH. All rights reserved.
 */
package io.scanbot.sdk.plugin.cordova;

import android.graphics.Color;

public final class ScanbotConstants {

    private ScanbotConstants() {}


    public static final String EXTRAS_ARG_IMAGE_FILE_URI = "imageFileUri";
    public static final String EXTRAS_ARG_ORIGINAL_IMAGE_FILE_URI = "originalImageFileUri";
    public static final String EXTRAS_ARG_IMAGE_ORIENTATION = "imageOrientation";
    public static final String EXTRAS_ARG_DETECTED_POLYGON = "detectedPolygon";
    public static final String EXTRAS_ARG_DETECTION_RESULT = "detectionResult";
    public static final String EXTRAS_ARG_TEXT_RES_MAP = "textResourcesMap";
    public static final String EXTRAS_ARG_EDGE_COLOR = "edgeColor";
    public static final String EXTRAS_ARG_JPG_QUALITY = "jpgQuality";
    public static final String EXTRAS_ARG_SAMPLE_SIZE = "sampleSize";
    public static final String EXTRAS_ARG_AUTOSNAPPING_ENABLED = "autoSnappingEnabled";
    public static final String EXTRAS_ARG_AUTOSNAPPING_SENSITIVITY = "autoSnappingSensitivity";
    public static final String EXTRAS_ARG_ERROR_MSG = "errorMessage";
    public static final String EXTRAS_ARG_BARCODE_JSON_RESULT = "barcodeJsonResult";
    public static final String EXTRAS_ARG_FLASH_ENABLED = "flashEnabled";
    public static final String EXTRAS_ARG_BARCODE_FORMATS = "barcodeFormats";
    public static final String EXTRAS_ARG_PLAY_TONE = "playTone";
    public static final String EXTRAS_ARG_VIBRATE = "vibrate";

    public static final String ACTION_DISMISS_SB_CAMERA = ScanbotConstants.class.getPackage().toString() + ".dismissCamera";
    public static final String ACTION_DISMISS_SB_BARCODE_SCANNER = ScanbotConstants.class.getPackage().toString() + ".dismissBarcodeScanner";


    // Activity Request Codes:
    public static final int REQUEST_SB_CAMERA               = 42001; // Scanbot Camera Activity Request Code
    public static final int REQUEST_SB_EDIT_IMAGE           = 42002; // Scanbot Edit Image Activity Request Code
    public static final int REQUEST_SB_BARCODE_SCANNER      = 42003; // Scanbot Barcode Scanner Activity Request Code

    // Activity Result Codes:
    public static final int RESULT_SB_CAM_PICTURE_TAKEN     = 43001; // Scanbot Camera Activity Result Code
    public static final int RESULT_SB_EDIT_IMAGE            = 43002; // Scanbot Edit Image Activity Result Code
    public static final int RESULT_SB_EDIT_IMAGE_ERROR      = 43003; // Scanbot Edit Image Activity Error Result Code
    public static final int RESULT_SB_BARCODE_SCANNER       = 43004; // Scanbot Barcode Scanner Activity Result Code


    public static final int DEFAUL_EDGE_COLOR = Color.parseColor("#ff80cbc4");

    // Scanbot Camera default autosnapping feature enabled
    public static final boolean DEFAULT_AUTOSNAPPING_ENABLED = true;

    // Scanbot Camera default autosnapping sensivity value
    public static final double DEFAULT_AUTOSNAPPING_SENSITIVITY = 0.66;

}
