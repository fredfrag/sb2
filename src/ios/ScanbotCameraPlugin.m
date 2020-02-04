//
//  ScanbotCameraPlugin.m
//  Scanbot SDK Cordova Plugin
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import "ScanbotCameraPlugin.h"
#import "ScanbotCameraViewController.h"
#import "ScanbotBarcodeScannerViewController.h"

#import <Foundation/Foundation.h>

#ifdef _CORDOVA_TEST_TARGET
#import "CDVPlugin.h"
#import "CDVInvokedUrlCommand.h"
#else
#import <Cordova/CDVPlugin.h>
#endif

#import <ScanbotSDK/SBSDKScanbotSDK.h>

#import "LoggingUtils.h"
#import "HandyJSONParameters.h"

#import "ImageUtils.h"
#import "SBSDKPolygon+JSON.h"
#import "UIColor+JSON.h"

static NSString *const defauleEdgeColor = @"#ff80cbc4";

@interface ScanbotCameraPlugin () <SBSDKImageEditingViewControllerDelegate>

@property (strong, nonatomic) CDVInvokedUrlCommand *cropVCCommand;
@property (nonatomic) CGFloat croppingOutputQuality;
@property (nonatomic, weak) SBSDKImageEditingViewController *imageEditingController;

@end

@implementation ScanbotCameraPlugin

- (BOOL)checkSDKInitializationWithCommand:(CDVInvokedUrlCommand *)command {
	if (![SharedConfiguration defaultConfiguration].isSDKInitialized) {
		NSString *errorMessage = @"Scanbot SDK is not initialized. Please call the ScanbotSdk.initializeSdk() function first.";
		CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
														  messageAsString:errorMessage];
		[self.commandDelegate sendPluginResult:pluginResult
									callbackId:command.callbackId];
		return NO;
	}
	return YES;
}

- (UIViewController *)topMostController {
	UIViewController *topController = [UIApplication sharedApplication].keyWindow.rootViewController;
	while (topController.presentedViewController) {
		topController = topController.presentedViewController;
	}
	return topController;
}

- (void)startCamera:(CDVInvokedUrlCommand *)command {
	if (![self checkSDKInitializationWithCommand:command]) {
		return;
	}
	
	SBSDKLog(@"ScanbotCameraPlugin: startCamera ...");
	[self.commandDelegate runInBackground:^{
		SBSDKLog(@"Creating ScanbotCameraViewController instance ...");
		ScanbotCameraViewController *scanbotCameraViewController = [ScanbotCameraViewController new];
		scanbotCameraViewController.command = command;
		scanbotCameraViewController.commandDelegate = self.commandDelegate;
		
		HandyJSONParameters *params = [HandyJSONParameters JSONParametersWithCordovaCommand:command];
		
		NSString *const edgeColorKey = @"edgeColor";
		UIColor *edgeColor = [UIColor colorFromHexString:defauleEdgeColor];
		if (params[edgeColorKey] && ![params[edgeColorKey] isEqualToString:@""]) {
			edgeColor = [UIColor colorFromHexString:params[edgeColorKey]];
		}
		scanbotCameraViewController.strokeColor = edgeColor;
		scanbotCameraViewController.imageCompressionQuality = params.qualityValue;
		scanbotCameraViewController.autoSnappingEnabled = [params boolParameterValueForKey:@"autoSnappingEnabled"
																			  defaultValue:YES];
		scanbotCameraViewController.autoSnappingSensitivity = params.autoSnappingSensitivityValue;
		scanbotCameraViewController.sampleSize = params.sampleSizeValue;
		
		SBSDKLog(@"Starting scanbot camera....");
		dispatch_async(dispatch_get_main_queue(), ^{
			[[self topMostController] presentViewController:scanbotCameraViewController
												   animated:YES
												 completion:nil];
		});
	}];
}

- (void)dismissCamera:(CDVInvokedUrlCommand *)command {
	if (![self checkSDKInitializationWithCommand:command]) {
		return;
	}
	
	UIViewController *topmostVC = [self topMostController];
	if ([topmostVC isKindOfClass:[ScanbotCameraViewController class]]) {
		[topmostVC dismissViewControllerAnimated:YES completion:nil];
	}
}

