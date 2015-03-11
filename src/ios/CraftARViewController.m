//
//  CraftARViewController.m
//  HelloCordova
//
//  Created by Daniel Cabrera on 17/07/14.
//
//

#import "CraftARViewController.h"
#import <CraftARSDK/CRSConnect.h>
#import "MyCRSConnect.h"


@interface CraftARViewController () <CraftARSDKProtocol, CraftARCloudRecognitionProtocol> {
    // CraftAR SDK reference
    CraftARSDK *_sdk;
    
    CraftARCloudRecognition *_cloudRecognition;
    CraftARTracking *_tracking;
    
    BOOL _isTrackingEnabled;
    BOOL _automaticAR;
    BOOL _cameraFrozen;
    NSString *_token;


}
@end

@implementation CraftARViewController


- (id)initWithNibName:(NSString *)nibNameOrNil bundle:(NSBundle *)nibBundleOrNil
{
    self = [super initWithNibName:nibNameOrNil bundle:nibBundleOrNil];
    if (self) {
        // Custom initialization
    }
    return self;
}

- (void)viewDidLoad
{
    [super viewDidLoad];
    
    // setup the CraftAR SDK
    _sdk = [CraftARSDK sharedCraftARSDKWithCRSConnect: [MyCRSConnect class]];

    _sdk.delegate = self;
    
    _cloudRecognition = [_sdk getCloudRecognitionInterface];
    _cloudRecognition.delegate = self;
    
    // setup CordovaWebView
    _cwv = [CDVViewController new];
    _cwv.startPage = _overlay;

    _cwv.view.frame = [[UIScreen mainScreen] applicationFrame];
    _cwv.webView.opaque = NO;
    _cwv.webView.backgroundColor = [UIColor clearColor];
    _cwv.webView.delegate = self;
    [self.view addSubview:_cwv.view];
    
    _cameraFrozen = NO;
    _automaticAR = YES;
    
    
}

- (BOOL)shouldAutorotate{
    return NO;
}

-(NSUInteger)supportedInterfaceOrientations
{
    return UIInterfaceOrientationMaskPortrait;
}


- (void) viewWillAppear:(BOOL) animated {
    [super viewWillAppear:animated];
    // Start Video Preview for search and tracking
    [_sdk startCaptureWithView:self.videoPreviewView];
    [_cloudRecognition stopFinderMode];
}


- (void) viewWillDisappear:(BOOL)animated {
    [super viewWillDisappear:animated];
    
    // Stop the SDK when the view is being closed to cleanup sdk internals.
    [_sdk stopCapture];
}


- (void) startFinding {
    NSLog(@"StartFinding");
    [_cloudRecognition startFinderMode];
}

- (void) stopFinding {
    NSLog(@"StopFinding");
    [_cloudRecognition stopFinderMode];
}

- (BOOL) startTracking {
    NSLog(@"StartTracking");
    _isTrackingEnabled = true;
    [_tracking startTracking];
    
    return _isTrackingEnabled;
}

- (BOOL) stopTracking {
    NSLog(@"StopTracking");
    _isTrackingEnabled = false;
    [_tracking stopTracking];
    
    return _isTrackingEnabled;
}


-(void) restartCamera{
    NSLog(@"restartCamera");
    [_sdk unfreezeCapture];
    _cameraFrozen = NO;

}

-(BOOL) takePictureAndSearch {
    [_sdk takeSnapshot];
    _cameraFrozen = YES;
    /*This return true is to be compatible with the android version*/
    return true;
}


- (void) didGetSnapshot: (UIImage*) snapshot {
    [_sdk freezeCapture];
    [_cloudRecognition searchWithUIImage:snapshot];
}

-(void) setToken: (NSString *) tkn{
    _token = [tkn copy];
    [_cloudRecognition setToken:tkn];
}

-(void) setAutomaticAR:(BOOL) autoAR{
    NSLog(@"automaticAR");
    _automaticAR = autoAR;
}

-(void) setConnectUrl:(NSString *) connectUrl{
    NSLog(@"setConnectUrl");
}

-(void) setSearchUrl:(NSString *) connectUrl{
    NSLog(@"setSearchUrl");
    
}

