//
//  ScanbotBarcodeScannerViewController.m
//  Scanbot SDK Cordova Plugin
//
//  Copyright (c) 2018 doo GmbH. All rights reserved.
//

#import "ScanbotBarcodeScannerViewController.h"
#import "HandyJSONParameters.h"
#import "LoggingUtils.h"

@interface ScanbotBarcodeScannerViewController ()

@property (assign, nonatomic) BOOL shouldDetectCodes;
@property (nonatomic, strong) UIButton *closeButton;
@property (nonatomic, strong) UIButton *flashButton;

@end

@implementation ScanbotBarcodeScannerViewController

- (void)viewDidLoad {
    [super viewDidLoad];

    self.scannerViewController = [[SBSDKScannerViewController alloc] initWithParentViewController:self
                                                                                       parentView:nil
                                                                                     imageStorage:nil
                                                                            enableQRCodeDetection:YES];

    SBSDKLog(@"ScanbotBarcodeScannerViewController: SBSDKScannerViewController created");
    SBSDKLog(@"ScanbotBarcodeScannerViewController: flashEnabled = %@", self.flashEnabled ? @"true" : @"false");
    SBSDKLog(@"ScanbotBarcodeScannerViewController: playTone = %@", self.playTone ? @"true" : @"false");
    SBSDKLog(@"ScanbotBarcodeScannerViewController: vibrate = %@", self.vibrate ? @"true" : @"false");
    SBSDKLog(@"ScanbotBarcodeScannerViewController: barcodeFormats = %@", self.barcodeFormats);

    self.scannerViewController.delegate = self;
    self.scannerViewController.shutterButtonHidden = YES;
    self.scannerViewController.detectionStatusHidden = YES;

    self.scannerViewController.cameraSession.torchLightEnabled = self.flashEnabled;
}

- (void)viewWillAppear:(BOOL)animated {
    [super viewWillAppear:animated];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(updateFlashButtonStatus)
                                                 name:UIApplicationWillEnterForegroundNotification
                                               object:nil];
}

- (void)viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    self.shouldDetectCodes = NO;
    [[NSNotificationCenter defaultCenter] removeObserver:self
                                                    name:UIApplicationWillEnterForegroundNotification
                                                  object:nil];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    self.shouldDetectCodes = YES;
}

- (void)viewDidLayoutSubviews {
	[self placeCloseButton];
	[self placeFlashButton];
}

- (BOOL)shouldAutorotate {
    return NO;
}

- (UIInterfaceOrientationMask)supportedInterfaceOrientations {
    return UIInterfaceOrientationMaskPortrait;
}

- (UIStatusBarStyle)preferredStatusBarStyle {
    return UIStatusBarStyleLightContent;
}

- (void)placeCloseButton {
    CGRect buttonFrame = CGRectMake(30, 40, 20, 20);
    if (!self.closeButton) {
        self.closeButton = [[UIButton alloc] initWithFrame:buttonFrame];
        [self.closeButton setImage:[UIImage imageNamed:@"ui_action_close"]
                          forState:UIControlStateNormal];
        [self.closeButton addTarget:self
                             action:@selector(closeButtonTapped:)
                   forControlEvents:UIControlEventTouchUpInside];
    } else {
        [self.closeButton setFrame:buttonFrame];
    }
    [self.view addSubview:self.closeButton];
    [self.view bringSubviewToFront:self.closeButton];
}

- (void)placeFlashButton {
    CGSize screenSize = [UIScreen mainScreen].bounds.size;
    CGRect buttonFrame = CGRectMake(screenSize.width - 80, screenSize.height - 80, 40, 40);
    if (!self.flashButton) {
        self.flashButton = [[UIButton alloc] initWithFrame:buttonFrame];
        [self.flashButton setImage:[UIImage imageNamed:@"ui_flash_on"]
                          forState:UIControlStateSelected];
        [self.flashButton setImage:[UIImage imageNamed:@"ui_flash_off"]
                          forState:UIControlStateNormal];
        [self.flashButton addTarget:self
                             action:@selector(flashButtonTapped:)
                   forControlEvents:UIControlEventTouchUpInside];
        [self.flashButton setSelected:self.scannerViewController.cameraSession.isTorchLightEnabled];
    } else {
        [self.flashButton setFrame:buttonFrame];
    }
    [self.view addSubview:self.flashButton];
    [self.view bringSubviewToFront:self.flashButton];
}