- (void)startBarcodeScanner:(CDVInvokedUrlCommand *)command {
	if (![self checkSDKInitializationWithCommand:command]) {
		return;
	}

	SBSDKLog(@"ScanbotCameraPlugin: startBarcodeScanner ...");
	[self.commandDelegate runInBackground:^{
		SBSDKLog(@"Creating ScanbotBarcodeScannerViewController instance ...");
		ScanbotBarcodeScannerViewController *barcodeScannerViewController = [ScanbotBarcodeScannerViewController new];
		barcodeScannerViewController.command = command;
		barcodeScannerViewController.commandDelegate = self.commandDelegate;

		HandyJSONParameters *params = [HandyJSONParameters JSONParametersWithCordovaCommand:command];

		barcodeScannerViewController.flashEnabled = [params boolParameterValueForKey:@"flashEnabled"
														  			    defaultValue:YES];
		barcodeScannerViewController.playTone = [params boolParameterValueForKey:@"playTone"
														  		    defaultValue:YES];
		barcodeScannerViewController.vibrate = [params boolParameterValueForKey:@"vibrate"
														  		   defaultValue:YES];
        NSSet<NSString *> *barcodeFormats = params[@"barcodeFormats"];
        barcodeScannerViewController.barcodeFormats = (barcodeFormats != nil ? barcodeFormats : [[NSSet alloc] init]);

		SBSDKLog(@"Starting barcode scanner ...");
		dispatch_async(dispatch_get_main_queue(), ^{
			[[self topMostController] presentViewController:barcodeScannerViewController
												   animated:YES
												 completion:nil];
		});
	}];
}

- (void)dismissBarcodeScanner:(CDVInvokedUrlCommand *)command {
    if (![self checkSDKInitializationWithCommand:command]) {
        return;
    }

    UIViewController *topmostVC = [self topMostController];
    if ([topmostVC isKindOfClass:[ScanbotBarcodeScannerViewController class]]) {
        [topmostVC dismissViewControllerAnimated:YES completion:nil];
    }
}

- (void)startCropping:(CDVInvokedUrlCommand *)command {
	if (![self checkSDKInitializationWithCommand:command]) {
		return;
	}
	
	SBSDKLog(@"ScanbotCameraPlugin: startCropping ...");
	[self.commandDelegate runInBackground:^{
		HandyJSONParameters *params = [HandyJSONParameters JSONParametersWithCordovaCommand:command];
		self.croppingOutputQuality = params.qualityValue;
		
		NSString *imageFileUri = params[@"imageFileUri"];
		NSURL *inputImageURL = [NSURL URLWithString:imageFileUri];
		SBSDKLog(@"inputImageURL: %@", inputImageURL);
		
		UIImage *image = [ImageUtils loadImage:inputImageURL.absoluteString];
        if (!image) {
            NSString *errorMessage = [NSString stringWithFormat:@"File does not exist: %@", inputImageURL.absoluteString];
            CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
                                                              messageAsString:errorMessage];
            [self.commandDelegate sendPluginResult:pluginResult
                                        callbackId:command.callbackId];
            return;
        }

		SBSDKImageEditingViewController *imageEditingController = [self configureImageEditingViewControllerWithImage:image
																										   andParams:params];
		self.cropVCCommand = command;
		
		SBSDKLog(@"Starting SBSDKImageEditingViewController...");
		dispatch_async(dispatch_get_main_queue(), ^{
			UINavigationController *navigationController = [[UINavigationController alloc] initWithRootViewController:imageEditingController];
			navigationController.navigationBar.barStyle = UIBarStyleBlack;
			navigationController.navigationBar.tintColor = [UIColor whiteColor];
			[[self topMostController] presentViewController:navigationController
												   animated:YES
												 completion:^{
													 self.imageEditingController = imageEditingController;
												 }];
		});
	}];
}

