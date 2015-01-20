//
//  CraftARViewController.h
//  HelloCordova
//
//  Created by Daniel Cabrera on 17/07/14.
//
//

#import <UIKit/UIKit.h>
#import <Cordova/CDVViewController.h>
#import <CraftARSDK/CraftARSDK.h>
#import "NSString+UrlDecode.h"

@interface CraftARViewController : UIViewController<UIWebViewDelegate>

@property (weak, nonatomic) IBOutlet UIView *videoPreviewView;
@property (strong, nonatomic) CDVViewController *cwv;
@property (weak, nonatomic) NSString *overlay;

- (void) startFinding;
- (void) stopFinding;
- (BOOL) startTracking;
- (BOOL) stopTracking;
- (void) restartCamera;
- (BOOL) takePictureAndSearch;
- (void) setToken: (NSString *) tkn;
- (void) didGetSearchResultsIR:(NSArray *)results;
- (void) didGetSearchResultsAR:(NSArray *)results;
- (NSDictionary *) generateDictionaryJson:(CraftARItem *) item;

@end
