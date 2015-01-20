//
//  CraftARPlugin.m
//  HelloCordova
//
//  Created by Daniel Cabrera on 15/07/14.
//
//

#import "CraftARPlugin.h"
#import <Cordova/CDVPluginResult.h>
#import <Cordova/CDVViewController.h>
#import "CraftARViewController.h"

@implementation CraftARPlugin

- (void) startView:(CDVInvokedUrlCommand*)command{
    NSLog(@"CraftAR plugin - StartView");
    CDVPluginResult* pluginResult = nil;

    
    if ([command.arguments count] != 1){
       pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_ERROR];
       [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
       return;
    }

       
    CraftARViewController *target;
    
    UIStoryboard *exampleStoryBoard = [UIStoryboard storyboardWithName:@"CraftAR" bundle:nil];
    target = (CraftARViewController *)[exampleStoryBoard instantiateViewControllerWithIdentifier:@"CraftARViewController"];
    
    NSDictionary *dict =[command.arguments objectAtIndex:0];
    target.overlay = (NSString *) [dict objectForKey:@"loadUrl"];
    
    
    
    [self.viewController presentViewController:target
                                      animated:YES
                                    completion:^{
                                        //pluginResult = [CDVPluginResult resultWithStatus:CDVCommandStatus_OK messageAsString:@""];
                                        [self.commandDelegate sendPluginResult:pluginResult callbackId:command.callbackId];
                                    }];
    
    
    
    
    
}



@end