-(void) stopAR{
    NSLog(@"stopAR");
    [_tracking stopTracking];
    [_tracking removeAllARItems];
    [_sdk unfreezeCapture];
}

-(void) close{
    NSLog(@"close");
    [self dismissViewControllerAnimated:YES completion:nil];
}

/*
@JavascriptInterface
public void setConnectUrl(String connectUrl)
{
    Log.d(ARTAG, "setConnectUrl");
    cloudRecognition.setConnectUrl(connectUrl);
}

@JavascriptInterface
public void setSearchUrl(String searchUrl)
{
    Log.d(ARTAG, "setSearchUrl");
    cloudRecognition.setSearchUrl(searchUrl);
}

*/

- (void) didFailWithError:(CraftARSDKError *)error {
    NSLog(@"Error: %@", [error localizedDescription]);
    
    NSString *params = [NSString stringWithFormat:@"requestFailedResponse(%d,\"%@\");", error.code, error.localizedDescription];
    [_cwv.webView performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:) withObject:params waitUntilDone:NO];

    
}

- (void) didValidateToken {
    // Called after setToken or startSearch if the token is valid.
}
- (void) didGetSearchResults:(NSArray *)results {
    NSLog(@"didSearchResults");
    
    if (_automaticAR && _cameraFrozen){
        [self restartCamera];
    }
    
    NSMutableArray *itemir = [[NSMutableArray alloc] init];
    NSMutableArray *itemar = [[NSMutableArray alloc] init];

    for (CraftARItem* item in results) {
        if (item.getType == ITEM_TYPE_AR) {
            [itemar addObject:item];
        }
        else {
            [itemir addObject:item];
        }
    }
    [self didGetSearchResultsIR:itemir];
    [self didGetSearchResultsAR:itemar];
}

- (void) didGetSearchResultsIR:(NSArray *)results {
    NSError *error;

    NSMutableArray *arrayOfDicts = [[NSMutableArray alloc] init];
    
    for(CraftARItem *item in results)
    {
        NSDictionary *dictObject =  [self generateDictionaryJson:item];
        [arrayOfDicts addObject:dictObject];
    }
    
    NSData *dictsData = [NSJSONSerialization dataWithJSONObject:arrayOfDicts
                                                        options:NSJSONWritingPrettyPrinted error:&error];
    
    NSString *JSONparams = [[NSString alloc] initWithData:dictsData encoding:NSUTF8StringEncoding];
    
    NSString *params =[NSString stringWithFormat:@"searchCompletedIR(%@);", JSONparams];
    [_cwv.webView performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:) withObject:params waitUntilDone:NO];
}

- (void) didGetSearchResultsAR:(NSArray *)results {
    if(!_automaticAR && !_isTrackingEnabled) return;
    
    BOOL haveContent = false;
    for (CraftARItem *item in results) {
        
        if (item.getType == ITEM_TYPE_AR) {
            
            CraftARItemAR *arItem = (CraftARItemAR *) item;
            [_tracking addARItem:arItem];
            haveContent = true;
            
        }
    }
    if (haveContent) {
        [_cloudRecognition stopFinderMode];
        //[_cwv.webView stringByEvaluatingJavaScriptFromString:@"ARFound();"];
        NSString *params = @"ARFound();";
        [_cwv.webView performSelectorOnMainThread:@selector(stringByEvaluatingJavaScriptFromString:) withObject:params waitUntilDone:NO];
        [_tracking startTracking];
        _isTrackingEnabled = true;

    }
}

