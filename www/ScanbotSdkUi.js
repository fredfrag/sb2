/*
  Scanbot SDK Cordova Plugin

  Copyright (c) 2017 doo GmbH. All rights reserved.
 */

function _cordova_exec(actionName, successCallback, errorCallback, options) {
  cordova.exec(successCallback, errorCallback, "ScanbotSdkUi", actionName, (options ? [options] : []));
}

module.exports = {

  // Scanbot SDK UI functions:

  startCamera: function(successCallback, errorCallback, options) {
    _cordova_exec("startCamera", successCallback, errorCallback, options);
  },

  dismissCamera: function() {
    _cordova_exec("dismissCamera");
  },

  startCropping: function(successCallback, errorCallback, options) {
    _cordova_exec("startCropping", successCallback, errorCallback, options);
  },

  startBarcodeScanner: function(successCallback, errorCallback, options) {
    _cordova_exec("startBarcodeScanner", successCallback, errorCallback, options);
  },

  dismissBarcodeScanner: function() {
    _cordova_exec("dismissBarcodeScanner");
  }

};
