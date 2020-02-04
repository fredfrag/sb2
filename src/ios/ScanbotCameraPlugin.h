//
//  ScanbotCameraPlugin.h
//  Scanbot SDK Cordova Plugin
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#ifdef _CORDOVA_TEST_TARGET
#import "CDVPlugin.h"
#else
#import <Cordova/CDVPlugin.h>
#endif

@interface ScanbotCameraPlugin : CDVPlugin

- (void)startCamera:(CDVInvokedUrlCommand *)command;
- (void)dismissCamera:(CDVInvokedUrlCommand *)command;

- (void)startCropping:(CDVInvokedUrlCommand *)command;

- (void)startBarcodeScanner:(CDVInvokedUrlCommand *)command;

@end
