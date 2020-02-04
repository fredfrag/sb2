//
//  SBSDKMachineReadableZoneRecognizerTextLine.h
//  ScanbotSDKBeta
//
//  Created by Andrew Petrus on 13.12.17.
//  Copyright © 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

/**
 * @class SBSDKMachineReadableZoneRecognizerTextLine
 * Incapsulates machine readable zones single text line recognition information
 */
@interface SBSDKMachineReadableZoneRecognizerTextLine : NSObject

@property (nonatomic, strong) NSString *recognizedText;
@property (nonatomic) double confidenceValue;

@end