- (NSDictionary *) generateDictionaryJson:(CraftARItem *) item{
    NSError *error;

    NSString *content = nil;
    
    if (item.contents!=nil){
        NSData *contentsData;
        contentsData = [NSJSONSerialization dataWithJSONObject:item.contents
                                                           options:NSJSONWritingPrettyPrinted error:&error];
        content =  [[NSString alloc] initWithData:contentsData encoding:NSUTF8StringEncoding];
    }


    NSDictionary *dictItem = [NSDictionary dictionaryWithObjectsAndKeys:
                              (content)?content:@"null", @"content",
                              (item.url)?item.url:@"null", @"url",
                              (item.itemId)?item.itemId:@"null",  @"uuid",
                              (item.itemName)?item.itemName:@"null", @"name",
                              (item.custom)?item.custom:@"null", @"custom",
                              nil];
    
    
    NSDictionary *dictImg = [NSDictionary dictionaryWithObjectsAndKeys:
                              (item.thumbnail120)?item.thumbnail120:@"null", @"thumb_120",
                              (item.imageId)?item.imageId:@"null", @"uuid",
                             nil];
    
    
    NSData *itemsData = [NSJSONSerialization dataWithJSONObject:dictItem
                                                        options:NSJSONWritingPrettyPrinted error:&error];
    NSData *imageData = [NSJSONSerialization dataWithJSONObject:dictImg
                                                        options:NSJSONWritingPrettyPrinted error:&error];
    
    return [NSDictionary dictionaryWithObjectsAndKeys:
                                 [item.score stringValue], @"score",
                                [[NSString alloc] initWithData:imageData encoding:NSUTF8StringEncoding], @"image",
                                [[NSString alloc] initWithData:itemsData encoding:NSUTF8StringEncoding], @"item",
                                nil];
}

- (void) didStartCapture {
    NSLog(@"didStartCapture");
    // Get the CloudRecognition and set self as delegate to receive search responses
    _cloudRecognition = [_sdk getCloudRecognitionInterface];
    [_cloudRecognition setDelegate:self];
    
    // Get the Tracking instance
    _tracking = [_sdk getTrackingInterface];

}

- (void) webViewDidFinishLoad:(UIWebView*) theWebView
{
    return [_cwv webViewDidFinishLoad:theWebView];
}

- (void) webViewDidStartLoad:(UIWebView*)theWebView
{
    return [_cwv webViewDidStartLoad:theWebView];
}

- (void) webView:(UIWebView*)theWebView didFailLoadWithError:(NSError*)error
{
    return [_cwv webView:theWebView didFailLoadWithError:error];
}

- (BOOL) webView:(UIWebView*)theWebView shouldStartLoadWithRequest:(NSURLRequest*)request navigationType:(UIWebViewNavigationType)navigationType
{
    BOOL b = YES;
    
    // Break apart request URL
    NSString *requestString = [[request URL] absoluteString];
    NSArray *components = [requestString componentsSeparatedByString:@":"];
    
    // Check for your protocol
    if ([components count] > 1 &&
        [(NSString *)[components objectAtIndex:0] isEqualToString:@"craftar"])
    {
        
        if ([components count] == 2){
            
            if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//startFinding"]){
                [self startFinding];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//stopFinding"]){
                [self stopFinding];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//startTracking"]){
                [self startTracking];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//stopTracking"]){
                [self stopTracking];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//restartCamera"]){
                [self restartCamera];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//takePictureAndSearch"]){
                [self takePictureAndSearch];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//stopAR"]){
                [self stopAR];
                b = NO;
            }
            else if ([(NSString *)[components objectAtIndex:1] isEqualToString:@"//close"]){
                [self close];
                b = NO;
            } else if ([(NSString *) [components objectAtIndex:1] rangeOfString:@"//setToken?p="].location != NSNotFound){
                NSArray *paramsComponents = [(NSString *)[components objectAtIndex:1] componentsSeparatedByString:@"="];
                NSString *token = (NSString *)[paramsComponents objectAtIndex:1];
                [self setToken:[token stringByDecodingURLFormat]];
                b = NO;
            } else if ([(NSString *)[components objectAtIndex:1] containsString:@"//setAutomaticAR?p="]) {
                NSArray *paramsComponents = [(NSString *)[components objectAtIndex:1] componentsSeparatedByString:@"="];
                NSString *boolParam = (NSString *)[paramsComponents objectAtIndex:1];
                [self setAutomaticAR:[boolParam boolValue]];
                b = NO;
            }
        }
        
    }
    else{
        b = [_cwv webView:theWebView shouldStartLoadWithRequest:request navigationType:navigationType];
    }

    
    return b;
}

- (void)didReceiveMemoryWarning
{
    [super didReceiveMemoryWarning];
    
}


@end