- (SBSDKImageEditingViewController *)configureImageEditingViewControllerWithImage:(UIImage *)image
																		andParams:(HandyJSONParameters *)params {
	SBSDKImageEditingViewController *imageEditingController = [[SBSDKImageEditingViewController alloc] init];
	imageEditingController.image = image;
	imageEditingController.delegate = self;
	
	NSString *const edgeColorKey = @"edgeColor";
	UIColor *edgeColor = [UIColor colorFromHexString:defauleEdgeColor];
	if (params[edgeColorKey] && ![params[edgeColorKey] isEqualToString:@""]) {
		edgeColor = [UIColor colorFromHexString:params[edgeColorKey]];
	}
	imageEditingController.edgeColor = edgeColor;
	imageEditingController.magneticEdgeColor = edgeColor;
	return imageEditingController;
}

#pragma mark - SBSDKImageEditingViewController delegate

- (UIBarStyle)imageEditingViewControllerToolbarStyle:(SBSDKImageEditingViewController *)editingViewController {
	return UIBarStyleDefault;
}

- (UIColor *)imageEditingViewControllerToolbarItemTintColor:(SBSDKImageEditingViewController *)editingViewController {
	return [UIColor whiteColor];
}

- (UIColor *)imageEditingViewControllerToolbarTintColor:(SBSDKImageEditingViewController *)editingViewController {
	return  [UIColor blackColor];
}

- (void)imageEditingViewController:(SBSDKImageEditingViewController *)editingViewController
		didApplyChangesWithPolygon:(SBSDKPolygon *)polygon
					  croppedImage:(UIImage *)croppedImage {
	SBSDKLog(@"Got warpImage result");
	NSString *outputImageFilePath = [ImageUtils generateTemporaryDocumentsFilePath:@"jpg"];
	NSURL *outputImageURL = [NSURL fileURLWithPath:outputImageFilePath];
	if ([ImageUtils saveImage:outputImageFilePath image:croppedImage quality:self.croppingOutputQuality]) {
		NSDictionary *callbackResult = @{@"imageFileUri":outputImageURL.absoluteString,
										 @"polygon":[polygon polygonPoints]};
		CDVPluginResult *pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK
													  messageAsDictionary:callbackResult];
		[self.commandDelegate sendPluginResult:pluginResult
									callbackId:self.cropVCCommand.callbackId];
	} else {
		[self.commandDelegate sendPluginResult:[CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR
																 messageAsString:@"Save image failed."]
									callbackId:self.cropVCCommand.callbackId];
	}
	[[self topMostController] dismissViewControllerAnimated:YES
												 completion:nil];
}

- (void)imageEditingViewControllerDidCancelChanges:(SBSDKImageEditingViewController *)editingViewController {
	[[self topMostController] dismissViewControllerAnimated:YES completion:nil];
}

- (UIBarButtonItem *)imageEditingViewControllerApplyButtonItem:(SBSDKImageEditingViewController *)editingViewController {
	return [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ui_action_checkmark"]
											style:UIBarButtonItemStylePlain
										   target:nil
										   action:NULL];
}

- (UIBarButtonItem *)imageEditingViewControllerCancelButtonItem:(SBSDKImageEditingViewController *)editingViewController {
	return [[UIBarButtonItem alloc] initWithImage:[UIImage imageNamed:@"ui_action_close"]
											style:UIBarButtonItemStylePlain
										   target:nil
										   action:NULL];
}

- (void)rotateImage:(id)sender {
	if (self.imageEditingController) {
		[self.imageEditingController rotateInputImageClockwise:YES animated:YES];
	}
}

- (UIBarButtonItem *)imageEditingViewControllerRotateClockwiseToolbarItem:(SBSDKImageEditingViewController *)editingViewController {
	UIView *contentView = [[UIView alloc] initWithFrame:CGRectMake(0.0f, 0.0f, 50.0f, 50.0f)];
	UIImageView *imageView = [[UIImageView alloc] initWithFrame:CGRectMake(0.0f, 5.0f, contentView.frame.size.width, 34.0f)];
	imageView.contentMode = UIViewContentModeScaleAspectFit;
	imageView.image = [UIImage imageNamed:@"ui_edit_rotate"];
	UIButton *handleTapButton = [[UIButton alloc] initWithFrame:contentView.bounds];
	[handleTapButton addTarget:self action:@selector(rotateImage:) forControlEvents:UIControlEventTouchUpInside];
	
	[contentView addSubview:imageView];
	[contentView addSubview:handleTapButton];
	return [[UIBarButtonItem alloc] initWithCustomView:contentView];
}

@end

