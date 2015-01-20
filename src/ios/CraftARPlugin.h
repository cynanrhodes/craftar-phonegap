//
//  CraftARPlugin.h
//  HelloCordova
//
//  Created by Daniel Cabrera on 15/07/14.
//
//

#import <Foundation/Foundation.h>
#import <Cordova/CDVPlugin.h>

@interface CraftARPlugin : CDVPlugin

- (void) startView:(CDVInvokedUrlCommand*)command;

@end
