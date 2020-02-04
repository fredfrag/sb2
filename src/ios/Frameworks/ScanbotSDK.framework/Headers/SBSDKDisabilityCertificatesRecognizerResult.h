//
//  SBSDKDisabilityCertificatesRecognizerResult.h
//  ScanbotSDKBeta
//
//  Created by Andrew Petrus on 14.11.17.
//  Copyright © 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "SBSDKDisabilityCertificatesRecognizerCheckboxResult.h"
#import "SBSDKDisabilityCertificatesRecognizerDateResult.h"

/**
 * @class SBSDKDisabilityCertificatesRecognizerResult
 * Class contains disability certificates recognizer retrieved information.
 */
@interface SBSDKDisabilityCertificatesRecognizerResult : NSObject

/** Array of found checkboxes */
@property (nonatomic, strong) NSArray<SBSDKDisabilityCertificatesRecognizerCheckboxResult *> *checkboxes;

/** Array of found dates */
@property (nonatomic, strong) NSArray<SBSDKDisabilityCertificatesRecognizerDateResult *> *dates;

/** Defines whether recognition was successful */
@property (nonatomic) BOOL recognitionSuccessful;

/** Human readable string representation of contained information */
- (NSString *)stringRepresentation;

@end
