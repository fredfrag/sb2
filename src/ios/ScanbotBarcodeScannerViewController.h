//
//  ScanbotBarcodeScannerViewController.h
//  Scanbot SDK Cordova Plugin
//
//  Copyright (c) 2018 doo GmbH. All rights reserved.
//

#import <ScanbotSDK/SBSDKScanbotSDK.h>
#import <AudioToolbox/AudioServices.h>

#ifdef _CORDOVA_TEST_TARGET
#import "CDVPlugin.h"
#import "CDVInvokedUrlCommand.h"
#else
#import <Cordova/CDVPlugin.h>
#endif

@interface ScanbotBarcodeScannerViewController : UIViewController
@end

@interface ScanbotBarcodeScannerViewController() <SBSDKScannerViewControllerDelegate>

@property (strong, nonatomic) SBSDKScannerViewController *scannerViewController;
@property (strong, nonatomic) CDVInvokedUrlCommand *command;
@property (weak, nonatomic) id<CDVCommandDelegate> commandDelegate;

@property (nonatomic) BOOL flashEnabled;
@property (nonatomic) BOOL playTone;
@property (nonatomic) BOOL vibrate;
@property (strong, nonatomic, nonnull) NSSet<NSString *> *barcodeFormats;

@end