- (void)closeButtonTapped:(id)sender {
    [self dismissViewControllerAnimated:YES completion:nil];
}

- (void)flashButtonTapped:(id)sender {
    self.scannerViewController.cameraSession.torchLightEnabled = !self.scannerViewController.cameraSession.isTorchLightEnabled;
    [self.flashButton setSelected:self.scannerViewController.cameraSession.isTorchLightEnabled];
}

- (void)updateFlashButtonStatus {
    if (self.flashButton) {
        [self.flashButton setSelected:self.scannerViewController.cameraSession.isTorchLightEnabled];
    }
}



#pragma mark - SBSDKScannerViewControllerDelegate

- (BOOL)scannerControllerShouldAnalyseVideoFrame:(SBSDKScannerViewController *)controller {
    return NO;
}

- (BOOL)scannerControllerShouldDetectMachineReadableCodes:(SBSDKScannerViewController *)controller {
    return self.shouldDetectCodes;
}

- (void)scannerController:(SBSDKScannerViewController *)controller
didDetectMachineReadableCodes:(NSArray<SBSDKMachineReadableCodeMetadata *> *)codes {
    [codes enumerateObjectsUsingBlock:^(SBSDKMachineReadableCodeMetadata * _Nonnull metadata,
                                        NSUInteger index,
                                        BOOL * _Nonnull stop) {
        NSString *detectedBarcodeFormat = [self barcodeFormatStringFromAVMetadataObjectType:metadata.codeObject.type];

        if ([self.barcodeFormats count] == 0 || [self.barcodeFormats containsObject:detectedBarcodeFormat]) {
            SBSDKLog(@"Detected barcode format %@", detectedBarcodeFormat);
            [self playBeepToneAndVibrate];

            // TODO parse and return details (like parsed type, QR code details, etc)
            /*SBSDKMachineReadableCode *parsedCode = [SBSDKMachineReadableCodeManager.defaultManager
                                                    parseCodeFromMetadata:metadata]; */
            [self dismissViewControllerAnimated:YES completion:^{
                CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
                                                              messageAsDictionary:@{@"barcodeFormat":detectedBarcodeFormat,
                                                                                    @"textValue":metadata.codeObject.stringValue,
                                                                                    //@"parsedType":parsedCode.type // TODO
                                                                                    }];
                [self.commandDelegate sendPluginResult:pluginResult
                                            callbackId:self.command.callbackId];
            }];
        }
    }];
}

- (BOOL)scannerController:(SBSDKScannerViewController *)controller
shouldRotateInterfaceForDeviceOrientation:(UIDeviceOrientation)orientation
                transform:(CGAffineTransform)transform {
    return NO;
}


- (NSString *)barcodeFormatStringFromAVMetadataObjectType:(AVMetadataObjectType)metadataObjectType {
    if ([metadataObjectType isEqualToString:@"org.iso.Aztec"])              return @"AZTEC";
    if ([metadataObjectType isEqualToString:@"org.iso.Code128"])            return @"CODE_128";
    if ([metadataObjectType isEqualToString:@"org.iso.Code39"])             return @"CODE_39";
    if ([metadataObjectType isEqualToString:@"org.iso.Code39Mod43"])        return @"CODE_39";
    if ([metadataObjectType isEqualToString:@"com.intermec.Code93"])        return @"CODE_93";
    if ([metadataObjectType isEqualToString:@"org.iso.DataMatrix"])         return @"DATA_MATRIX";
    if ([metadataObjectType isEqualToString:@"org.gs1.EAN-13"])             return @"EAN_13";
    if ([metadataObjectType isEqualToString:@"org.gs1.EAN-8"])              return @"EAN_8";
    if ([metadataObjectType isEqualToString:@"org.ansi.Interleaved2of5"])   return @"ITF";
    if ([metadataObjectType isEqualToString:@"org.gs1.ITF14"])              return @"ITF";
    if ([metadataObjectType isEqualToString:@"org.iso.PDF417"])             return @"PDF_417";
    if ([metadataObjectType isEqualToString:@"org.iso.QRCode"])             return @"QR_CODE";
    if ([metadataObjectType isEqualToString:@"org.gs1.UPC-E"])              return @"UPC_E";

    SBSDKLog(@"Unsupported barcode/qr-code format = %@", metadataObjectType);
    return metadataObjectType;
}

- (void)playBeepToneAndVibrate {
    if (self.playTone) {
        AudioServicesPlaySystemSound(1052);
    }
    if (self.vibrate) {
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate);
    }
}


@end
