//
//  SBSDKDisabilityCertificatesRecognizerDateResult.h
//  ScanbotSDKBeta
//
//  Created by Andrew Petrus on 15.11.17.
//  Copyright © 2017 doo GmbH. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, SBSDKDisabilityCertificatesRecognizerDateResultType) {
	SBSDKDisabilityCertificateRecognizerDateResultTypeIncapableOfWorkSince,
	SBSDKDisabilityCertificateRecognizerDateResultTypeIncapableOfWorkUntil,
	SBSDKDisabilityCertificateRecognizerDateResultTypeDiagnosedOn,
	SBSDKDisabilityCertificateRecognizerDateResultTypeUndefined
};

/**
 * @class SBSDKDisabilityCertificatesRecognizerDateResult
 * Class contains date information retrieved by disability certificates recognizer.
 */
@interface SBSDKDisabilityCertificatesRecognizerDateResult : NSObject

/** String representation of recognized and validated date */
@property (nonatomic, strong) NSString *dateString;

/** Date record type */
@property (nonatomic) SBSDKDisabilityCertificatesRecognizerDateResultType dateRecordType;

/** Character recognition confidence */
@property (nonatomic) double recognitionConfidence;

/** Date validation confidence */
@property (nonatomic) double validationConfidence;

@end
