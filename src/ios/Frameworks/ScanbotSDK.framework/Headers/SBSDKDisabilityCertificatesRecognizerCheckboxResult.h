//
//  SBSDKDisabilityCertificateRecognizerCheckboxResult.h
//  ScanbotSDKBeta
//
//  Created by Andrew Petrus on 14.11.17.
//  Copyright © 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, SBSDKDisabilityCertificateRecognizerCheckboxType) {
    SBSDKDisabilityCertificateRecognizerCheckboxTypeInitialCertificate,
    SBSDKDisabilityCertificateRecognizerCheckboxTypeRenewedCertificate,
    SBSDKDisabilityCertificateRecognizerCheckboxTypeWorkAccident,
    SBSDKDisabilityCertificateRecognizerCheckboxTypeAssignedToAccidentInsuranceDoctor,
    SBSDKDisabilityCertificateRecognizerCheckboxTypeUndefined
};

/**
 * @class SBSDKDisabilityCertificatesRecognizerCheckboxResult
 * Contains information about recognized DC checkbox
 */
@interface SBSDKDisabilityCertificatesRecognizerCheckboxResult : NSObject

/** Checkbox type */
@property (nonatomic) SBSDKDisabilityCertificateRecognizerCheckboxType type;

/** Is checkbox checked */
@property (nonatomic) BOOL isChecked;

/** Checked confidence */
@property (nonatomic) double confidenceValue;

@end
