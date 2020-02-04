//
//  ImageUtils.h
//  Scanbot SDK Cordova Plugin
//
//  Copyright (c) 2017 doo GmbH. All rights reserved.
//

#import <ScanbotSDK/ScanbotSDK.h>

static NSString *const sbsdkTmpFolder = @"sbsdk-temp";

@interface ImageUtils : NSObject

+ (NSString *)generateTemporaryFileName:(NSString *)extension;
+ (NSString *)generateTemporaryDocumentsFilePath:(NSString*)extension;
+ (UIImage *)loadImage:(NSString *)imageFilePath;
+ (BOOL)saveImage:(NSString *)imageFilePath image:(UIImage *)image quality:(CGFloat)quality;
+ (void)recreateTempDirectoryIfNeeded;
+ (NSError *)removeAllFilesFromTemporaryDocumentsDirectory;
+ (NSString *)tempDirectoryPath;
+ (BOOL)imageFileExists:(NSString *)imageFileUri;

@end
