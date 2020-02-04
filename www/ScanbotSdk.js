/*
  Scanbot SDK Cordova Plugin

  Copyright (c) 2017 doo GmbH. All rights reserved.
 */

function _cordova_exec(actionName, successCallback, errorCallback, options) {
  cordova.exec(successCallback, errorCallback, "ScanbotSdk", actionName, (options ? [options] : []));
}

module.exports = {

  // ------------------------------------------------
  // Scanbot SDK functions:

  initializeSdk: function(successCallback, errorCallback, options) {
    _cordova_exec("initializeSdk", successCallback, errorCallback, options);
  },

  isLicenseValid: function(successCallback, errorCallback) {
    _cordova_exec("isLicenseValid", successCallback, errorCallback);
  },

  documentDetection: function(successCallback, errorCallback, options) {
    _cordova_exec("documentDetection", successCallback, errorCallback, options);
  },

  applyImageFilter: function(successCallback, errorCallback, options) {
    _cordova_exec("applyImageFilter", successCallback, errorCallback, options);
  },
    
  rotateImage: function(successCallback, errorCallback, options) {
    _cordova_exec("rotateImage", successCallback, errorCallback, options);
  },

  createPdf: function(successCallback, errorCallback, options) {
    _cordova_exec("createPdf", successCallback, errorCallback, options);
  },

  performOcr: function(successCallback, errorCallback, options) {
    _cordova_exec("performOcr", successCallback, errorCallback, options);
  },

  getOcrConfigs: function(successCallback, errorCallback, options) {
    _cordova_exec("getOcrConfigs", successCallback, errorCallback, options);
  },

  cleanup: function(successCallback, errorCallback, options) {
    _cordova_exec("cleanup", successCallback, errorCallback, options);
  },

  // ------------------------------------------------


  // ------------------------------------------------
  // Scanbot SDK constants:

  ImageFilter: {
    NONE: "NONE",
    COLOR_ENHANCED: "COLOR_ENHANCED",
    GRAYSCALE: "GRAYSCALE",
    BINARIZED: "BINARIZED",
    COLOR_DOCUMENT: "COLOR_DOCUMENT"
  },

  DetectionResult: {
    OK: "OK",
    OK_BUT_BAD_ANGLES: "OK_BUT_BAD_ANGLES",
    OK_BUT_BAD_ASPECT_RATIO: "OK_BUT_BAD_ASPECT_RATIO",
    OK_BUT_TOO_SMALL: "OK_BUT_TOO_SMALL",
    ERROR_TOO_DARK: "ERROR_TOO_DARK",
    ERROR_TOO_NOISY: "ERROR_TOO_NOISY",
    ERROR_NOTHING_DETECTED: "ERROR_NOTHING_DETECTED"
  },
  
  OcrOutputFormat: {
    PDF_FILE: "PDF_FILE",
    PLAIN_TEXT: "PLAIN_TEXT",
    FULL_OCR_RESULT: "FULL_OCR_RESULT"
  },

  BarcodeFormat: {
    /** Aztec 2D barcode format. */
    AZTEC: "AZTEC",

    /** CODABAR 1D format. */
    CODABAR: "CODABAR",

    /** Code 39 1D format. */
    CODE_39: "CODE_39",

    /** Code 93 1D format. */
    CODE_93: "CODE_93",

    /** Code 128 1D format. */
    CODE_128: "CODE_128",

    /** Data Matrix 2D barcode format. */
    DATA_MATRIX: "DATA_MATRIX",

    /** EAN-8 1D format. */
    EAN_8: "EAN_8",

    /** EAN-13 1D format. */
    EAN_13: "EAN_13",

    /** ITF (Interleaved Two of Five) 1D format. */
    ITF: "ITF",

    /** MaxiCode 2D barcode format. */
    MAXICODE: "MAXICODE",

    /** PDF417 format. */
    PDF_417: "PDF_417",

    /** QR Code 2D barcode format. */
    QR_CODE: "QR_CODE",

    /** RSS 14 */
    RSS_14: "RSS_14",

    /** RSS EXPANDED */
    RSS_EXPANDED: "RSS_EXPANDED",

    /** UPC-A 1D format. */
    UPC_A: "UPC_A",

    /** UPC-E 1D format. */
    UPC_E: "UPC_E",

    /** UPC/EAN extension format. Not a stand-alone format. */
    UPC_EAN_EXTENSION: "UPC_EAN_EXTENSION"
  }
  // ------------------------------------------------

};
